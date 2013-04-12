/*
 *  This software copyright by various authors including the RPTools.net
 *  development team, and licensed under the LGPL Version 3 or, at your
 *  option, any later version.
 *
 *  Portions of this software were originally covered under the Apache
 *  Software License, Version 1.1 or Version 2.0.
 *
 *  See the file LICENSE elsewhere in this distribution for license details.
 */

package net.sbbi.upnp.jmx;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle HTTP UPNP requests on UPNPMBeanDevices
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPMBeanDevicesRequestsHandler implements Runnable {
  
  private final static Log log = LogFactory.getLog( UPNPMBeanDevicesRequestsHandler.class );
  private final static int MAX_HTTP_WORKERS = 10;
  
  private final static Map<String, UPNPMBeanDevicesRequestsHandler> instances = new HashMap<String, UPNPMBeanDevicesRequestsHandler>();
  
  private Set<UPNPMBeanDevice> handledDevices = new HashSet<UPNPMBeanDevice>();
  private Set<HttpWorker> httpWorkers = new HashSet<HttpWorker>();
  private ServerSocket srv;
  private boolean isRunning = false;
  
  private InetSocketAddress bindAddress;
  
  public final static UPNPMBeanDevicesRequestsHandler getInstance( InetSocketAddress bindAddress ) {
    String key = bindAddress.toString();
    synchronized( instances ) {
      UPNPMBeanDevicesRequestsHandler handler = instances.get( key );
      if ( handler == null ) {
        handler = new UPNPMBeanDevicesRequestsHandler( bindAddress );
        instances.put( key, handler );
      }
      return handler;
    }
  }
  
  private UPNPMBeanDevicesRequestsHandler( InetSocketAddress bindAddress ) {
    this.bindAddress = bindAddress;
  }
  
  protected void addUPNPMBeanDevice( UPNPMBeanDevice rootDevice ) {
    synchronized( handledDevices ) {
      for ( Iterator<UPNPMBeanDevice> i = handledDevices.iterator(); i.hasNext(); ) {
        UPNPMBeanDevice registred = i.next();
        if ( registred.getDeviceType().equals( rootDevice.getDeviceType() ) && 
             registred.getUuid().equals( rootDevice.getUuid() ) ) {
          // API Use error
          throw new RuntimeException( "An UPNPMBeanDevice object of type " + rootDevice.getDeviceType() + 
                                      " with uuid " + rootDevice.getUuid() +
                                      " is already registred within this class, use a different UPNPMBeanDevice internalId" );
        }
      }
      if ( handledDevices.isEmpty() ) {
        Thread runner = new Thread( this, "UPNPMBeanDevicesRequestsHandler " + bindAddress.toString() );
        runner.setDaemon( true );
        runner.start();
      }
      handledDevices.add( rootDevice );
      // adding the child devices
      for ( Iterator<UPNPMBeanDevice> i = rootDevice.getUPNPMBeanChildrens().iterator(); i.hasNext(); ) {
        handledDevices.add( i.next() );
      }
    }
  }
  
  protected void removeUPNPMBeanDevice( UPNPMBeanDevice rootDevice ) {
    synchronized( handledDevices ) {
      if ( handledDevices.contains( rootDevice ) ) {
        handledDevices.remove( rootDevice );
        // removing the child devices
        for ( Iterator<UPNPMBeanDevice> i = rootDevice.getUPNPMBeanChildrens().iterator(); i.hasNext(); ) {
          handledDevices.remove( i.next() );
        }
        if ( handledDevices.isEmpty() ) {
          try {
            isRunning = false;
            srv.close();
          } catch ( IOException ex ) {
            // do not care
          }
        }
      }
    }
  }
  
  protected void notifyWorkerThreadEnd( HttpWorker worker ) {
    synchronized( httpWorkers ) {
      httpWorkers.remove( worker );
    }
  }
  
  public void run() {
    try {
      srv = new ServerSocket( bindAddress.getPort(), 200, bindAddress.getAddress() );
    } catch ( IOException ex ) {
      log.error( "Error during server socket creation, thread cannot start", ex );
      return;
    }
    isRunning = true;
    while ( isRunning ) {
      try {
        Socket skt = srv.accept();
        skt.setSoTimeout( 30000 );
        HttpWorker worker = new HttpWorker(skt, log, handledDevices, this );
        while ( httpWorkers.size() >= MAX_HTTP_WORKERS ) {
          try {
            Thread.sleep( 100 );
          } catch ( InterruptedException ex ) {
            // ignore
          }
        }
        Thread workerThread = new Thread( worker, "UPNPMBeanDevicesRequestsHandler Http Worker " + httpWorkers.size() );
        workerThread.start();
        synchronized( httpWorkers ) {
          httpWorkers.add( worker );
        }
      } catch ( IOException ex ) {
        if ( isRunning ) {
          log.error( "Error during client socket creation", ex );
        }
      }
    }
  }
  
  private class HttpWorker implements Runnable {
    
    private Socket client;
    private Log logger;
    private Set<UPNPMBeanDevice> devices;
    private UPNPMBeanDevicesRequestsHandler handler;
    
    public HttpWorker( Socket client, Log log, Set<UPNPMBeanDevice> handledDevices, UPNPMBeanDevicesRequestsHandler handler ) {
      this.client = client;
      this.logger = log;
      this.devices = handledDevices;
      this.handler = handler;
    }
    
    public void run() {
      try {
        byte[] buffer = new byte[256];
        InputStream in = client.getInputStream();
        StringBuffer request = new StringBuffer();
        int readen = 0;
        String firstReadenData = null;
        while ( readen != -1 ) {
          readen = in.read( buffer );
          String data = new String( buffer, 0, readen );
          if ( firstReadenData == null ) {
            firstReadenData = data.toUpperCase();
          }
          request.append( data );
          // either a simple get request
          String rawRequest = request.toString();
          if ( rawRequest.endsWith( "\r\n\r\n" ) && firstReadenData.startsWith( "GET" ) ) {
            readen = -1;
          } else if ( rawRequest.indexOf( "</s:Envelope>" ) != -1 ) {
            // or a post request with content that should end with </s:Envelope>
            readen = -1;
          }
        }
        
        OutputStream out = client.getOutputStream();
        String req = request.toString().trim();
        if ( logger.isDebugEnabled() ) logger.debug( "Received message:\n" + req );
        String toWrite = null;
        if ( req.length() > 0 ) {
          HttpRequest httpReq = new HttpRequest( req );
          if ( httpReq.getHttpCommand() != null && httpReq.getHttpCommandArg() != null && 
               httpReq.getHttpCommandArg().trim().length() > 0 ) {
            String cmd = httpReq.getHttpCommand();
            HttpRequestHandler handler = null;
            if ( cmd.equals( "GET" ) ) {
              handler = HttpGetRequest.getInstance();
              // TODO implement M-POST
            } else if ( cmd.equals( "POST" ) ) {
              handler = HttpPostRequest.getInstance();
            } else if ( cmd.equals( "SUBSCRIBE" ) ) {
              handler = HttpSubscriptionRequest.getInstance();
            } else if ( cmd.equals( "UNSUBSCRIBE" ) ) {
              handler = HttpSubscriptionRequest.getInstance();
            }
            if ( handler != null ) {
              toWrite = handler.service( devices, httpReq );
            }
          }
        }
        if ( toWrite == null ) {
          String content = "<html><head><title>Not found</title></head><body>The requested ressource cannot be found</body></html>";
          StringBuffer rtr = new StringBuffer();
          rtr.append( "HTTP/1.1 404 Not Found\r\n" );
          rtr.append( "CONTENT-LENGTH: " ).append( content.length() ).append( "\r\n" );
          rtr.append( "CONTENT-TYPE: text/html\r\n\r\n" );
          rtr.append( content );
          toWrite = rtr.toString();
        }
        if ( logger.isDebugEnabled() ) logger.debug( "Sending response :\n" + toWrite );
        out.write( toWrite.getBytes() );
        out.flush();
        out.close();
        in.close();
        client.close();
      } catch ( IOException ex ) {
        logger.error( "IO Exception occured during client serving", ex );
      } catch ( Throwable t ) {
        logger.error( "Unexpected Exception occured during client serving", t );
      } finally {
        handler.notifyWorkerThreadEnd( this );
      }
    }
  }
}

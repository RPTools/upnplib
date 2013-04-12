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

package net.sbbi.upnp.samples;

import java.io.IOException;

import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.ServicesEventing;
import net.sbbi.upnp.ServiceEventHandler;
import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.services.UPNPService;

public class MyStateVariableEventsHandler 
  implements ServiceEventHandler {

  public void handleStateVariableEvent( String varName, String newValue ) {
    System.out.println( "State variable " + varName +
                        " changed to " + newValue );
  }
    
  public static void main( String[] args ) {
    
    ServicesEventing instance = ServicesEventing.getInstance();
    MyStateVariableEventsHandler handler = new MyStateVariableEventsHandler();
    instance.setDaemon( false );
    // let's find a device
    UPNPRootDevice[] devices = null;
    try {
      devices = Discovery.discover();
    } catch ( IOException ex ) {
      ex.printStackTrace( System.err );
    }
    if ( devices != null ) {
      UPNPDevice firstDevice = devices[0].getChildDevices()
                                                     .iterator().next();
      UPNPService firstService = firstDevice.getServices()
                                                         .iterator().next();
      try {
        int duration = instance.register( firstService, handler, -1 );
        if ( duration != -1 && duration != 0 ) {
          System.out.println( "State variable events registered for " + duration + " ms" );
        } else if ( duration == 0 ) {
          System.out.println( "State variable events registered for infinite ms" );
        }
        try {
          Thread.sleep( 5000 );
        } catch ( InterruptedException ex ) {
          
        }
        instance.unRegister( firstService, handler );
      } catch ( IOException ex ) {
        ex.printStackTrace( System.err );
        // comm error during registration with device such as timeoutException
      }
    } else {
      System.out.println( "Unable to find devices" );
    }
  }
}

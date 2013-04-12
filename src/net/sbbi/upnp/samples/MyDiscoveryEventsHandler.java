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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sbbi.upnp.DiscoveryAdvertisement;
import net.sbbi.upnp.DiscoveryEventHandler;
import net.sbbi.upnp.devices.UPNPRootDevice;

public class MyDiscoveryEventsHandler 
  implements DiscoveryEventHandler {

  private Map<String, UPNPRootDevice> devices = new HashMap<String, UPNPRootDevice>();

  public void eventSSDPAlive( String usn, String udn, 
                              String nt, String maxAge, 
                              URL location ) {
    System.out.println( "Device " + usn + " at " +
                        location + " of type " +
                        nt + " alive" );
    if ( devices.get( usn ) == null ) {
      // let's create the device
      UPNPRootDevice device = null;
      try {
        device = new UPNPRootDevice( location, maxAge );
        devices.put( usn, device );
        System.out.println( "Device " + usn + " added" );
        // and now let's play with the device..
      } catch ( MalformedURLException ex ) {
        // should never happen unless the UPNP devices
        // sends crappy URLs
      }
    }
  }

  public void eventSSDPByeBye( String usn, String udn, 
                               String nt ) {
    if ( devices.get( usn ) != null ) {
      devices.remove( usn );
      System.out.println( "Device " + usn + " leaves" );
    }
  }

  public static void main( String[] args ) throws IOException {
    // let's look for all root devices joining the network
    // ( "upnp:rootdevice" ) and set the events handler thread
    // as a non deamon thread so that the JVM does not stop
    // when the main static methods ends
    DiscoveryAdvertisement instance = DiscoveryAdvertisement.getInstance();
    MyDiscoveryEventsHandler handler = new MyDiscoveryEventsHandler();
    instance.setDaemon( false );
    instance.registerEvent( DiscoveryAdvertisement.EVENT_SSDP_ALIVE, 
                            "upnp:rootdevice", handler );
  }
}

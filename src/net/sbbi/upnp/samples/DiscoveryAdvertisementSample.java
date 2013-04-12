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

import net.sbbi.upnp.*;

/**
 * Sample class for discovery advertisement service sample.
 * This sampe register an event of type ssdp:byebye ( when a device is turned off )
 * and an event of type ssdp:alive ( when a device joins the network )
 * and print some data when such event occurs.
 * You'll need to start this sample class then turn off or on your device to see
 * this sample in action.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class DiscoveryAdvertisementSample {
    public final static void main( String args[] ) throws IOException {
	AdvHandler handler = new AdvHandler();
	
	DiscoveryAdvertisement.getInstance().setDaemon( false );
	System.out.println( "Registering EVENT_SSDP_ALIVE event" );
	DiscoveryAdvertisement.getInstance().registerEvent( DiscoveryAdvertisement.EVENT_SSDP_ALIVE, "upnp:rootdevice", handler );
	System.out.println( "Registering EVENT_SSDP_BYE_BYE event" );
	DiscoveryAdvertisement.getInstance().registerEvent( DiscoveryAdvertisement.EVENT_SSDP_BYE_BYE, "upnp:rootdevice", handler );
	System.out.println( "Waiting for incoming events" );
}

    private static class AdvHandler implements DiscoveryEventHandler {
	public void eventSSDPAlive( String usn, String udn, String nt, String maxAge, java.net.URL location ) {
		System.out.println( "Root device at " + location + " plugged in network, advertisement will expire in " + maxAge + " ms" );
	}

	public void eventSSDPByeBye( String usn, String udn, String nt ) {
		System.out.println( "Bye Bye usn:" + usn + " udn:" + udn + " nt:" + nt );
	}
    }
}

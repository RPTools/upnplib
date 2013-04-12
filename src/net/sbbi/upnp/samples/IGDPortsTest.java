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
import java.net.InetAddress;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPResponseException;

/**
 * This will use the net.sbbi.upnp.impls.InternetGatewayDevice class to discover
 * IGD devices on the nework via the getDevices() method and try to open and then close
 * port 9090 on the IGD device.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class IGDPortsTest {
  public final static void main( String args[] ) {
    int discoveryTiemout = 5000; // 5 secs
    try {
      InternetGatewayDevice[] IGDs = InternetGatewayDevice.getDevices( discoveryTiemout );
      if ( IGDs != null ) {
        for ( int i = 0; i < IGDs.length; i++ ) {
          InternetGatewayDevice testIGD = IGDs[i];
          System.out.println( "Found device " + testIGD.getIGDRootDevice().getModelName() );
          System.out.println( "NAT table size is " + testIGD.getNatTableSize() );
          // now let's open the port
          String localHostIP = InetAddress.getLocalHost().getHostAddress();
          boolean mapped = testIGD.addPortMapping(
	      "Some mapping description", null, 9090, 9090,
	      localHostIP, 0, "TCP" );
          if ( mapped ) {
            System.out.println( "Port 9090 mapped to " + localHostIP );
            System.out.println( "Current mappings count is " + testIGD.getNatMappingsCount() );
            // checking on the device
            ActionResponse resp = testIGD.getSpecificPortMappingEntry( null, 9090, "TCP" );
            if ( resp != null ) {
              System.out.println( "Port 9090 mapping confirmation received from device" );
            }
            // and now close it
            boolean unmapped = testIGD.deletePortMapping( null, 9090, "TCP" );
            if ( unmapped ) {
              System.out.println( "Port 9090 unmapped" );
            }
          }
        }
      } else {
        System.out.println( "Unable to find IGD on your network" );
      }
    } catch ( IOException ex ) {
      System.err.println( "IOException occured during discovery or ports mapping " + ex.getMessage() );
    } catch( UPNPResponseException respEx ) {
      System.err.println( "UPNP device unhappy " +
	  respEx.getDetailErrorCode() + " " +
	  respEx.getDetailErrorDescription() );
    }
  }
}

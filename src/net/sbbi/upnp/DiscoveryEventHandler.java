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

package net.sbbi.upnp;

/**
 * Interface for object that want to receive events from the 
 * DiscoveryAdvertisement thread
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public interface DiscoveryEventHandler {

  /**
   * Called when a device joins the network or advertise it is still alive
   * @param usn the device USN (udn::nt)
   * @param udn the device UDN
   * @param nt  the device NT
   * @param maxAge the device maxAge
   * @param location the device location
   */
  public void eventSSDPAlive( String usn, String udn, String nt, String maxAge, java.net.URL location );
	
  /**
   * Called when a device is leaving the network
   * @param usn the device USN (udn::nt)
   * @param udn the device UDN
   * @param nt the device NT
   */
  public void eventSSDPByeBye( String usn, String udn, String nt );
}

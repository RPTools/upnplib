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
 * This interface can be use to register against the DiscoveryListener class to receive SSDP search responses.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public interface DiscoveryResultsHandler {
	/**
	 * Method called by the DiscoveryListener class when a search response message has been received from the network
	 * 
	 * @param usn
	 *            the device USN
	 * @param udn
	 *            the device UDN
	 * @param nt
	 *            the device NT
	 * @param maxAge
	 *            the message max age
	 * @param location
	 *            the device location
	 * @param firmware
	 *            the device firmware
	 */
	public void discoveredDevice(String usn, String udn, String nt, String maxAge, java.net.URL location, String firmware);
}

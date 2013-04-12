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

import java.util.Set;

/**
 * Interface to define how a class must handle an http request
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public interface HttpRequestHandler {
	/**
	 * Handles the HTTP request
	 * 
	 * @param devices
	 *            the potential target UPNPMBeanDevice objects
	 * @param request
	 *            teh client http request
	 * @return the content who should be sent to the client or null if the request does not match any target
	 *         UPNPMBeanDevice provided in the Set
	 */
	public String service(Set<UPNPMBeanDevice> devices, HttpRequest request);
}

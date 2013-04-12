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
 * Class to handle HTTP UPNP SUBSCRIBE and UNSUBSCRIBE requests on UPNPMBeanDevices
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class HttpSubscriptionRequest implements HttpRequestHandler {
	private final static HttpSubscriptionRequest instance = new HttpSubscriptionRequest();

	public static HttpRequestHandler getInstance() {
		return instance;
	}

	private HttpSubscriptionRequest() {
	}

	public String service(Set<UPNPMBeanDevice> devices, HttpRequest request) {
		return null;
	}
}

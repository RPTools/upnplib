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

import java.net.InetAddress;
import java.net.URL;

/**
 * This class is used to provide information about a subscription done via the ServicesEventing class
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ServiceEventSubscription {
	private String serviceType = null;
	private String serviceId = null;
	private URL serviceURL = null;
	private String SID = null;
	private InetAddress deviceIp = null;
	private int durationTime = 0;

	public ServiceEventSubscription(String serviceType, String serviceId, URL serviceURL,
			String sid, InetAddress deviceIp, int durationTime) {
		this.serviceType = serviceType;
		this.serviceId = serviceId;
		this.serviceURL = serviceURL;
		SID = sid;
		this.deviceIp = deviceIp;
		this.durationTime = durationTime;
	}

	public InetAddress getDeviceIp() {
		return deviceIp;
	}

	/**
	 * Subcription duration in seconds
	 * 
	 * @return sub duration time, 0 for an infinite time
	 */
	public int getDurationTime() {
		return durationTime;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public URL getServiceURL() {
		return serviceURL;
	}

	/**
	 * The subscription ID returned by the UPNPDevice
	 * 
	 * @return subscription id
	 */
	public String getSID() {
		return SID;
	}
}

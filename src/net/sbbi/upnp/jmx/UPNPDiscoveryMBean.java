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

import javax.management.MBeanRegistration;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * MBean to discover UPNP devices on the network and register the devices service as UPNPServiceMBean objects during the
 * MBean registration. The registered UPNPServiceMBean will also be automatically unregistered when the device is
 * leaving the network or when the UPNPDiscoveryMBean is unregistered from teh MBeans server.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public interface UPNPDiscoveryMBean extends MBeanRegistration {

	/**
	 * Notification type for devices joining the network
	 */
	public final static String SSDP_ALIVE_NOTIFICATION = UPNPDiscoveryMBean.class.getName() + ".ssdp.alive";
	/**
	 * Notification type for devices leaving the network
	 */
	public final static String SSDP_BYEBYE_NOTIFICATION = UPNPDiscoveryMBean.class.getName() + ".ssdp.byebye";

	/**
	 * The registered devices search targets
	 * 
	 * @return a set of search targets
	 */
	public Set<String> getSearchTargets();

	/**
	 * Computes an array of object names of registered UPNPServiceMBeans for a given UPNP device UDN
	 * 
	 * @param deviceUDN
	 *            the UPNP device UDN ( unique id on the network )
	 * @return an array of object names or null if not matchs found for the given UDN
	 * @throws MalformedObjectNameException
	 *             if an object name cannot be computed for an UPNPServiceMBean
	 */
	public ObjectName[] getRegisteredUPNPServiceMBeans(String deviceUDN) throws MalformedObjectNameException;

	/**
	 * The list of registered devices UDN, the returned UDN can be used with the getRegisteredUPNPServiceMBeans(String
	 * deviceUDN) method to retreive UDN bound UPNPServiceMBean object names
	 * 
	 * @return a string array of UDN or null if no UPNP device services registered as UPNPServiceMBean
	 */
	public String[] getRegisteredUPNPServiceMBeansUDNs();

}

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

package net.sbbi.upnp.devices;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sbbi.upnp.services.UPNPService;

import org.apache.log4j.Logger;

/**
 * This class represents an UPNP device, this device contains a set of services that will be needed to access the device
 * functionalities.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPDevice {
	private final static Logger log = Logger.getLogger(UPNPDevice.class);

	protected String deviceType;
	protected String friendlyName;
	protected String manufacturer;
	protected URL manufacturerURL;
	protected URL presentationURL;
	protected String modelDescription;
	protected String modelName;
	protected String modelNumber;
	protected String modelURL;
	protected String serialNumber;
	protected String UDN;
	protected String USN;
	protected long UPC;

	protected List<DeviceIcon> deviceIcons;
	protected List<UPNPService> services;
	protected List<UPNPDevice> childDevices;

	protected UPNPDevice parent;

	public URL getManufacturerURL() {
		return manufacturerURL;
	}

	/**
	 * Presentation URL
	 * 
	 * @return URL the presenation URL, or null if the device does not provide such information
	 */
	public URL getPresentationURL() {
		return presentationURL;
	}

	public String getModelDescription() {
		return modelDescription;
	}

	public String getModelName() {
		return modelName;
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public String getModelURL() {
		return modelURL;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public String getUDN() {
		return UDN;
	}

	public String getUSN() {
		return USN;
	}

	public long getUPC() {
		return UPC;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public String getFriendlyName() {
		return friendlyName;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public boolean isRootDevice() {
		return this instanceof UPNPRootDevice;
	}

	/**
	 * Access to the device icons definitions
	 * 
	 * @return a list containing DeviceIcon objects or null if no icons defined
	 */
	public List<DeviceIcon> getDeviceIcons() {
		return deviceIcons;
	}

	/**
	 * Generates a list of all the child ( not only top level, full childrens hierarchy included ) UPNPDevice objects
	 * for this device.
	 * 
	 * @return the generated list or null if no child devices bound
	 */
	public List<UPNPDevice> getChildDevices() {
		if (childDevices == null)
			return null;
		List<UPNPDevice> rtrVal = new ArrayList<UPNPDevice>();
		for (Iterator<UPNPDevice> itr = childDevices.iterator(); itr.hasNext();) {
			UPNPDevice device = itr.next();
			rtrVal.add(device);
			List<UPNPDevice> found = device.getChildDevices();
			if (found != null) {
				rtrVal.addAll(found);
			}
		}
		return rtrVal;
	}

	/**
	 * Generates a list of all the child ( only top level ) UPNPDevice objects for this device.
	 * 
	 * @return the generated list or null if no child devices bound
	 */
	public List<UPNPDevice> getTopLevelChildDevices() {
		if (childDevices == null)
			return null;
		List<UPNPDevice> rtrVal = new ArrayList<UPNPDevice>();
		for (Iterator<UPNPDevice> itr = childDevices.iterator(); itr.hasNext();) {
			UPNPDevice device = itr.next();
			rtrVal.add(device);
		}
		return rtrVal;
	}

	/**
	 * Return the parent UPNPDevice, null if the device is an UPNPRootDevice
	 * 
	 * @return the parent device instance
	 */
	public UPNPDevice getDirectParent() {
		return parent;
	}

	/**
	 * Looks for a child UPNP device definition file, the whole devices tree will be searched, starting from the current
	 * device node.
	 * 
	 * @param deviceURI
	 *            the device URI to search
	 * @return An UPNPDevice if anything matches or null
	 */
	public UPNPDevice getChildDevice(String deviceURI) {
		if (log.isDebugEnabled())
			log.debug("searching for device URI:" + deviceURI);
		if (getDeviceType().equals(deviceURI))
			return this;
		if (childDevices == null)
			return null;
		for (Iterator<UPNPDevice> itr = childDevices.iterator(); itr.hasNext();) {
			UPNPDevice device = itr.next();
			UPNPDevice found = device.getChildDevice(deviceURI);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Looks for all UPNP device service definitions objects
	 * 
	 * @return A list of all device services
	 */
	public List<UPNPService> getServices() {
		if (services == null)
			return null;
		List<UPNPService> rtrVal = new ArrayList<UPNPService>();
		rtrVal.addAll(services);
		return rtrVal;
	}

	/**
	 * Looks for a UPNP device service definition object for the given service URI (Type)
	 * 
	 * @param serviceURI
	 *            the URI of the service
	 * @return A matching UPNPService object or null
	 */
	public UPNPService getService(String serviceURI) {
		if (services == null)
			return null;
		if (log.isDebugEnabled())
			log.debug("searching for service URI:" + serviceURI);
		for (Iterator<UPNPService> itr = services.iterator(); itr.hasNext();) {
			UPNPService service = itr.next();
			if (service.getServiceType().equals(serviceURI)) {
				return service;
			}
		}
		return null;
	}

	/**
	 * Looks for a UPNP device service definition object for the given service ID
	 * 
	 * @param serviceURI
	 *            the ID of the service
	 * @return A matching UPNPService object or null
	 */
	public UPNPService getServiceByID(String serviceID) {
		if (services == null)
			return null;
		if (log.isDebugEnabled())
			log.debug("searching for service ID:" + serviceID);
		for (Iterator<UPNPService> itr = services.iterator(); itr.hasNext();) {
			UPNPService service = itr.next();
			if (service.getServiceId().equals(serviceID)) {
				return service;
			}
		}
		return null;
	}

	/**
	 * Looks for the all the UPNP device service definition object for the current UPNP device object. This method can
	 * be used to retreive multiple same kind ( same service type ) of services with different services id on a device
	 * 
	 * @param serviceURI
	 *            the URI of the service
	 * @return A matching List of UPNPService objects or null
	 */
	public List<UPNPService> getServices(String serviceURI) {
		if (services == null)
			return null;
		List<UPNPService> rtrVal = new ArrayList<UPNPService>();
		if (log.isDebugEnabled())
			log.debug("searching for services URI:" + serviceURI);
		for (Iterator<UPNPService> itr = services.iterator(); itr.hasNext();) {
			UPNPService service = itr.next();
			if (service.getServiceType().equals(serviceURI)) {
				rtrVal.add(service);
			}
		}
		if (rtrVal.isEmpty()) {
			return null;
		}
		return rtrVal;
	}

	/**
	 * The toString return the device type
	 * 
	 * @return the device type
	 */
	@Override
	public String toString() {
		return getDeviceType();
	}
}

/*
 * This software copyright by various authors including the RPTools.net development team, and licensed under the LGPL
 * Version 3 or, at your option, any later version.
 * 
 * Portions of this software were originally covered under the Apache Software License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.sbbi.upnp.samples;

import java.util.Iterator;
import java.util.List;

import net.sbbi.upnp.Discovery;
import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.UPNPMessageFactory;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.UPNPService;

import org.apache.log4j.BasicConfigurator;

/**
 * Sample class to access an UPNP device that implement the Internet Gateway Device specs This sample will simply print
 * the device external ip. We assume that an UPNP device that is implementing IGD is available on the network
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class IGDAccessSample {
	public static void main(String args[]) {
		//DiscoveryAdvertisement.getInstance().notifyEvent(1, null);
		int deviceCount = 0;
		try {
			BasicConfigurator.configure();
			// search for an INTERNET_GATEWAY_DEVICE, we give 1500 ms for the
			// device to respond
			UPNPRootDevice[] rootDevices = Discovery.discover(1500, "urn:schemas-upnp-org:device:InternetGatewayDevice:1");
			// looks like we have received a response now we need to look at a
			// wan connection device for our little search
			if (rootDevices != null) {
				// we take the first device found
				UPNPRootDevice rootDevice = rootDevices[0];
				System.out.println("Plugged to device " + rootDevice.getDeviceType() + ", manufactured by " + rootDevice.getManufacturer() + " model " + rootDevice.getModelName());
				// let's list all the available devices first
				List<UPNPDevice> devices = rootDevice.getChildDevices();
				if (devices != null) {
					devices.add(rootDevice);
					for (Iterator<UPNPDevice> i = devices.iterator(); i.hasNext();) {
						deviceCount++;
						UPNPDevice device = i.next();
						System.out.println();
						System.out.println("type " + device.getDeviceType());
						if (device.getDirectParent() != null) {
							System.out.println("parent type " + device.getDirectParent().getDeviceType());
						}
						List<UPNPService> deviceServices = device.getServices();
						if (deviceServices != null) {
							for (Iterator<UPNPService> iSrv = deviceServices.iterator(); iSrv.hasNext();) {
								UPNPService srv = iSrv.next();
								System.out.println("  service " + srv.getServiceType() + " at " + srv.getSCPDURL());
								for (Iterator<String> itrActions = srv.getAvailableActionsName(); itrActions.hasNext();) {
									System.out.println("\t" + itrActions.next());
								}
							}
						}
					}
				}
				List<UPNPDevice> rootChildDevices = rootDevice.getChildDevices();
				System.out.println("Child devices available : " + rootChildDevices);
				// we lookup for the wan connection device object now.
				UPNPDevice wanConnDevice = rootDevice.getChildDevice("urn:schemas-upnp-org:device:WANConnectionDevice:1");

				if (wanConnDevice != null) {
					// great this device is implemented
					System.out.println("Found required device " + wanConnDevice.getDeviceType());
					// now we need to lookup the service WANIPConnection for our
					// litte action
					UPNPService wanIPSrv = wanConnDevice.getService("urn:schemas-upnp-org:service:WANIPConnection:1");

					if (wanIPSrv != null) {
						System.out.println("Service " + wanIPSrv.getServiceType() + " found\n");
						if (wanIPSrv.getUPNPServiceAction("GetExternalIPAddress") != null) {
							// great our action is available (normal -- this is required
							// by the specs :o) )
							UPNPMessageFactory wanIPMsgFactory = UPNPMessageFactory.getNewInstance(wanIPSrv);
							ActionMessage externalIPAdrMsg = wanIPMsgFactory.getMessage("GetExternalIPAddress");
							List<String> params = externalIPAdrMsg.getInputParameterNames();
							// now we list the needed input parameters for this message;
							// should be empty
							System.out.println("Action required input params:");
							System.out.println(params);
							params = externalIPAdrMsg.getOutputParameterNames();
							// and now the output (returned by the device after the
							// message is sent) params.  Normally only 1 value, the
							// external IP address
							System.out.println("Action returned values:");
							System.out.println(params);
							// Now we send the message to the UPNPDevice and we wait
							// for a response.
							try {
								ActionResponse response = externalIPAdrMsg.service();
								System.out.println("Message response values:");
								for (int i = 0; i < params.size(); i++) {
									String param = params.get(i);
									System.out.println(param + "=" + response.getOutActionArgumentValue(param));
								}
							} catch (UPNPResponseException ex) {
								// can happen if device do not implement state variables
								// queries
							}
							System.out.println("Validity time remaining=" + rootDevice.getValidityTime());
							// now let's try to query a state variable
							System.out.println("Query PortMappingDescription state variable");
							try {
								System.out.println("Response=" + wanIPSrv.getUPNPServiceStateVariable("PortMappingDescription").getValue());
							} catch (UPNPResponseException ex) {
								// can happen if device do not implement state variables
								// queries
							}
						}
					}
				}
			}
			System.out.println("Number of devices found: " + deviceCount);
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
}

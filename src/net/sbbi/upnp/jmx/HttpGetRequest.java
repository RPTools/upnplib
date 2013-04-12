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

import java.util.Iterator;
import java.util.Set;

/**
 * Class to handle HTTP UPNP GET requests on UPNPMBeanDevices
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class HttpGetRequest implements HttpRequestHandler {

	private final static HttpGetRequest instance = new HttpGetRequest();

	public static HttpRequestHandler getInstance() {
		return instance;
	}

	private HttpGetRequest() {
	}

	public String service(Set<UPNPMBeanDevice> devices, HttpRequest request) {
		StringBuffer rtr = null;
		String filePath = request.getHttpCommandArg();
		// Request are : /uuid/desc.xml or /uuid/serviceId/scpd.xml
		boolean validGet = (filePath.startsWith("/") && filePath.endsWith("/desc.xml")) ||
				(filePath.startsWith("/") && filePath.endsWith("/scpd.xml"));

		if (validGet) {
			String uuid = null;
			String serviceUuid = null;
			int lastSlash = filePath.lastIndexOf('/');
			if (lastSlash != -1) {
				uuid = filePath.substring(1, lastSlash);
				serviceUuid = uuid;
				// check if this is an uuid/desc.xml or uuid/serviceId/scpd.xml request type
				int slashIndex = uuid.indexOf("/");
				if (slashIndex != -1) {
					uuid = uuid.substring(0, slashIndex);
				}
			}
			if (uuid != null) {
				// search now the bean within the set
				UPNPMBeanDevice found = null;
				synchronized (devices) {
					for (Iterator<UPNPMBeanDevice> i = devices.iterator(); i.hasNext();) {
						UPNPMBeanDevice dv = i.next();
						if (dv.getUuid().equals(uuid)) {
							found = dv;
							break;
						}
					}
				}
				if (found != null) {
					String contentToReturn = null;
					if (filePath.endsWith("/desc.xml")) {
						contentToReturn = found.getDeviceInfo();
					} else if (filePath.endsWith("/scpd.xml")) {
						UPNPMBeanService srv = found.getUPNPMBeanService(serviceUuid);
						if (srv != null) {
							contentToReturn = srv.getDeviceSCDP();
						}
					}

					rtr = new StringBuffer();
					rtr.append("HTTP/1.1 200 OK\r\n");
					String accept = request.getHTTPHeaderField("CONTENT-LANGUAGE");
					if (accept != null) {
						rtr.append("CONTENT-LANGUAGE: ").append(accept).append("\r\n");
					}
					rtr.append("CONTENT-LENGTH: ").append(contentToReturn.length()).append("\r\n");
					rtr.append("CONTENT-TYPE: text/xml\r\n\r\n");
					rtr.append(contentToReturn);
					return rtr.toString();
				}
			}
		}
		return null;
	}

}

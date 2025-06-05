/*
 * This software copyright by various authors including the RPTools.net development team, and licensed under the LGPL
 * Version 3 or, at your option, any later version.
 * 
 * Portions of this software were originally covered under the Apache Software License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.sbbi.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sbbi.upnp.devices.UPNPRootDevice;

import org.apache.log4j.Logger;

/**
 * Class to discover an UPNP device on the network.</br> A multicast socket will be created to discover devices, the
 * binding port for this socket is set to 1901, if this is causing a problem you can use the
 * net.sbbi.upnp.Discovery.bindPort system property to specify another port. The discovery methods only accept matching
 * device description and broadcast message response IP to avoid a security flaw with the protocol. If you are not happy
 * with such behaviour you can set the net.sbbi.upnp.ddos.matchip system property to false to avoid this check.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class Discovery {
	private final static Logger log = Logger.getLogger(Discovery.class);

	public final static String ROOT_DEVICES = "upnp:rootdevice";
	public final static String ALL_DEVICES = "ssdp:all";

	public static final int DEFAULT_MX = 3;
	public static final int DEFAULT_TTL = 4;
	public static final int DEFAULT_TIMEOUT = 1500;
	public static final String DEFAULT_SEARCH = ALL_DEVICES;
	public static final int DEFAULT_SSDP_SEARCH_PORT = 1901;

	public final static String SSDP_IP = "239.255.255.250";
	public final static int SSDP_PORT = 1900;

	/**
	 * Devices discovering on all network interfaces with default values, all root devices will be searched
	 * 
	 * @return an array of UPNP Root device or null if nothing found with the default timeout. Null does NOT means that
	 *         no UPNP device is available on the network. It only means that for this default timeout no devices
	 *         responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover() throws IOException {
		return discover(DEFAULT_TIMEOUT, DEFAULT_TTL, DEFAULT_MX, DEFAULT_SEARCH);
	}

	/**
	 * Devices discovering on all network interfaces with a given root device to search
	 * 
	 * @param searchTarget
	 *            the device URI to search
	 * @return an array of UPNP Root device that matches the search or null if nothing found with the default timeout.
	 *         Null does NOT means that no UPNP device is available on the network. It only means that for this given
	 *         timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(String searchTarget) throws IOException {
		return discover(DEFAULT_TIMEOUT, DEFAULT_TTL, DEFAULT_MX, Collections.singleton(searchTarget));
	}

	/**
	 * Devices discovering on all network interfaces with a given root device to search
	 * 
	 * @param searchTargets
	 *            the device URIs to search
	 * @return an array of UPNP Root device that matches the search or null if nothing found with the default timeout.
	 *         Null does NOT means that no UPNP device is available on the network. It only means that for this given
	 *         timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(Collection<String> searchTargets) throws IOException {
		return discover(DEFAULT_TIMEOUT, DEFAULT_TTL, DEFAULT_MX, searchTargets);
	}

	/**
	 * Devices discovering on all network interfaces with a given timeout and a given root device to search
	 * 
	 * @param timeOut
	 *            the time allowed for a device to give a response
	 * @param searchTarget
	 *            the device URI to search
	 * @return an array of UPNP Root device that matches the search or null if nothing found with the given timeout.
	 *         Null does NOT means that no UPNP device is available on the network. It only means that for this given
	 *         timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, String searchTarget) throws IOException {
		return discover(timeOut, DEFAULT_TTL, DEFAULT_MX, Collections.singleton(searchTarget));
	}

	/**
	 * Devices discovering on all network interfaces with a given timeout and a given root device to search
	 * 
	 * @param timeOut
	 *            the time allowed for a device to give a response
	 * @param searchTargets
	 *            the device URIs to search
	 * @return an array of UPNP Root device that matches the search or null if nothing found with the given timeout.
	 *         Null does NOT means that no UPNP device is available on the network. It only means that for this given
	 *         timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, Collection<String> searchTargets) throws IOException {
		return discover(timeOut, DEFAULT_TTL, DEFAULT_MX, searchTargets);
	}

	/**
	 * Devices discovering on all network interfaces with a given timeout and a given root device to search, as well as
	 * a ttl and mx param
	 * 
	 * @param timeOut
	 *            the timeout for the a device to give a reponse
	 * @param ttl
	 *            the UDP socket packets time to live
	 * @param mx
	 *            discovery message mx http header field value
	 * @param searchTarget
	 *            the device URI to search
	 * @return an array of UPNP Root device that matches the search or null if nothing found within the given timeout.
	 *         Null return does NOT means that no UPNP device is available on the network. It only means that for this
	 *         given timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, int ttl, int mx, String searchTarget) throws IOException {
		return discoverDevices(timeOut, ttl, mx, Collections.singleton(searchTarget), null);
	}

	/**
	 * Devices discovering on all network interfaces with a given timeout and a given root device to search, as well as
	 * a ttl and mx param
	 * 
	 * @param timeOut
	 *            the timeout for the a device to give a reponse
	 * @param ttl
	 *            the UDP socket packets time to live
	 * @param mx
	 *            discovery message mx http header field value
	 * @param searchTargets
	 *            the device URIs to search
	 * @return an array of UPNP Root device that matches the search or null if nothing found within the given timeout.
	 *         Null return does NOT means that no UPNP device is available on the network. It only means that for this
	 *         given timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, int ttl, int mx, Collection<String> searchTargets) throws IOException {
		return discoverDevices(timeOut, ttl, mx, searchTargets, null);
	}

	/**
	 * Devices discovering with a given timeout and a given root device to search on an given network interface, as well
	 * as a ttl and mx param
	 * 
	 * @param timeOut
	 *            the timeout for the a device to give a reponse
	 * @param ttl
	 *            the UDP socket packets time to live
	 * @param mx
	 *            discovery message mx http header field value
	 * @param searchTarget
	 *            the device URI to search
	 * @param ni
	 *            the networkInterface where to search devices, null to lookup all interfaces
	 * @return an array of UPNP Root device that matches the search or null if nothing found within the given timeout.
	 *         Null return does NOT means that no UPNP device is available on the network. It only means that for this
	 *         given timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, int ttl, int mx, String searchTarget, NetworkInterface ni) throws IOException {
		return discoverDevices(timeOut, ttl, mx, Collections.singleton(searchTarget), ni);
	}

	/**
	 * Devices discovering with a given timeout and a given root device to search on an given network interface, as well
	 * as a ttl and mx param
	 * 
	 * @param timeOut
	 *            the timeout for the a device to give a reponse
	 * @param ttl
	 *            the UDP socket packets time to live
	 * @param mx
	 *            discovery message mx http header field value
	 * @param searchTargets
	 *            the device URIs to search
	 * @param ni
	 *            the networkInterface where to search devices, null to lookup all interfaces
	 * @return an array of UPNP Root device that matches the search or null if nothing found within the given timeout.
	 *         Null return does NOT means that no UPNP device is available on the network. It only means that for this
	 *         given timeout no devices responded or that effectively no devices are available at all.
	 * @throws IOException
	 *             if some IOException occurs during discovering
	 */
	public static UPNPRootDevice[] discover(int timeOut, int ttl, int mx, Collection<String> searchTargets, NetworkInterface ni) throws IOException {
		return discoverDevices(timeOut, ttl, mx, searchTargets, ni);
	}

	private static UPNPRootDevice[] discoverDevices(int timeOut, int ttl, int mx, Collection<String> searchTargets, NetworkInterface ni) throws IOException {
		if (searchTargets == null || searchTargets.size() == 0) {
			throw new IllegalArgumentException("Illegal searchTargets");
		}
		for (Iterator<String> i = searchTargets.iterator(); i.hasNext();) {
			String searchTarget = i.next();
			if (searchTarget == null || searchTarget.trim().length() == 0) {
				throw new IllegalArgumentException("Illegal searchTarget");
			}
		}
		final Map<String, UPNPRootDevice> devices = new HashMap<String, UPNPRootDevice>();

		DiscoveryResultsHandler handler = new DiscoveryResultsHandler() {
			public void discoveredDevice(String usn, String udn, String nt, String maxAge, URL location, String firmware) {
				synchronized (devices) {
					if (!devices.containsKey(usn)) {
						try {
							UPNPRootDevice device = new UPNPRootDevice(location, maxAge, firmware, usn, udn);
							devices.put(usn, device);
						} catch (Exception ex) {
							log.error("Error occurred during UPNP root device object creation from location " + location, ex);
						}
					}
				}
			}
		};

		DiscoveryListener discoveryListener = DiscoveryListener.getInstance();
		for (Iterator<String> i = searchTargets.iterator(); i.hasNext();) {
			String searchTarget = i.next();
			discoveryListener.registerResultsHandler(handler, searchTarget);
		}
		if (ni == null) {
			for (Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
				NetworkInterface intf = e.nextElement();
				for (Enumeration<InetAddress> adrs = intf.getInetAddresses(); adrs.hasMoreElements();) {
					InetAddress adr = adrs.nextElement();
					if (adr instanceof Inet4Address && !adr.isLoopbackAddress()) {
						try {
							sendSearchMessages(adr, ttl, mx, searchTargets);
						} catch (IOException ioe) {
							// Probably 'No route to host' so just ignore this interface and keep going...
						}
					}
				}
			}
		} else {
			for (Enumeration<InetAddress> adrs = ni.getInetAddresses(); adrs.hasMoreElements();) {
				InetAddress adr = adrs.nextElement();
				if (adr instanceof Inet4Address && !adr.isLoopbackAddress()) {
					try {
						sendSearchMessages(adr, ttl, mx, searchTargets);
					} catch (IOException ioe) {
						// Probably 'No route to host' so just ignore this interface and keep going...
					}
				}
			}
		}
		timeOut = timeOut < mx * 1000 ? (mx + 1) * 1000 : timeOut;
		try {
			Thread.sleep(timeOut);
		} catch (InterruptedException ex) {
			// don't care
		}
		for (Iterator<String> i = searchTargets.iterator(); i.hasNext();) {
			String searchTarget = i.next();
			discoveryListener.unRegisterResultsHandler(handler, searchTarget);
		}
		if (devices.isEmpty()) {
			return null;
		}
		int j = 0;
		UPNPRootDevice[] rootDevices = new UPNPRootDevice[devices.size()];
		for (Iterator<UPNPRootDevice> i = devices.values().iterator(); i.hasNext();) {
			rootDevices[j++] = i.next();
		}
		return rootDevices;
	}

	/**
	 * Sends an SSDP search message on the network
	 * 
	 * @param src
	 *            the sender ip
	 * @param ttl
	 *            the time to live
	 * @param mx
	 *            the mx field
	 * @param searchTarget
	 *            the search target
	 * @throws IOException
	 *             if some IO errors occurs during search
	 */
	public static void sendSearchMessage(InetAddress src, int ttl, int mx, String searchTarget) throws IOException {
		sendSearchMessages(src, ttl, mx, Collections.singleton(searchTarget));
	}

	private static void sendSearchMessages(InetAddress src, int ttl, int mx, Collection<String> searchTargets) throws IOException {
		int bindPort = DEFAULT_SSDP_SEARCH_PORT;
		String port = System.getProperty("net.sbbi.upnp.Discovery.bindPort");
		if (port != null) {
			bindPort = Integer.parseInt(port);
		}
		InetAddress groupAdr = InetAddress.getByName(Discovery.SSDP_IP);
		InetSocketAddress adr = new InetSocketAddress(groupAdr, Discovery.SSDP_PORT);
		InetSocketAddress bindAdr = new InetSocketAddress(src, bindPort);

		java.net.MulticastSocket skt = new java.net.MulticastSocket(bindAdr);
//		skt.joinGroup(groupAdr); // Don't need to be a member of the group to send packets to that group...
		skt.setTimeToLive(ttl);
		for (Iterator<String> i = searchTargets.iterator(); i.hasNext();) {
			String searchTarget = i.next();

			StringBuffer packet = new StringBuffer();
			packet.append("M-SEARCH * HTTP/1.1\r\n");
			packet.append("HOST: 239.255.255.250:1900\r\n");
			packet.append("ST: ").append(searchTarget).append("\r\n");
			packet.append("MAN: \"ssdp:discover\"\r\n");
			packet.append("MX: ").append(mx).append("\r\n");
			packet.append("\r\n");
			if (log.isDebugEnabled())
				log.debug("Sending discovery message on 239.255.255.250:1900 multicast address from ip " + src.getHostAddress() + ":\n" + packet.toString());
			String toSend = packet.toString();
			byte[] pk = toSend.getBytes();
			skt.send(new DatagramPacket(pk, pk.length, adr));
		}
		skt.disconnect();
		skt.close();
	}

}

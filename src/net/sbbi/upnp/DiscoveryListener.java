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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class can be used to listen for UPNP devices responses when a search message is sent by a control point ( using
 * the net.sbbi.upnp.Discovery.sendSearchMessage() method )
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class DiscoveryListener implements Runnable {
	private final static Logger log = Logger.getLogger(DiscoveryListener.class);

	private static boolean MATCH_IP = true;

	static {
		String prop = System.getProperty("net.sbbi.upnp.ddos.matchip");
		if (prop != null && prop.equals("false"))
			MATCH_IP = false;
	}

	private static final int DEFAULT_TIMEOUT = 250;

	private final Map<String, Set<DiscoveryResultsHandler>> registeredHandlers = new HashMap<String, Set<DiscoveryResultsHandler>>();

	private final Object REGISTRATION_PROCESS = new Object();

	private final static DiscoveryListener singleton = new DiscoveryListener();

	private boolean inService = false;
	private boolean daemon = true;

	private java.net.MulticastSocket skt;
	private DatagramPacket input;

	private DiscoveryListener() {
	}

	public final static DiscoveryListener getInstance() {
		return singleton;
	}

	/**
	 * Sets the listener as a daemon thread
	 * 
	 * @param daemon
	 *            daemon thread
	 */
	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	/**
	 * Registers an SSDP response message handler
	 * 
	 * @param resultsHandler
	 *            the SSDP response message handler
	 * @param searchTarget
	 *            the search target
	 * @throws IOException
	 *             if some errors occurs during SSDP search response messages listener thread startup
	 */
	public void registerResultsHandler(DiscoveryResultsHandler resultsHandler, String searchTarget) throws IOException {
		synchronized (REGISTRATION_PROCESS) {
			if (!inService)
				startDevicesListenerThread();
			Set<DiscoveryResultsHandler> handlers = registeredHandlers.get(searchTarget);
			if (handlers == null) {
				handlers = new HashSet<DiscoveryResultsHandler>();
				registeredHandlers.put(searchTarget, handlers);
			}
			handlers.add(resultsHandler);
		}
	}

	/**
	 * Unregisters an SSDP response message handler
	 * 
	 * @param resultsHandler
	 *            the SSDP response message handler
	 * @param searchTarget
	 *            the search target
	 */
	public void unRegisterResultsHandler(DiscoveryResultsHandler resultsHandler, String searchTarget) {
		synchronized (REGISTRATION_PROCESS) {
			Set<DiscoveryResultsHandler> handlers = registeredHandlers.get(searchTarget);
			if (handlers != null) {
				handlers.remove(resultsHandler);
				if (handlers.size() == 0) {
					registeredHandlers.remove(searchTarget);
				}
			}
			if (registeredHandlers.size() == 0) {
				stopDevicesListenerThread();
			}
		}
	}

	private void startDevicesListenerThread() throws IOException {
		synchronized (singleton) {
			if (!inService) {
				this.startMultiCastSocket();
				Thread deamon = new Thread(this, "DiscoveryListener daemon");
				deamon.setDaemon(daemon);
				deamon.start();
				while (!inService) {
					// wait for the thread to be started let's wait a few ms
					try {
						Thread.sleep(2);
					} catch (InterruptedException ex) {
						// don t care
					}
				}
			}
		}
	}

	private void stopDevicesListenerThread() {
		synchronized (singleton) {
			inService = false;
		}
	}

	private void startMultiCastSocket() throws IOException {
		int bindPort = Discovery.DEFAULT_SSDP_SEARCH_PORT;
		String port = System.getProperty("net.sbbi.upnp.Discovery.bindPort");
		if (port != null) {
			bindPort = Integer.parseInt(port);
		}
		skt = new java.net.MulticastSocket(null);
		skt.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), bindPort));
		skt.setTimeToLive(Discovery.DEFAULT_TTL);
		skt.setSoTimeout(DEFAULT_TIMEOUT);
		skt.joinGroup(InetAddress.getByName(Discovery.SSDP_IP));
		byte[] buf = new byte[2048];
		input = new DatagramPacket(buf, buf.length);
	}

	public void run() {
		if (!Thread.currentThread().getName().equals("DiscoveryListener daemon")) {
			throw new RuntimeException("No right to call this method");
		}
		inService = true;
		while (inService) {
			try {
				listenBroadCast();
			} catch (SocketTimeoutException ex) {
				// ignoring
			} catch (IOException ioEx) {
				log.error("IO Exception during UPNP DiscoveryListener messages listening thread", ioEx);
			} catch (Exception ex) {
				log.error("Fatal Error during UPNP DiscoveryListener messages listening thread, thread will exit", ex);
				inService = false;
			}
		}
		try {
			skt.leaveGroup(InetAddress.getByName(Discovery.SSDP_IP));
			skt.close();
		} catch (Exception ex) {
			// ignoring
		}
	}

	private void listenBroadCast() throws IOException {
		skt.receive(input);
		InetAddress from = input.getAddress();
		String received = new String(input.getData(), input.getOffset(), input.getLength());
		HttpResponse msg = null;
		try {
			msg = new HttpResponse(received);
		} catch (IllegalArgumentException ex) {
			// crappy http sent
			if (log.isDebugEnabled())
				log.debug("Skipping non-compliant HTTP message " + received);
			return;
		}
		String header = msg.getHeader();
		if (header != null && header.startsWith("HTTP/1.1 200 OK") && msg.getHTTPHeaderField("st") != null) {
			// probably a search repsonse !
			String deviceDescrLoc = msg.getHTTPHeaderField("location");
			if (deviceDescrLoc == null || deviceDescrLoc.trim().length() == 0) {
				if (log.isDebugEnabled())
					log.debug("Skipping SSDP message, missing HTTP header 'location' field");
				return;
			}
			URL loc = new URL(deviceDescrLoc);
			if (MATCH_IP) {
				InetAddress locHost = InetAddress.getByName(loc.getHost());
				if (!from.equals(locHost)) {
					log.warn("Discovery message sender IP " + from +
							" does not match device description IP " + locHost +
							" skipping device, set the net.sbbi.upnp.ddos.matchip system property" +
							" to false to avoid this check");
					return;
				}
			}
			if (log.isDebugEnabled())
				log.debug("Processing " + deviceDescrLoc + " device description location");
			String st = msg.getHTTPHeaderField("st");
			if (st == null || st.trim().length() == 0) {
				if (log.isDebugEnabled())
					log.debug("Skipping SSDP message, missing HTTP header 'st' field");
				return;
			}
			String usn = msg.getHTTPHeaderField("usn");
			if (usn == null || usn.trim().length() == 0) {
				if (log.isDebugEnabled())
					log.debug("Skipping SSDP message, missing HTTP header 'usn' field");
				return;
			}
			String maxAge = msg.getHTTPFieldElement("Cache-Control", "max-age");
			if (maxAge == null || maxAge.trim().length() == 0) {
				if (log.isDebugEnabled())
					log.debug("Skipping SSDP message, missing HTTP header 'max-age' field");
				return;
			}
			String server = msg.getHTTPHeaderField("server");
			if (server == null || server.trim().length() == 0) {
				if (log.isDebugEnabled())
					log.debug("Skipping SSDP message, missing HTTP header 'server' field");
				return;
			}
			String udn = usn;
			int index = udn.indexOf("::");
			if (index != -1)
				udn = udn.substring(0, index);
			synchronized (REGISTRATION_PROCESS) {
				Set<DiscoveryResultsHandler> handlers = registeredHandlers.get(st);
				if (handlers != null) {
					for (Iterator<DiscoveryResultsHandler> i = handlers.iterator(); i.hasNext();) {
						DiscoveryResultsHandler handler = i.next();
						handler.discoveredDevice(usn, udn, st, maxAge, loc, server);
					}
				}
			}
		} else {
			if (log.isDebugEnabled())
				log.debug("Skipping non-compliant HTTP message " + received);
		}
	}
}

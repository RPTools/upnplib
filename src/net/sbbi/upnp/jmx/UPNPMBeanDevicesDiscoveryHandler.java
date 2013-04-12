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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sbbi.upnp.Discovery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class to handle UPNP discovery mechanism on UPNPMBeanDevice
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPMBeanDevicesDiscoveryHandler implements Runnable {

	private final static Log log = LogFactory.getLog(UPNPMBeanDevicesDiscoveryHandler.class);

	private final static Map<String, UPNPMBeanDevicesDiscoveryHandler> instances = new HashMap<String, UPNPMBeanDevicesDiscoveryHandler>();

	private final Set<UPNPMBeanDevice> handledDevices = new HashSet<UPNPMBeanDevice>();
	private java.net.MulticastSocket skt;
	private boolean isRunning = false;
	private boolean isRunningSSDPDaemon = false;

	private final InetSocketAddress bindAddress;

	public final static UPNPMBeanDevicesDiscoveryHandler getInstance(InetSocketAddress bindAddress) {
		String key = bindAddress.toString();
		synchronized (instances) {
			UPNPMBeanDevicesDiscoveryHandler handler = instances.get(key);
			if (handler == null) {
				handler = new UPNPMBeanDevicesDiscoveryHandler(bindAddress);
				instances.put(key, handler);
			}
			return handler;
		}
	}

	private UPNPMBeanDevicesDiscoveryHandler(InetSocketAddress bindAddress) {
		this.bindAddress = bindAddress;
	}

	protected void addUPNPMBeanDevice(UPNPMBeanDevice rootDevice) throws IOException {
		if (!rootDevice.isRootDevice())
			return;
		synchronized (handledDevices) {
			for (Iterator<UPNPMBeanDevice> i = handledDevices.iterator(); i.hasNext();) {
				UPNPMBeanDevice registred = i.next();
				if (registred.getDeviceType().equals(rootDevice.getDeviceType()) &&
						registred.getUuid().equals(rootDevice.getUuid())) {
					// API Use error
					throw new RuntimeException("An UPNPMBeanDevice object of type " + rootDevice.getDeviceType() +
							" with uuid " + rootDevice.getUuid() +
							" is already registred within this class, use a different UPNPMBeanDevice internalId");
				}
			}
			if (handledDevices.isEmpty()) {
				Thread runner = new Thread(this, "UPNPMBeanDeviceDiscoveryHandler " + bindAddress.toString());
				runner.setDaemon(true);
				runner.start();

				SSDPAliveBroadcastMessageSender sender = new SSDPAliveBroadcastMessageSender(handledDevices);
				Thread runner2 = new Thread(sender, "SSDPAliveBroadcastMessageSender " + bindAddress.toString());
				runner2.setDaemon(true);
				runner2.start();

			}
			sendHello(rootDevice);
			handledDevices.add(rootDevice);
		}
	}

	protected void removeUPNPMBeanDevice(UPNPMBeanDevice rootDevice) throws IOException {
		if (!rootDevice.isRootDevice())
			return;
		synchronized (handledDevices) {
			if (handledDevices.contains(rootDevice)) {
				handledDevices.remove(rootDevice);
				sendByeBye(rootDevice);
				if (handledDevices.isEmpty() && isRunning) {
					isRunning = false;
					isRunningSSDPDaemon = false;
					skt.close();
				}
			}
		}
	}

	private void sendHello(UPNPMBeanDevice dv) throws IOException {
		InetAddress group = InetAddress.getByName("239.255.255.250");
		java.net.MulticastSocket multi = new java.net.MulticastSocket(bindAddress.getPort());
		multi.setInterface(bindAddress.getAddress());
		multi.setTimeToLive(dv.getSSDPTTL());

		List<String> packets = getReplyMessages(dv, true, dv.getSSDPAliveDelay());
		for (int i = 0; i < packets.size(); i++) {
			String packet = packets.get(i);
			if (log.isDebugEnabled())
				log.debug("Sending ssdp alive message on 239.255.255.250:1900 multicast address:\n" + packet.toString());
			byte[] pk = packet.getBytes();
			multi.send(new DatagramPacket(pk, pk.length, group, 1900));
		}
		multi.close();
	}

	private void sendByeBye(UPNPMBeanDevice dv) throws IOException {
		InetAddress group = InetAddress.getByName("239.255.255.250");
		java.net.MulticastSocket multi = new java.net.MulticastSocket(bindAddress.getPort());
		multi.setInterface(bindAddress.getAddress());
		multi.setTimeToLive(dv.getSSDPTTL());

		List<String> packets = getByeByeReplyMessages(dv);
		for (int i = 0; i < packets.size(); i++) {
			String packet = packets.get(i);
			if (log.isDebugEnabled())
				log.debug("Sending ssdp:byebye message on 239.255.255.250:1900 multicast address:\n" + packet.toString());
			byte[] pk = packet.getBytes();
			multi.send(new DatagramPacket(pk, pk.length, group, 1900));
		}
		multi.close();
	}

	private List<String> getReplyMessages(UPNPMBeanDevice rootDevice, boolean ssdpAliveMsg, int maxAge) {
		// TODO handle custom NT and ST
		// TODO create a thread to dispatch ssdp:alive messages
		List<String> rtrVal = new ArrayList<String>();

		StringBuffer basePacket = new StringBuffer();
		StringBuffer packet = null;
		if (ssdpAliveMsg) {
			basePacket.append("NOTIFY * HTTP/1.1\r\n");
			basePacket.append("HOST: 239.255.255.250:1900\r\n");
		} else {
			basePacket.append("HTTP/1.1 200 OK\r\n");
		}
		basePacket.append("CACHE-CONTROL: max-age = ").append(maxAge).append("\r\n");
		basePacket.append("LOCATION: ").append(rootDevice.getLocation()).append("\r\n");
		basePacket.append("SERVER: ").append(UPNPMBeanDevice.IMPL_NAME).append("\r\n");

		// 3 messages for the root device
		packet = new StringBuffer(basePacket.toString());
		if (ssdpAliveMsg) {
			packet.append("NT: uuid:").append(rootDevice.getUuid()).append("\r\n");
			packet.append("NTS: ssdp:alive\r\n");
		} else {
			packet.append("ST: uuid:").append(rootDevice.getUuid()).append("\r\n");
			packet.append("EXT:\r\n");
		}
		packet.append("USN: uuid:").append(rootDevice.getUuid()).append("\r\n\r\n");
		rtrVal.add(packet.toString());

		packet = new StringBuffer(basePacket.toString());
		if (ssdpAliveMsg) {
			packet.append("NT: ").append(rootDevice.getDeviceType()).append("\r\n");
			packet.append("NTS: ssdp:alive\r\n");
		} else {
			packet.append("ST: ").append(rootDevice.getDeviceType()).append("\r\n");
			packet.append("EXT:\r\n");
		}
		packet.append("USN: uuid:").append(rootDevice.getUuid()).append("::").append(rootDevice.getDeviceType()).append("\r\n\r\n");
		rtrVal.add(packet.toString());

		packet = new StringBuffer(basePacket.toString());
		if (ssdpAliveMsg) {
			packet.append("NT: upnp:rootdevice\r\n");
			packet.append("NTS: ssdp:alive\r\n");
		} else {
			packet.append("ST: upnp:rootdevice\r\n");
			packet.append("EXT:\r\n");
		}
		packet.append("USN: uuid:").append(rootDevice.getUuid()).append("::upnp:rootdevice\r\n\r\n");
		rtrVal.add(packet.toString());

		packet = new StringBuffer(basePacket.toString());

		List<UPNPMBeanService> services = new ArrayList<UPNPMBeanService>();
		services.addAll(rootDevice.getUPNPMBeanServices());
		// 2 messages for each embedded devices
		for (Iterator<UPNPMBeanDevice> i = rootDevice.getUPNPMBeanChildrens().iterator(); i.hasNext();) {
			UPNPMBeanDevice child = i.next();
			services.addAll(child.getUPNPMBeanServices());

			packet = new StringBuffer(basePacket.toString());
			if (ssdpAliveMsg) {
				packet.append("NT: uuid:").append(child.getUuid()).append("\r\n");
				packet.append("NTS: ssdp:alive\r\n");
			} else {
				packet.append("ST: uuid:").append(child.getUuid()).append("\r\n");
				packet.append("EXT:\r\n");
			}
			packet.append("USN: uuid:").append(child.getUuid()).append("\r\n\r\n");
			rtrVal.add(packet.toString());

			packet = new StringBuffer(basePacket.toString());
			if (ssdpAliveMsg) {
				packet.append("NT: ").append(child.getDeviceType()).append("\r\n");
				packet.append("NTS: ssdp:alive\r\n");
			} else {
				packet.append("ST: ").append(child.getDeviceType()).append("\r\n");
				packet.append("EXT:\r\n");
			}
			packet.append("USN: uuid:").append(child.getUuid()).append("::").append(child.getDeviceType()).append("\r\n\r\n");
			rtrVal.add(packet.toString());

		}

		for (Iterator<UPNPMBeanService> i = services.iterator(); i.hasNext();) {
			UPNPMBeanService srv = i.next();
			// 1 message for each service embedded service
			if (ssdpAliveMsg) {
				packet.append("NT: ").append(srv.getServiceType()).append("\r\n");
				packet.append("NTS: ssdp:alive\r\n");
			} else {
				packet.append("ST: ").append(srv.getServiceType()).append("\r\n");
				packet.append("EXT:\r\n");
			}
			packet.append("USN: uuid:").append(srv.getDeviceUUID()).append("::").append(srv.getServiceType()).append("\r\n\r\n");
			rtrVal.add(packet.toString());
		}
		return rtrVal;
	}

	private List<String> getByeByeReplyMessages(UPNPMBeanDevice rootDevice) {
		List<String> rtrVal = new ArrayList<String>();

		StringBuffer basePacket = new StringBuffer();
		StringBuffer packet = null;
		basePacket.append("NOTIFY * HTTP/1.1\r\n");
		basePacket.append("HOST: 239.255.255.250:1900\r\n");
		// 3 messages for the root device
		packet = new StringBuffer(basePacket.toString());
		packet.append("NT: uuid:").append(rootDevice.getUuid()).append("\r\n");
		packet.append("NTS: ssdp:byebye\r\n");
		packet.append("USN: uuid:").append(rootDevice.getUuid()).append("\r\n\r\n");
		rtrVal.add(packet.toString());

		packet = new StringBuffer(basePacket.toString());
		packet.append("NT: ").append(rootDevice.getDeviceType()).append("\r\n");
		packet.append("NTS: ssdp:byebye\r\n");
		packet.append("USN: uuid:").append(rootDevice.getUuid()).append("::").append(rootDevice.getDeviceType()).append("\r\n\r\n");
		rtrVal.add(packet.toString());

		packet = new StringBuffer(basePacket.toString());
		packet.append("NT: upnp:rootdevice\r\n");
		packet.append("NTS: ssdp:byebye\r\n");
		packet.append("USN: uuid:").append(rootDevice.getUuid()).append("::upnp:rootdevice\r\n\r\n");
		rtrVal.add(packet.toString());

		List<UPNPMBeanService> services = new ArrayList<UPNPMBeanService>();
		services.addAll(rootDevice.getUPNPMBeanServices());
		// 2 messages for each embedded devices
		for (Iterator<UPNPMBeanDevice> i = rootDevice.getUPNPMBeanChildrens().iterator(); i.hasNext();) {
			UPNPMBeanDevice child = i.next();
			services.addAll(child.getUPNPMBeanServices());

			packet = new StringBuffer(basePacket.toString());
			packet.append("NT: uuid:").append(child.getUuid()).append("\r\n");
			packet.append("NTS: ssdp:byebye\r\n");
			packet.append("USN: uuid:").append(child.getUuid()).append("\r\n\r\n");
			rtrVal.add(packet.toString());

			packet = new StringBuffer(basePacket.toString());
			packet.append("NT: ").append(child.getDeviceType()).append("\r\n");
			packet.append("NTS: ssdp:byebye\r\n");
			packet.append("USN: uuid:").append(child.getUuid()).append("::").append(child.getDeviceType()).append("\r\n\r\n");
			rtrVal.add(packet.toString());

		}
		// 1 messages for each service
		for (Iterator<UPNPMBeanService> i = services.iterator(); i.hasNext();) {
			UPNPMBeanService srv = i.next();

			packet = new StringBuffer(basePacket.toString());
			packet.append("NT: urn:").append(srv.getServiceType()).append("\r\n");
			packet.append("NTS: ssdp:byebye\r\n");
			packet.append("USN: uuid:").append(srv.getDeviceUUID()).append("::").append(srv.getServiceType()).append("\r\n\r\n");
			rtrVal.add(packet.toString());
		}
		return rtrVal;
	}

	public void run() {
		InetAddress group = null;
		try {
			group = InetAddress.getByName("239.255.255.250");
			skt = new java.net.MulticastSocket(1900);
			skt.setInterface(bindAddress.getAddress());
			skt.joinGroup(group);
		} catch (IOException ex) {
			log.error("Error during multicast socket creation, thread cannot start", ex);
			return;
		}
		isRunning = true;
		while (isRunning) {
			try {
				byte[] buffer = new byte[4096];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, bindAddress.getPort());
				skt.receive(packet);
				String received = new String(packet.getData(), 0, packet.getLength());
				if (log.isDebugEnabled())
					log.debug("Received message:\n" + received);
				HttpRequest req = new HttpRequest(received);
				if (req.getHttpCommand().equals("M-SEARCH")) {
					String man = req.getHTTPHeaderField("MAN");
					if (man.equals("\"ssdp:discover\"")) {
						String searchTarget = req.getHTTPHeaderField("ST");
						// TODO check ALL devices search target
						//if ( searchTarget.equals( Discovery.ALL_DEVICES ) ) {
						if (searchTarget.equals(Discovery.ROOT_DEVICES)) {
							java.net.MulticastSocket multi = new java.net.MulticastSocket();
							multi.setInterface(bindAddress.getAddress());
							for (Iterator<UPNPMBeanDevice> i = handledDevices.iterator(); i.hasNext();) {
								UPNPMBeanDevice dv = i.next();
								List<String> packets = getReplyMessages(dv, false, dv.getSSDPAliveDelay());
								for (int z = 0; z < packets.size(); z++) {
									String pack = packets.get(z);
									if (log.isDebugEnabled())
										log.debug("Sending http reply message on " + packet.getAddress() + ":" + packet.getPort() + " multicast address:\n" + pack.toString());
									byte[] pk = pack.getBytes();
									multi.setTimeToLive(dv.getSSDPTTL());
									multi.send(new DatagramPacket(pk, pk.length, packet.getAddress(), packet.getPort()));
								}
							}
							multi.close();
						} else {
							// TODO check a specific search target
						}
					}
				}
			} catch (IOException ex) {
				if (isRunning) {
					log.error("Error during multicast socket IO operations", ex);
				}
			}
		}
	}

	private class SSDPAliveBroadcastMessageSender implements Runnable {
		private Set<UPNPMBeanDevice> devices = new HashSet<UPNPMBeanDevice>();
		private final Map<String, Long> devicesLastBroadCast = new HashMap<String, Long>();

		private SSDPAliveBroadcastMessageSender(Set<UPNPMBeanDevice> upnpRootDevices) {
			this.devices = upnpRootDevices;
		}

		public void run() {
			isRunningSSDPDaemon = true;
			while (isRunningSSDPDaemon) {
				synchronized (devices) {
					for (Iterator<UPNPMBeanDevice> i = devices.iterator(); i.hasNext();) {
						UPNPMBeanDevice dv = i.next();
						String key = dv.getUuid();
						long deviceDelay = dv.getSSDPAliveDelay();
						Long lastCall = devicesLastBroadCast.get(key);
						if (lastCall == null) {
							lastCall = new Long(System.currentTimeMillis() + (deviceDelay * 60) + 1000);
							devicesLastBroadCast.put(key, lastCall);
						}
						if (lastCall.longValue() + (deviceDelay * 60) < System.currentTimeMillis()) {
							try {
								InetAddress group = InetAddress.getByName("239.255.255.250");
								java.net.MulticastSocket multi = new java.net.MulticastSocket(bindAddress.getPort());
								multi.setInterface(bindAddress.getAddress());
								multi.setTimeToLive(dv.getSSDPTTL());
								multi.joinGroup(group);
								List<String> packets = getReplyMessages(dv, true, dv.getSSDPAliveDelay());
								for (int z = 0; z < packets.size(); z++) {
									String pack = packets.get(z);
									if (log.isDebugEnabled())
										log.debug("Sending http message on " + group.getAddress() + ":1900 multicast address:\n" + pack.toString());
									byte[] pk = pack.getBytes();
									multi.send(new DatagramPacket(pk, pk.length, group, 1900));
								}
								multi.leaveGroup(group);
								multi.close();
								devicesLastBroadCast.put(key, new Long(System.currentTimeMillis()));
							} catch (IOException ex) {
								log.error("Error occured during SSDP alive broadcast message sending", ex);
							}
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}
}

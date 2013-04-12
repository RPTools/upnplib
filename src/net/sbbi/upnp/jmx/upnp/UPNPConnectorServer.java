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

package net.sbbi.upnp.jmx.upnp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;

import net.sbbi.upnp.jmx.UPNPDiscovery;
import net.sbbi.upnp.jmx.UPNPDiscoveryMBean;
import net.sbbi.upnp.jmx.UPNPMBeanDevice;
import net.sbbi.upnp.jmx.UPNPServiceMBean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX connector server, this connector can be used to expose all deployed MBeans on an MBeans Server as UPNP
 * devices</br> You can use an object implementing the {@link net.sbbi.upnp.jmx.upnp.UPNPMBeanBuilder} interface do
 * define which beans can be deployed as UPNP devices.</br> Look at the UPNP_MBEANS_BUILDER,
 * EXPOSE_UPNP_DEVICES_AS_MBEANS and EXPOSE_MBEANS_AS_UPNP_DEVICES vars for more info about connector specific settings.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPConnectorServer extends JMXConnectorServer implements NotificationListener {
	private final static Log log = LogFactory.getLog(UPNPConnectorServer.class);

	/**
	 * Environement key used to define the {@link net.sbbi.upnp.jmx.upnp.UPNPMBeanBuilder} used to select MBeans to
	 * deploy as UPNP devices Key value must be an object UPNPMBeanBuilder instance. When no implemntation is provided,
	 * the default {@link net.sbbi.upnp.jmx.upnp.UPNPMBeanBuilderImpl} will be use.
	 */
	public final static String UPNP_MBEANS_BUILDER = UPNPConnectorServer.class.getName() + ".upnpbeans.builder";

	/**
	 * Environement key do define if the connector can also expose as MBeans all UPNP devices services on the network.
	 * Key value must be Boolean.TRUE or Boolean.FALSE, default to Boolean.FALSE
	 */
	public final static String EXPOSE_UPNP_DEVICES_AS_MBEANS = UPNPConnectorServer.class.getName() + ".upnpdevices.as.mbeans";
	/**
	 * Integer key to define the discovery timeout (in ms) of UPNP devices on the network when the
	 * EXPOSE_UPNP_DEVICES_AS_MBEANS env key is set to Boolean.TRUE
	 */
	public final static String EXPOSE_UPNP_DEVICES_AS_MBEANS_TIMEOUT = UPNPConnectorServer.class.getName() + ".upnpdevices.as.mbeans.timeout";
	/**
	 * Environement key when EXPOSE_UPNP_DEVICES_AS_MBEANS is set to true, will define SSDP messages will be handled by
	 * the connector, if set to true new devices joining the network will be automatically exposed as MBeans Key value
	 * must be Boolean.TRUE or Boolean.FALSE, default to Boolean.FALSE
	 */
	public final static String HANDLE_SSDP_MESSAGES = UPNPConnectorServer.class.getName() + ".upnpdevices.as.mbeans.ssdp";
	/**
	 * Environnment key to define if the MBeans registered into the connector MBeans server can be exposed as UPNP
	 * devices. Key value must be Boolean.TRUE or Boolean.FALSE, default to Boolean.TRUE
	 */
	public final static String EXPOSE_MBEANS_AS_UPNP_DEVICES = UPNPConnectorServer.class.getName() + ".mbeans.as.upnpdevices";

	/**
	 * When set to true all MBeans registred into the MBeans server prior to the connector registration will be exposed
	 * as UPNP devices Default to Boolean.FALSE
	 */
	public final static String EXPOSE_EXISTING_MBEANS_AS_UPNP_DEVICES = UPNPConnectorServer.class.getName() + ".existing.mbeans.as.upnpdevices";

	private final JMXServiceURL serviceURL;
	private final Map<String, ?> env;
	private final InetSocketAddress sktAddress;
	private UPNPMBeanBuilder builder;
	private Boolean exposeUPNPAsMBeans;
	private Boolean exposeMBeansAsUPNP;
	private Boolean exposeExistingMBeansAsUPNP;
	private Boolean handleSSDPMessages;
	private final Object STOP_PROCESS = new Object();
	private final Map<String, UPNPMBeanDevice> registeredMBeans = Collections.synchronizedMap(new HashMap<String, UPNPMBeanDevice>());
	private ObjectName discoveryBeanName;

	public UPNPConnectorServer(MBeanServer server, JMXServiceURL serviceURL, Map<String, ?> env) throws IOException {
		super(server);
		this.serviceURL = serviceURL;
		this.env = env;
		// TODO implement an JMXConnector for security (such as basic http auth)? problems with the UPNP protocol itself
		sktAddress = new InetSocketAddress(InetAddress.getByName(serviceURL.getHost()), serviceURL.getPort());
		builder = (UPNPMBeanBuilder) env.get(UPNP_MBEANS_BUILDER);
		if (builder == null) {
			builder = new UPNPMBeanBuilderImpl();
		}
		exposeUPNPAsMBeans = (Boolean) env.get(EXPOSE_UPNP_DEVICES_AS_MBEANS);
		if (exposeUPNPAsMBeans == null)
			exposeUPNPAsMBeans = Boolean.FALSE;
		exposeMBeansAsUPNP = (Boolean) env.get(EXPOSE_MBEANS_AS_UPNP_DEVICES);
		if (exposeMBeansAsUPNP == null)
			exposeMBeansAsUPNP = Boolean.TRUE;
		handleSSDPMessages = (Boolean) env.get(HANDLE_SSDP_MESSAGES);
		if (handleSSDPMessages == null)
			handleSSDPMessages = Boolean.FALSE;
		exposeExistingMBeansAsUPNP = (Boolean) env.get(EXPOSE_EXISTING_MBEANS_AS_UPNP_DEVICES);
		if (exposeExistingMBeansAsUPNP == null)
			exposeExistingMBeansAsUPNP = Boolean.FALSE;

		if (!exposeMBeansAsUPNP.booleanValue() && !exposeUPNPAsMBeans.booleanValue() && !exposeExistingMBeansAsUPNP.booleanValue()) {
			throw new IOException("Useless UPNPConnectorServer since nothing will be deployed, unregister it");
		}
	}

	public JMXServiceURL getAddress() {
		return serviceURL;
	}

	public Map<String, Object> getAttributes() {
		return Collections.unmodifiableMap(env);
	}

	public boolean isActive() {
		return false;
	}

	public void start() throws IOException {
		MBeanServer server = getMBeanServer();
		if (exposeMBeansAsUPNP.booleanValue()) {
			try {
				ObjectName delegate = new ObjectName("JMImplementation:type=MBeanServerDelegate");
				NotificationEmitter emmiter = MBeanServerInvocationHandler.newProxyInstance(server, delegate, NotificationEmitter.class, false);
				// register for MBeans registration
				emmiter.addNotificationListener(this, null, this);
			} catch (Exception ex) {
				IOException ioEx = new IOException("UPNPConnector start error");
				ioEx.initCause(ex);
				throw ioEx;
			}
		}
		if (exposeUPNPAsMBeans.booleanValue()) {
			int timeout = 2500;
			if (env.containsKey(EXPOSE_UPNP_DEVICES_AS_MBEANS_TIMEOUT)) {
				timeout = ((Integer) env.get(EXPOSE_UPNP_DEVICES_AS_MBEANS_TIMEOUT)).intValue();
			}
			try {
				discoveryBeanName = new ObjectName("UPNPLib discovery:name=Discovery MBean_" + this.hashCode());
				UPNPDiscoveryMBean bean = new UPNPDiscovery(timeout, handleSSDPMessages.booleanValue(), true);
				server.registerMBean(bean, discoveryBeanName);
			} catch (Exception ex) {
				IOException ioEx = new IOException("Error occured during MBeans discovery");
				ioEx.initCause(ex);
				throw ioEx;
			}
		}
		if (exposeExistingMBeansAsUPNP.booleanValue()) {
			int c = 0;
			Set<ObjectName> objectInstances = super.getMBeanServer().queryNames(null, null);
			for (Iterator<ObjectName> i = objectInstances.iterator(); i.hasNext();) {
				ObjectName name = i.next();
				MBeanServerNotification not = new MBeanServerNotification(MBeanServerNotification.REGISTRATION_NOTIFICATION, this, c++, name);
				handleNotification(not, this);
			}
		}
	}

	public void stop() throws IOException {
		MBeanServer server = getMBeanServer();
		IOException error = null;
		if (exposeMBeansAsUPNP.booleanValue()) {
			try {
				ObjectName delegate = new ObjectName("JMImplementation:type=MBeanServerDelegate");
				NotificationEmitter emmiter = MBeanServerInvocationHandler.newProxyInstance(server, delegate, NotificationEmitter.class, false);
				emmiter.removeNotificationListener(this, null, this);
			} catch (Exception ex) {
				// MX4J throws an unexpected ListenerNotFoundException with jre 1.5.06.. works nice with sun JMX impl
				if (!(ex instanceof ListenerNotFoundException)) {
					IOException ioEx = new IOException("UPNPConnector stop error");
					ioEx.initCause(ex);
					error = ioEx;
				}
			}
			synchronized (STOP_PROCESS) {
				// now stop all the remaining Devices
				for (Iterator<UPNPMBeanDevice> i = registeredMBeans.values().iterator(); i.hasNext();) {
					UPNPMBeanDevice dv = i.next();
					try {
						dv.stop();
					} catch (IOException ex) {
						log.error("Error during UPNPMBean device stop", ex);
					}
				}
				registeredMBeans.clear();
			}
		}
		if (exposeUPNPAsMBeans.booleanValue()) {
			try {
				server.unregisterMBean(discoveryBeanName);
			} catch (Exception ex) {
				IOException ioEx = new IOException("Error occured during MBeans discovery");
				ioEx.initCause(ex);
				throw ioEx;
			}
		}
		if (error != null) {
			throw error;
		}
	}

	public void handleNotification(Notification notification, Object handBack) {

		if (notification.getType().equals(MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
			MBeanServerNotification regNot = (MBeanServerNotification) notification;
			MBeanServer srv = getMBeanServer();
			ObjectName name = regNot.getMBeanName();
			try {
				ObjectInstance objIn = srv.getObjectInstance(name);
				String className = objIn.getClassName();
				// do not expose as UPN, UPNP devices exposed as MBeans ( class UPNPServiceMBean purpose )
				if (className.equals(UPNPServiceMBean.class.getName()))
					return;
				if (builder.select(name, className)) {
					MBeanInfo info = srv.getMBeanInfo(name);
					UPNPMBeanDevice dv = builder.buildUPNPMBean(getMBeanServer(), objIn, info);
					if (dv != null) {
						dv.setBindAddress(sktAddress);
						dv.start();
						registeredMBeans.put(name.toString(), dv);
					}
				}
			} catch (Exception ex) {
				log.error("Error during UPNP Mbean device " + name.toString() + " creation", ex);
			}
		} else if (notification.getType().equals(MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
			MBeanServerNotification regNot = (MBeanServerNotification) notification;
			String beanName = regNot.getMBeanName().toString();
			synchronized (STOP_PROCESS) {
				UPNPMBeanDevice dv = registeredMBeans.get(beanName);
				if (dv != null) {
					try {
						dv.stop();
					} catch (Exception ex) {
						log.error("Error during UPNPMBean device stop", ex);
					}
					registeredMBeans.remove(beanName);
				}
			}
		}
	}
}

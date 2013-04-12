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

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import mx4j.log.CommonsLogger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JMX manager for UPNP devices, entry point for the MX4j HTTP admin console
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class JMXManager {
	private final static JMXManager instance = new JMXManager();
	private final static Log log = LogFactory.getLog(JMXManager.class);
	private ObjectName discBeanName = null;
	private MBeanServer server;

	public final static JMXManager getInstance() {
		return instance;
	}

	public final static JMXManager getNewInstance(MBeanServer server) {
		JMXManager manager = new JMXManager();
		manager.setMBeanserver(server);
		return manager;
	}

	private void setMBeanserver(MBeanServer server) {
		this.server = server;
	}

	public void startup(int discoveryTimeout) throws Exception {
		discBeanName = new ObjectName("UPNPLib discovery:name=Discovery MBean_" + this.hashCode());
		UPNPDiscoveryMBean bean = new UPNPDiscovery(discoveryTimeout, true, true);
		server.registerMBean(bean, discBeanName);
	}

	public void shutdown() {
		try {
			server.unregisterMBean(discBeanName);
		} catch (Exception ex) {
			log.error("Error occured during UPNPDiscoveryMBean unregistration", ex);
		}
	}

	private MBeanServer initMBeanServer(MBeanServerConfig conf) throws Exception {
		mx4j.log.Log.redirectTo(new CommonsLogger());
		//  make sure that MX4j Server builder is used
		String oldSysProp = System.getProperty("javax.management.builder.initial");
		System.setProperty("javax.management.builder.initial", "mx4j.server.MX4JMBeanServerBuilder");
		MBeanServer server = MBeanServerFactory.createMBeanServer("UPNPLib");
		if (oldSysProp != null) {
			System.setProperty("javax.management.builder.initial", oldSysProp);
		}
		ObjectName serverName = new ObjectName("Http:name=HttpAdaptor");
		server.createMBean("mx4j.tools.adaptor.http.HttpAdaptor", serverName, null);
		// set attributes
		server.setAttribute(serverName, new Attribute("Port", new Integer(conf.adapterAdapterPort)));

		Boolean allowWanBool = new Boolean(conf.allowWan);
		if (allowWanBool.booleanValue()) {
			server.setAttribute(serverName, new Attribute("Host", "0.0.0.0"));
		} else {
			server.setAttribute(serverName, new Attribute("Host", "localhost"));
		}

		ObjectName processorName = new ObjectName("Http:name=XSLTProcessor");
		server.createMBean("mx4j.tools.adaptor.http.XSLTProcessor", processorName, null);
		server.setAttribute(processorName, new Attribute("LocaleString", conf.locale));

		server.setAttribute(processorName, new Attribute("UseCache", Boolean.FALSE));

		server.setAttribute(processorName, new Attribute("PathInJar", "net/sbbi/jmx/xsl"));

		server.setAttribute(serverName, new Attribute("ProcessorName", processorName));
		// add user names
		server.invoke(serverName, "addAuthorization", new Object[] { conf.adapterUserName, conf.adapterPassword }, new String[] { "java.lang.String", "java.lang.String" });
		// use basic authentication
		server.setAttribute(serverName, new Attribute("AuthenticationMethod", "basic"));
		// starts the server
		server.invoke(serverName, "start", null, null);

		return server;
	}

	public final static void main(String args[]) {
		if (args.length != 6) {
			log.info("Usage : JMXManager <AdapterPort> <UserName> <Password> <AllowWan> <Locale> <discoveryTimeout>");
			System.exit(0);
		}

		try {
			JMXManager manager = JMXManager.getInstance();
			MBeanServerConfig conf = new MBeanServerConfig(args);
			manager.setMBeanserver(manager.initMBeanServer(conf));
			manager.startup(conf.discoveryTimeout);
		} catch (Exception ex) {
			log.error("Error during startup", ex);
		}
	}

	private final static class MBeanServerConfig {
		private final String adapterAdapterPort;
		private final String adapterUserName;
		private final String adapterPassword;
		private final String allowWan;
		private final String locale;
		private final int discoveryTimeout;

		private MBeanServerConfig(String args[]) {
			adapterAdapterPort = args[0];
			adapterUserName = args[1];
			adapterPassword = args[2];
			allowWan = args[3];
			locale = args[4];
			discoveryTimeout = Integer.parseInt(args[5]);
		}
	}
}

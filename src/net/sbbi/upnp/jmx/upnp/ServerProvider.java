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
import java.net.MalformedURLException;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXServiceURL;

/**
 * JMX connector to expose as UPNP devices, all (or a set) MBeans registered into the server The JMX service URL looks
 * like this : service:jmx:upnp://localhost:8080 Only the host and port settings can be changed. The path in the service
 * url is currently unusable. look at {@link net.sbbi.upnp.jmx.upnp.UPNPConnectorServer} for more inormations
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ServerProvider implements JMXConnectorServerProvider {
	public final static String UPNP_PROTOCOL = "upnp";

	public JMXConnectorServer newJMXConnectorServer(JMXServiceURL serviceURL, Map<String, ?> env, MBeanServer server) throws IOException {
		String protocol = serviceURL.getProtocol();
		String path = serviceURL.getURLPath();
		if (!UPNP_PROTOCOL.equals(protocol))
			throw new MalformedURLException("Wrong protocol " + protocol + " for provider " + this.getClass().getName());
		// TODO support path setting
		if (path != null && path.trim().length() > 0)
			throw new MalformedURLException("provider " + this.getClass().getName() + " does not support path " + path);
		return new UPNPConnectorServer(server, serviceURL, env);
	}
}

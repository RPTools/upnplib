/*
 * This software copyright by various authors including the RPTools.net development team, and licensed under the LGPL
 * Version 3 or, at your option, any later version.
 * 
 * Portions of this software were originally covered under the Apache Software License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.sbbi.upnp.samples;

/*
 * import java.util.HashMap; import java.util.Map;
 * 
 * import javax.management.MBeanServer; import javax.management.ObjectName; import
 * javax.management.remote.JMXConnectorServer; import javax.management.remote.JMXConnectorServerFactory; import
 * javax.management.remote.JMXServiceURL;
 * 
 * import net.sbbi.upnp.jmx.UPNPMBeanDevice; import net.sbbi.upnp.jmx.upnp.UPNPConnectorServer;
 * 
 * import java.lang.management.ClassLoadingMXBean; import java.lang.management.ManagementFactory; import
 * java.lang.management.MemoryMXBean; import java.lang.management.RuntimeMXBean; import
 * java.lang.management.ThreadMXBean;
 */

/**
 * Sample class to expose JRE 1.5 JVM MBeans as UPNP devices
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class JDK5ManagementAsUpnp {

	public static void main(String[] args) {

		/*
		 * Runnable runnable = new Runnable() {
		 * 
		 * public void run() { UPNPMBeanDevice jdk5Device = new UPNPMBeanDevice( "JDK5Management", 1, "SBBI",
		 * "JDK 5 UPNP JMX Interface", "UPNPLIB", null ); UPNPMBeanDevice jdk5Device2 = new UPNPMBeanDevice(
		 * "JDK5Management", 1, "SBBI", "JDK 5 UPNP JMX Classloader Interface", "UPNPLIB", "BEAN2" );
		 * 
		 * try { MBeanServer defaultServer = ManagementFactory.getPlatformMBeanServer();
		 * 
		 * JMXServiceURL url = new JMXServiceURL( "service:jmx:upnp://192.168.0.3:8080" ); Map env = new HashMap();
		 * env.put( JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, "net.sbbi.upnp.jmx" ); env.put(
		 * UPNPConnectorServer.EXPOSE_MBEANS_AS_UPNP_DEVICES, Boolean.TRUE ); env.put(
		 * UPNPConnectorServer.EXPOSE_EXISTING_MBEANS_AS_UPNP_DEVICES, Boolean.TRUE );
		 * 
		 * JMXConnectorServer connectorServer = JMXConnectorServerFactory.newJMXConnectorServer( url, env, null );
		 * String upnpConnectorBeanName = "Connectors:protocol=upnp,host=192.168.0.3,port=8080"; ObjectName
		 * cntorServerName = ObjectName.getInstance( upnpConnectorBeanName ); defaultServer.registerMBean(
		 * connectorServer, cntorServerName ); connectorServer.start();
		 * 
		 * RuntimeMXBean mbean = ManagementFactory.getRuntimeMXBean(); ObjectName name = new ObjectName(
		 * ManagementFactory.RUNTIME_MXBEAN_NAME ); jdk5Device.addService( mbean, name, defaultServer, "RUNTIME_MXBEAN",
		 * "RUNTIME_MXBEAN_TYPE", 1 ); ThreadMXBean mbeanThread = ManagementFactory.getThreadMXBean(); name = new
		 * ObjectName( ManagementFactory.THREAD_MXBEAN_NAME ); jdk5Device.addService( mbeanThread, name, defaultServer,
		 * "THREAD_MXBEAN_NAME", "THREAD_MXBEAN_NAME_TYPE", 1 );
		 * 
		 * MemoryMXBean mbeanMem = ManagementFactory.getMemoryMXBean(); name = new ObjectName(
		 * ManagementFactory.MEMORY_MXBEAN_NAME ); jdk5Device.addService( mbeanMem, name, defaultServer,
		 * "MEMORY_MXBEAN_NAME", "MEMORY_MXBEAN_NAME_TYPE", 1 );
		 * 
		 * ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean(); name = new ObjectName(
		 * ManagementFactory.CLASS_LOADING_MXBEAN_NAME ); jdk5Device2.addService( clBean, name, defaultServer,
		 * "CLASS_LOADING_MXBEAN_NAME", "CLASS_LOADING_MXBEAN_NAME_TYPE", 1 );
		 * 
		 * jdk5Device.addChildMBean( jdk5Device2 ); System.out.println( "devices created" ); jdk5Device.start();
		 * System.out.println( "devices started" ); } catch ( Exception ex ) { ex.printStackTrace(); return; } while (
		 * true ) { try { Thread.sleep( 1000 ); } catch ( InterruptedException intEx ) {
		 * Thread.currentThread().interrupt(); break; } } try { jdk5Device.stop(); System.out.println( "devices stopped"
		 * ); } catch ( Exception ex ) { ex.printStackTrace(); } } };
		 * 
		 * Thread thrd = new Thread( runnable ); thrd.setDaemon( false ); thrd.start();
		 */

	}
}

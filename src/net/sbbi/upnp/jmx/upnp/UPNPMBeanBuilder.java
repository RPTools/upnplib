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
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import net.sbbi.upnp.jmx.UPNPMBeanDevice;

/**
 * Interface to define and build which MBeans can be deployed as UPNP devices
 * by an {@link net.sbbi.upnp.jmx.upnp.UPNPConnectorServer}
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public interface UPNPMBeanBuilder {

  /**
   * Select if the given MBean represented by it's object name, implemented by class className
   * can be exposed as an UPNP Device
   * @param objectName the MBean object name
   * @param className the MBean class name
   * @return true if the MBean can be exposed false otherwise..
   */
  public boolean select( ObjectName objectName, String className );
   
  /**
   * Build the UPNP MBean device, the method is NOT forced to return an UPNPMBeanDevice object.
   * The method must NOT start or bind to any network interface the UPNPMBeanDevice returned object
   * @param server the Mbean server, never null
   * @param objectInstance the MBean object instance, never null
   * @param info the MBean Object Info, never null
   * @return an instance (or null) of an UPNPMBeanDevice object.
   *         The implementation can deliver an UPNPMBeanDevice object containing multiple MBeans 
   *         provided (or not) by previous buildUPNPMBean methods call.
   * @throws IOException if some errors occurs during object creation
   */
  public UPNPMBeanDevice buildUPNPMBean( MBeanServer server, ObjectInstance objectInstance, 
                                         MBeanInfo info ) throws IOException;

}

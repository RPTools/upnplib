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

import java.net.URL;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * Discovery notification sent when a new set of UPNPServiceMBean for a given discovered
 * UPNP device is registered within the server. The same notification is also sent when a device is leaving the network.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPDiscoveryNotification extends Notification {
  public final static long serialVersionUID = 1;

  private String usn, udn, nt;
  private URL location;
  private ObjectName[] UPNPServiceMBeans;
  
  public UPNPDiscoveryNotification( String type, Object source, long sequenceNumber, long timeStamp ) {
    super( type, source, sequenceNumber, timeStamp );
  }

  /**
   * The Device descriptor location only provided when an ssdp alive notification is recieved
   * @return the device descriptor location
   */
  public URL getLocation() {
    return location;
  }

  protected void setLocation( URL location ) {
    this.location = location;
  }

  /**
   * The device type
   * @return the device type
   */
  public String getNt() {
    return nt;
  }

  protected void setNt( String nt ) {
    this.nt = nt;
  }

  /**
   * The device Identifier
   * @return the device ID
   */
  public String getUdn() {
    return udn;
  }

  protected void setUdn( String udn ) {
    this.udn = udn;
  }

  /**
   * The device id + ":" + type
   * @return the device USN
   */
  public String getUsn() {
    return usn;
  }

  protected void setUsn( String usn ) {
    this.usn = usn;
  }

  /**
   * The registered UPNPServiceMBeans object names bound to this device joining or leaving the network
   * @return UPNPServiceMBeans object names array
   */
  public ObjectName[] getUPNPServiceMBeans() {
    return UPNPServiceMBeans;
  }

  protected void setUPNPServiceMBeans( ObjectName[] serviceMBeans ) {
    UPNPServiceMBeans = serviceMBeans;
  }
  
  
  

}

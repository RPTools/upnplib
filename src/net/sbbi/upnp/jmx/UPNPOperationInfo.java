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

import java.lang.reflect.Method;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * ALPHA stage an unusable
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPOperationInfo extends MBeanOperationInfo {
  public final static long serialVersionUID = 1;

  public UPNPOperationInfo( String name, Method method ) {
    super( name, method );
  }
  
  public UPNPOperationInfo( String name, String param2, MBeanParameterInfo[] info, String arg3, int arg4 ) {
    super( name, param2, info, arg3, arg4 );
  }
  
  public void addOutputArgumentType( UPNPAttributeInfo attribute ) {
  }
}

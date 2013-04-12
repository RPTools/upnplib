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

import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;

/**
 * ALPHA stage an unusable
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPAttributeInfo extends MBeanAttributeInfo {
  public final static long serialVersionUID = 1;

  public UPNPAttributeInfo( String name1, String name2, Method method1, Method method2 ) throws IntrospectionException {
    super( name1, name2, method1, method2 );
  }
  
  public UPNPAttributeInfo( String name1, String name2, String name3, boolean boolean1, boolean boolean2, boolean boolean3 ) {
    super( name1, name2, name3, boolean1, boolean2, boolean3 );
  }

  public void setDefaultValue( String value ) {
    
  }
  
  public void addAllowedValueList( String value ) {
    
  }
  
  public void setUPNPDataType( String upnpDataType ) {
    
  }
  
  public void setAllowedValueRange( String minimum, String maximum, String step ) {
    
  }
  
}

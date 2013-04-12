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

package net.sbbi.upnp.services;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Set;

import net.sbbi.upnp.messages.StateVariableMessage;
import net.sbbi.upnp.messages.UPNPMessageFactory;
import net.sbbi.upnp.messages.UPNPResponseException;

/**
 * Class to contain a service state variable definition
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ServiceStateVariable implements ServiceStateVariableTypes {
	private StateVariableMessage stateVarMsg = null;

	protected String name;
	protected boolean sendEvents;
	protected String dataType;
	protected String defaultValue;

	protected String minimumRangeValue;
	protected String maximumRangeValue;
	protected String stepRangeValue;
	protected Set<String> allowedvalues;
	protected UPNPService parent;

	protected ServiceStateVariable() {
	}

	/**
	 * Call to the UPNP device to retreive the state variable actual value
	 * 
	 * @return the state variable actual value on the device, should be never null, an empty string could be returned by
	 *         the device
	 * @throws UPNPResponseException
	 *             if the device throws an exception during query
	 * @throws IOException
	 *             if some IO error with device occurs during query
	 */
	public String getValue() throws UPNPResponseException, IOException {
		if (stateVarMsg == null) {
			synchronized (this) {
				if (stateVarMsg == null) {
					UPNPMessageFactory factory = UPNPMessageFactory.getNewInstance(parent);
					stateVarMsg = factory.getStateVariableMessage(name);
				}
			}
		}
		return stateVarMsg.service().getStateVariableValue();
	}

	/**
	 * State variable name
	 * 
	 * @return the state variable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The parent UPNPService Object
	 * 
	 * @return the parent object instance
	 */
	public UPNPService getParent() {
		return parent;
	}

	/**
	 * Boolean to indicate if the variable is sending events when value of the var is changing. The events can be
	 * subscribed via the {@link net.sbbi.upnp.ServicesEventing} class
	 * 
	 * @return true if sending events
	 */
	public boolean isSendEvents() {
		return sendEvents;
	}

	/**
	 * The default value of the state variable
	 * 
	 * @return the default value representation as an string
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * The variable UPNP data type
	 * 
	 * @return the data type
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * The variable JAVA data type (using an UPNP->Java mapping)
	 * 
	 * @return the class mapped
	 */
	public Class<?> getDataTypeAsClass() {
		return getDataTypeClassMapping(dataType);
	}

	/**
	 * A set of allowed values (String objects) for the variable
	 * 
	 * @return the allowed values or null if none
	 */
	public Set<String> getAllowedvalues() {
		return allowedvalues;
	}

	/**
	 * The minimum value as a string
	 * 
	 * @return the minimum value or null if no restriction
	 */
	public String getMinimumRangeValue() {
		return minimumRangeValue;
	}

	/**
	 * The maximum value as a string
	 * 
	 * @return the maximum value or null if no restriction
	 */
	public String getMaximumRangeValue() {
		return maximumRangeValue;
	}

	/**
	 * The value step range as a string
	 * 
	 * @return the value step raqnge or null if no restriction
	 */
	public String getStepRangeValue() {
		return stepRangeValue;
	}

	public static Class<?> getDataTypeClassMapping(String dataType) {
		int hash = dataType.hashCode();
		if (hash == UI1_INT)
			return Short.class;
		else if (hash == UI2_INT)
			return Integer.class;
		else if (hash == UI4_INT)
			return Long.class;
		else if (hash == I1_INT)
			return Byte.class;
		else if (hash == I2_INT)
			return Short.class;
		else if (hash == I4_INT)
			return Integer.class;
		else if (hash == INT_INT)
			return Integer.class;
		else if (hash == R4_INT)
			return Float.class;
		else if (hash == R8_INT)
			return Double.class;
		else if (hash == NUMBER_INT)
			return Double.class;
		else if (hash == FIXED_14_4_INT)
			return Double.class;
		else if (hash == FLOAT_INT)
			return Float.class;
		else if (hash == CHAR_INT)
			return Character.class;
		else if (hash == STRING_INT)
			return String.class;
		else if (hash == DATE_INT)
			return Date.class;
		else if (hash == DATETIME_INT)
			return Date.class;
		else if (hash == DATETIME_TZ_INT)
			return Date.class;
		else if (hash == TIME_INT)
			return Date.class;
		else if (hash == TIME_TZ_INT)
			return Date.class;
		else if (hash == BOOLEAN_INT)
			return Boolean.class;
		else if (hash == BIN_BASE64_INT)
			return String.class;
		else if (hash == BIN_HEX_INT)
			return String.class;
		else if (hash == URI_INT)
			return URI.class;
		else if (hash == UUID_INT)
			return String.class;
		return null;
	}

	public static String getUPNPDataTypeMapping(String className) {
		if (className.equals(Short.class.getName()) || className.equals("short"))
			return I2;
		else if (className.equals(Byte.class.getName()) || className.equals("byte"))
			return I1;
		else if (className.equals(Integer.class.getName()) || className.equals("int"))
			return INT;
		else if (className.equals(Long.class.getName()) || className.equals("long"))
			return UI4;
		else if (className.equals(Float.class.getName()) || className.equals("float"))
			return FLOAT;
		else if (className.equals(Double.class.getName()) || className.equals("double"))
			return NUMBER;
		else if (className.equals(Character.class.getName()) || className.equals("char"))
			return CHAR;
		else if (className.equals(String.class.getName()) || className.equals("string"))
			return STRING;
		else if (className.equals(Date.class.getName()))
			return DATETIME;
		else if (className.equals(Boolean.class.getName()) || className.equals("boolean"))
			return BOOLEAN;
		else if (className.equals(URI.class.getName()))
			return URI;
		return null;
	}

	public static Object UPNPToJavaObject(String dataType, String value) throws Throwable {
		if (value == null)
			throw new Exception("null value");
		if (dataType == null)
			throw new Exception("null dataType");
		int hash = dataType.hashCode();
		if (hash == UI1_INT)
			return new Short(value);
		else if (hash == UI2_INT)
			return new Integer(value);
		else if (hash == UI4_INT)
			return new Long(value);
		else if (hash == I1_INT)
			return new Byte(value);
		else if (hash == I2_INT)
			return new Short(value);
		else if (hash == I4_INT)
			return new Integer(value);
		else if (hash == INT_INT)
			return new Integer(value);
		else if (hash == R4_INT)
			return new Float(value);
		else if (hash == R8_INT)
			return new Double(value);
		else if (hash == NUMBER_INT)
			return new Double(value);
		else if (hash == FIXED_14_4_INT)
			return new Double(value);
		else if (hash == FLOAT_INT)
			return new Float(value);
		else if (hash == CHAR_INT)
			return new Character(value.charAt(0));
		else if (hash == STRING_INT)
			return value;
		else if (hash == DATE_INT)
			return ISO8601Date.parse(value);
		else if (hash == DATETIME_INT)
			return ISO8601Date.parse(value);
		else if (hash == DATETIME_TZ_INT)
			return ISO8601Date.parse(value);
		else if (hash == TIME_INT)
			return ISO8601Date.parse(value);
		else if (hash == TIME_TZ_INT)
			return ISO8601Date.parse(value);
		else if (hash == BOOLEAN_INT) {
			if (value.equals("1") || value.equalsIgnoreCase("yes") || value.equals("true")) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		else if (hash == BIN_BASE64_INT)
			return value;
		else if (hash == BIN_HEX_INT)
			return value;
		else if (hash == URI_INT)
			return new URI(value);
		else if (hash == UUID_INT)
			return value;
		throw new Exception("Unhandled data type " + dataType);
	}
}

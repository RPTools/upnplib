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

package net.sbbi.upnp.messages;

import net.sbbi.upnp.services.ServiceStateVariable;

/**
 * This class contains data returned by a state variable query response
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class StateVariableResponse {
	protected ServiceStateVariable stateVar;
	protected String stateVariableValue;

	protected StateVariableResponse() {
	}

	public ServiceStateVariable getStateVar() {
		return stateVar;
	}

	public String getStateVariableValue() {
		return stateVariableValue;
	}
}

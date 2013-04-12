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

import net.sbbi.upnp.services.ServiceAction;
import net.sbbi.upnp.services.ServiceStateVariable;
import net.sbbi.upnp.services.UPNPService;

/**
 * Factory to create UPNP messages to access and communicate with a given UPNPDevice service capabilities
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPMessageFactory {
	private final UPNPService service;

	/**
	 * Private constructor since this is a factory.
	 * 
	 * @param service
	 *            the UPNPService that will be used to generate messages by thid factory
	 */
	private UPNPMessageFactory(UPNPService service) {
		this.service = service;
	}

	/**
	 * Generate a new factory instance for a given device service definition object
	 * 
	 * @param service
	 *            the UPNP service definition object for messages creation
	 * @return a new message factory
	 */
	public static UPNPMessageFactory getNewInstance(UPNPService service) {
		return new UPNPMessageFactory(service);
	}

	/**
	 * Creation of a new ActionMessage to communicate with the UPNP device
	 * 
	 * @param serviceActionName
	 *            the name of a service action, this name is case sensitive and matches exactly the name provided by the
	 *            UPNP device in the XML definition file
	 * @return a ActionMessage object or null if the action is unknown for this service messages factory
	 */
	public ActionMessage getMessage(String serviceActionName) {
		ServiceAction serviceAction = service.getUPNPServiceAction(serviceActionName);
		if (serviceAction != null) {
			return new ActionMessage(service, serviceAction);
		}
		return null;
	}

	/**
	 * Creation of a new StateVariableMessage to communicate with the UPNP device, for a service state variable query
	 * 
	 * @param serviceStateVariable
	 *            the name of a service state variable, this name is case sensitive and matches exactly the name
	 *            provided by the UPNP device in the XML definition file
	 * @return a StateVariableMessage object or null if the state variable is unknown for this service mesages factory
	 */
	public StateVariableMessage getStateVariableMessage(String serviceStateVariable) {
		ServiceStateVariable stateVar = service.getUPNPServiceStateVariable(serviceStateVariable);
		if (stateVar != null) {
			return new StateVariableMessage(service, stateVar);
		}
		return null;
	}
}

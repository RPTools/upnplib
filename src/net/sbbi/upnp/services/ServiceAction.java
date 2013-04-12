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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An object to represent a service action proposed by an UPNP service
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class ServiceAction {
	protected String name;
	protected UPNPService parent;
	private List<ServiceActionArgument> orderedActionArguments;
	private List<ServiceActionArgument> orderedInputActionArguments;
	private List<ServiceActionArgument> orderedOutputActionArguments;
	private List<String> orderedInputActionArgumentsNames;
	private List<String> orderedOutputActionArgumentsNames;

	protected ServiceAction() {
	}

	public UPNPService getParent() {
		return parent;
	}

	/**
	 * The action in and out arguments ServiceActionArgument objects list
	 * 
	 * @return the list with ServiceActionArgument objects or null if the action has no params
	 */
	public List<ServiceActionArgument> getActionArguments() {
		return orderedActionArguments;
	}

	/**
	 * Look for an ServiceActionArgument for a given name
	 * 
	 * @param argumentName
	 *            the argument name
	 * @return the argument or null if not found or not available
	 */
	public ServiceActionArgument getActionArgument(String argumentName) {
		if (orderedActionArguments == null)
			return null;
		for (Iterator<ServiceActionArgument> i = orderedActionArguments.iterator(); i.hasNext();) {
			ServiceActionArgument arg = i.next();
			if (arg.getName().equals(argumentName))
				return arg;
		}
		return null;
	}

	protected void setActionArguments(List<ServiceActionArgument> orderedActionArguments) {
		this.orderedActionArguments = orderedActionArguments;
		orderedInputActionArguments = getListForActionArgument(orderedActionArguments, ServiceActionArgument.DIRECTION_IN);
		orderedOutputActionArguments = getListForActionArgument(orderedActionArguments, ServiceActionArgument.DIRECTION_OUT);
		orderedInputActionArgumentsNames = getListForActionArgumentNames(orderedActionArguments, ServiceActionArgument.DIRECTION_IN);
		orderedOutputActionArgumentsNames = getListForActionArgumentNames(orderedActionArguments, ServiceActionArgument.DIRECTION_OUT);
	}

	/**
	 * Return a list containing input ( when a response is sent ) arguments objects
	 * 
	 * @return a list containing input arguments ServiceActionArgument objects or null when nothing is needed for such
	 *         operation
	 */
	public List<ServiceActionArgument> getInputActionArguments() {
		return orderedInputActionArguments;
	}

	/**
	 * Look for an input ServiceActionArgument for a given name
	 * 
	 * @param argumentName
	 *            the input argument name
	 * @return the argument or null if not found or not available
	 */
	public ServiceActionArgument getInputActionArgument(String argumentName) {
		if (orderedInputActionArguments == null)
			return null;
		for (Iterator<ServiceActionArgument> i = orderedInputActionArguments.iterator(); i.hasNext();) {
			ServiceActionArgument arg = i.next();
			if (arg.getName().equals(argumentName))
				return arg;
		}
		return null;
	}

	/**
	 * Return a list containing output ( when a response is received ) arguments objects
	 * 
	 * @return a list containing output arguments ServiceActionArgument objects or null when nothing returned for such
	 *         operation
	 */
	public List<ServiceActionArgument> getOutputActionArguments() {
		return orderedOutputActionArguments;
	}

	/**
	 * Look for an output ServiceActionArgument for a given name
	 * 
	 * @param argumentName
	 *            the input argument name
	 * @return the argument or null if not found or not available
	 */
	public ServiceActionArgument getOutputActionArgument(String argumentName) {
		if (orderedOutputActionArguments == null)
			return null;
		for (Iterator<ServiceActionArgument> i = orderedOutputActionArguments.iterator(); i.hasNext();) {
			ServiceActionArgument arg = i.next();
			if (arg.getName().equals(argumentName))
				return arg;
		}
		return null;
	}

	/**
	 * Return a list containing input ( when a response is sent ) arguments names
	 * 
	 * @return a list containing input arguments names as Strings or null when nothing is needed for such operation
	 */
	public List<String> getInputActionArgumentsNames() {
		return orderedInputActionArgumentsNames;
	}

	/**
	 * Return a list containing output ( when a response is received ) arguments names
	 * 
	 * @return a list containing output arguments names as Strings or null when nothing returned for such operation
	 */
	public List<String> getOutputActionArgumentsNames() {
		return orderedOutputActionArgumentsNames;
	}

	/**
	 * The action name
	 * 
	 * @return The action name
	 */
	public String getName() {
		return name;
	}

	private List<ServiceActionArgument> getListForActionArgument(List<ServiceActionArgument> args, String direction) {
		if (args == null)
			return null;
		List<ServiceActionArgument> rtrVal = new ArrayList<ServiceActionArgument>();
		for (Iterator<ServiceActionArgument> itr = args.iterator(); itr.hasNext();) {
			ServiceActionArgument actArg = itr.next();
			if (actArg.getDirection() == direction) {
				rtrVal.add(actArg);
			}
		}
		if (rtrVal.isEmpty())
			rtrVal = null;
		return rtrVal;
	}

	private List<String> getListForActionArgumentNames(List<ServiceActionArgument> args, String direction) {
		if (args == null)
			return null;
		List<String> rtrVal = new ArrayList<String>();
		for (Iterator<ServiceActionArgument> itr = args.iterator(); itr.hasNext();) {
			ServiceActionArgument actArg = itr.next();
			if (actArg.getDirection() == direction) {
				rtrVal.add(actArg.getName());
			}
		}
		if (rtrVal.isEmpty())
			rtrVal = null;
		return rtrVal;
	}
}

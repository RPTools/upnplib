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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sbbi.upnp.services.ServiceActionArgument;

/**
 * An action respons container Object
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class ActionResponse {
	private final Map<String, ServiceActionArgument> outArguments = new HashMap<String, ServiceActionArgument>();
	private final Map<String, String> outArgumentsVals = new HashMap<String, String>();

	protected ActionResponse() {
	}

	public ServiceActionArgument getOutActionArgument(String actionArgumentName) {
		return outArguments.get(actionArgumentName);
	}

	public String getOutActionArgumentValue(String actionArgumentName) {
		return outArgumentsVals.get(actionArgumentName);
	}

	public Set<String> getOutActionArgumentNames() {
		return outArguments.keySet();
	}

	/**
	 * Adds a result to the response, adding an existing result ServiceActionArgument will override the
	 * ServiceActionArgument value
	 * 
	 * @param arg
	 *            the service action argument
	 * @param value
	 *            the arg value
	 */
	protected void addResult(ServiceActionArgument arg, String value) {
		outArguments.put(arg.getName(), arg);
		outArgumentsVals.put(arg.getName(), value);
	}

	@Override
	public String toString() {
		StringBuffer rtrVal = new StringBuffer();
		for (Iterator<String> i = outArguments.keySet().iterator(); i.hasNext();) {
			String name = i.next();
			String value = outArgumentsVals.get(name);
			rtrVal.append(name).append("=").append(value);
			if (i.hasNext())
				rtrVal.append("\n");
		}
		return rtrVal.toString();
	}
}

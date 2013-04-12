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

/**
 * An argument for a service action
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class ServiceActionArgument {
	public final static String DIRECTION_IN = "in";
	public final static String DIRECTION_OUT = "out";

	protected ServiceStateVariable relatedStateVariable;
	protected String name;
	protected String direction;

	/**
	 * The related service state variable for this ServiceActionArgument
	 * 
	 * @return The related service state variable for this ServiceActionArgument
	 */
	public ServiceStateVariable getRelatedStateVariable() {
		return relatedStateVariable;
	}

	/**
	 * The argument name
	 * 
	 * @return the argument name
	 */
	public String getName() {
		return name;
	}

	/**
	 * The argument direction
	 * 
	 * @return the argument direction (in|out)
	 */
	public String getDirection() {
		return direction;
	}
}

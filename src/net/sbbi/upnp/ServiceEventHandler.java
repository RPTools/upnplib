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

package net.sbbi.upnp;

/**
 * Interface to implement to receive notifications about state
 * variables changes on au UPNP service. The object implementing this interface
 * can be used with the ServicesEventing class register method to receive the
 * desired notifications.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public interface ServiceEventHandler {
  
  /**
   * Handle a var change, called each time a UPNP service fires a
   * state variable eventing message.</br>
   * The code implemented in this method can block the thread.
   * @param varName the state variable name
   * @param newValue the new state variable value
   */
  public void handleStateVariableEvent( String varName, String newValue );
  
}

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

package net.sbbi.upnp.samples;

import java.net.InetAddress;
import java.rmi.RemoteException;

import net.sbbi.upnp.remote.UnicastRemoteObject;

/**
 * Implementation of the HelloWorld example.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class HelloWorld extends UnicastRemoteObject implements HelloWorldInterface {
  public final static long serialVersionUID = 1;

  public HelloWorld() throws RemoteException {
  }

  public String say( String myName ) throws RemoteException {
    String hostName = "localhost";
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch ( Exception ex ) {
    }
    return "Hello world to " + myName + " from computer " + hostName;
  }
}

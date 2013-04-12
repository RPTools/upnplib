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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Server program for the HelloWorld example.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class HelloWorldServer {
  
  public static void main ( String[] args ) {
    try {
      Registry reg = LocateRegistry.createRegistry( 1099 );
      System.out.println ( "Registry created" );
      HelloWorld hello = new HelloWorld();
      System.out.println ( "Object created" );
      reg.bind( "HelloWorld", hello );
      System.out.println ( "Object bound HelloWorld server is ready." );
    } catch ( Exception e ) {
      System.out.println( "Hello Server failed: " + e );
    }
  }
    
}

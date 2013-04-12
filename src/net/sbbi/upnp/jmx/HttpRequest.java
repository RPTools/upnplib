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

import java.util.*;

/**
 * A class to parse an HTTP request message.
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class HttpRequest {
  
  private String httpCommand;
  private String httpCommandArg;
  private Map<String, String> fields;
  private String body;
  
  /**
   * Constructor of the http request, will try to parse the raw request data
   * @param rawHttpRequest the raw request data
   */
  public HttpRequest( String rawHttpRequest ) {
    if ( rawHttpRequest.trim().length() == 0 ) {
      throw new IllegalArgumentException( "Empty HTTP request message" );
    }
    boolean bodyParsing = false;
    StringBuffer bodyParsed = new StringBuffer();
    fields = new HashMap<String, String>();
    String[] lines = rawHttpRequest.split( "\\r\\n" );
    String header = lines[0].trim();
    int space = header.indexOf( " " );
    if ( space != -1 ) {
      httpCommand = header.substring( 0, space );
      int space2 = header.indexOf( " ", space + 1 );
      if ( space2 != -1 ) {
        httpCommandArg = header.substring( space + 1, space2 );
      }
    }

    for ( int i = 1; i < lines.length; i++ ) {
      
      String line = lines[i];
      if ( line.length() == 0 ) {
        // line break before body
        bodyParsing = true;
      } else if ( bodyParsing ) {
        // we parse the message body
        bodyParsed.append( line ).append( "\r\n" );
      } else {
        // we parse the header
        if ( line.length() > 0 ) {
          int delim = line.indexOf( ':' );
          if ( delim != -1 ) {
            String key = line.substring( 0, delim ).toUpperCase();
            String value = line.substring( delim + 1 ).trim();
            fields.put( key, value );
          }
        }
      }	
    }
    if ( bodyParsing ) {
      body = bodyParsed.toString();
    }
  }
  
  public String getHttpCommand() {
    return httpCommand;
  }
  
  public String getHttpCommandArg() {
    return httpCommandArg;
  }
  
  public String getBody() {
    return body;
  }
  
  public String getHTTPFieldElement( String fieldName, String elementName ) throws IllegalArgumentException {
    String fieldNameValue = getHTTPHeaderField( fieldName );
    if ( fieldName!= null ) {
      
      StringTokenizer tokenizer = new StringTokenizer( fieldNameValue.trim(), "," );
      while (tokenizer.countTokens() > 0) {
        String nextToken = tokenizer.nextToken().trim();
        if ( nextToken.startsWith( elementName ) ) {
          int index = nextToken.indexOf( "=" );
          if ( index != -1 ) {
            return nextToken.substring( index + 1 );
          }
        }
      }
    }
    return null;
  }
  
  public String getHTTPHeaderField( String fieldName ) throws IllegalArgumentException {
    return fields.get( fieldName.toUpperCase() );
  }
}

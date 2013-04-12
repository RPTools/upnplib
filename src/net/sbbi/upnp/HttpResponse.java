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

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * A class to parse an HTTP response message.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class HttpResponse {
	private final static Logger log = Logger.getLogger(HttpResponse.class);
	private final String header;
	private final Map<String, String> fields;
	private String body;

	/**
	 * Constructor of the response, will try to parse the raw response data
	 * 
	 * @param rawHttpResponse
	 *            the raw response data
	 * @throws IllegalArgumentException
	 *             if some error occurs during parsing
	 */
	protected HttpResponse(String rawHttpResponse) throws IllegalArgumentException {
		if (rawHttpResponse == null || rawHttpResponse.trim().length() == 0) {
			throw new IllegalArgumentException("Empty HTTP response message");
		}
		boolean bodyParsing = false;
		StringBuffer bodyParsed = new StringBuffer();
		fields = new HashMap<String, String>();
		String[] lines = rawHttpResponse.split("\\r\\n");
		this.header = lines[0].trim();

		for (int i = 1; i < lines.length; i++) {
			String line = lines[i];
			if (log.isDebugEnabled())
				log.debug("Response: " + line + "\n");
			if (line.length() == 0) {
				// line break before body
				bodyParsing = true;
			} else if (bodyParsing) {
				// we parse the message body
				bodyParsed.append(line).append("\r\n");
			} else {
				// we parse the header
				if (line.length() > 0) {
					int delim = line.indexOf(':');
					if (delim != -1) {
						String key = line.substring(0, delim).toUpperCase();
						String value = line.substring(delim + 1).trim();
						fields.put(key, value);
					} else {
						throw new IllegalArgumentException("Invalid HTTP message header :" + line);
					}
				}
			}
		}
		if (bodyParsing) {
			body = bodyParsed.toString();
		}
	}

	public String getHeader() {
		return header;
	}

	public String getBody() {
		return body;
	}

	public String getHTTPFieldElement(String fieldName, String elementName) throws IllegalArgumentException {
		String fieldNameValue = getHTTPHeaderField(fieldName);
		if (fieldName != null) {

			StringTokenizer tokenizer = new StringTokenizer(fieldNameValue.trim(), ",");
			while (tokenizer.countTokens() > 0) {
				String nextToken = tokenizer.nextToken().trim();
				if (nextToken.startsWith(elementName)) {
					int index = nextToken.indexOf("=");
					if (index != -1) {
						return nextToken.substring(index + 1).trim();
					}
				}
			}
		}
		throw new IllegalArgumentException("HTTP element field " + elementName + " is not present");
	}

	public String getHTTPHeaderField(String fieldName) throws IllegalArgumentException {
		String field = fields.get(fieldName.toUpperCase());
		if (field == null) {
			throw new IllegalArgumentException("HTTP field " + fieldName + " is not present");
		}
		return field;
	}
}

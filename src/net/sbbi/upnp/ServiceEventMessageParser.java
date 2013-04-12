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

import org.xml.sax.Attributes;

/**
 * Simple SAX handler for UPNP service event message parsing, this message is in SOAP format
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ServiceEventMessageParser extends org.xml.sax.helpers.DefaultHandler {
	private boolean readPropertyName = false;
	private String currentPropName = null;
	private final Map<String, String> changedStateVars = new HashMap<String, String>();

	protected ServiceEventMessageParser() {
	}

	public Map<String, String> getChangedStateVars() {
		return changedStateVars;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (currentPropName != null) {
			String origChars = changedStateVars.get(currentPropName);
			String newChars = new String(ch, start, length);
			if (origChars == null) {
				changedStateVars.put(currentPropName, newChars);
			} else {
				changedStateVars.put(currentPropName, origChars + newChars);
			}
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		if (localName.equals("property")) {
			readPropertyName = true;
		} else if (readPropertyName) {
			currentPropName = localName;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (currentPropName != null && localName.equals(currentPropName)) {
			readPropertyName = false;
			currentPropName = null;
		}
	}
}

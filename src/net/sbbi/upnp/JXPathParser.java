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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.jxpath.xml.DOMParser;
import org.apache.commons.jxpath.xml.XMLParser2;
import org.apache.log4j.Logger;

/**
 * Parser to use with JXPath, this is used to fix some problems encountered with some UPNP devices returning buggy xml
 * docs... This parser acts like a wrapper and make some chars search and replace such as 0x0 with 0x20 to produce a
 * valid XML doc.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class JXPathParser extends XMLParser2 {
	private final static Logger log = Logger.getLogger(JXPathParser.class);
	private final char buggyChar = (char) 0;

	@Override
	public Object parseXML(InputStream in) {
		StringBuffer xml = new StringBuffer();
		try {
			byte[] buffer = new byte[512];
			int readen = 0;
			while ((readen = in.read(buffer)) != -1) {
				xml.append(new String(buffer, 0, readen));
			}
		} catch (IOException ex) {
			log.error("IOException occured during XML reception", ex);
			return null;
		}
		String doc = xml.toString();
		log.debug("Raw xml doc:\n" + doc);
		if (doc.indexOf(buggyChar) != -1) {
			doc = doc.replace(buggyChar, ' ');
		}
		ByteArrayInputStream in2 = new ByteArrayInputStream(doc.getBytes());
		DOMParser parser = new DOMParser();
		return parser.parseXML(in2);
	}
}

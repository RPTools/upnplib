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

import net.sbbi.upnp.services.ServiceStateVariable;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Simple SAX handler for UPNP state variable query response message parsing, this message is in SOAP format
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class StateVariableResponseParser extends org.xml.sax.helpers.DefaultHandler {
	private final static Logger log = Logger.getLogger(StateVariableResponseParser.class);

	private final static String SOAP_FAULT_EL = "Fault";

	private final ServiceStateVariable stateVar;
	private boolean faultResponse = false;
	private UPNPResponseException msgEx;

	private boolean readFaultCode = false;
	private boolean readFaultString = false;
	private boolean readErrorCode = false;
	private boolean readErrorDescription = false;
	private boolean parseStateVar = false;
	private StateVariableResponse result;

	protected StateVariableResponseParser(ServiceStateVariable stateVar) {
		this.stateVar = stateVar;
	}

	protected UPNPResponseException getUPNPResponseException() {
		return msgEx;
	}

	protected StateVariableResponse getStateVariableResponse() {
		return result;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (parseStateVar) {
			String origChars = result.stateVariableValue;
			String newChars = new String(ch, start, length);
			if (origChars == null) {
				result.stateVariableValue = newChars;
			} else {
				result.stateVariableValue = origChars + newChars;
			}
		} else if (readFaultCode) {
			msgEx.faultCode = new String(ch, start, length);
			readFaultCode = false;
		} else if (readFaultString) {
			msgEx.faultString = new String(ch, start, length);
			readFaultString = false;
		} else if (readErrorCode) {
			String code = new String(ch, start, length);
			try {
				msgEx.detailErrorCode = Integer.parseInt(code);
			} catch (Throwable ex) {
				log.debug("Error during returned error code " + code + " parsing");
			}
			readErrorCode = false;
		} else if (readErrorDescription) {
			msgEx.detailErrorDescription = new String(ch, start, length);
			readErrorDescription = false;
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {

		if (faultResponse) {
			if (localName.equals("faultcode")) {
				readFaultCode = true;
			} else if (localName.equals("faultstring")) {
				readFaultString = true;
			} else if (localName.equals("errorCode")) {
				readErrorCode = true;
			} else if (localName.equals("errorDescription")) {
				readErrorDescription = true;
			}
		} else if (localName.equals(SOAP_FAULT_EL)) {
			msgEx = new UPNPResponseException();
			faultResponse = true;
		} else if (localName.equals("return") || localName.equals("varName")) {
			// some buggy implementations ( intel sample media server )
			// do not use the specs compliant return element name but varName ...
			parseStateVar = true;
			result = new StateVariableResponse();
			result.stateVar = stateVar;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// some buggy implementations ( intel sample media server )
		// do not use the specs compliant return element name but varName ...
		if (localName.equals("return") || localName.equals("varName")) {
			parseStateVar = false;
		}
	}
}

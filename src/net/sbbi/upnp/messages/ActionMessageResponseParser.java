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

import net.sbbi.upnp.services.ServiceAction;
import net.sbbi.upnp.services.ServiceActionArgument;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

/**
 * Simple SAX handler for UPNP response message parsing, this message is in SOAP format
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class ActionMessageResponseParser extends org.xml.sax.helpers.DefaultHandler {
	private final static Logger log = Logger.getLogger(ActionMessageResponseParser.class);

	private final static String SOAP_FAULT_EL = "Fault";

	private final ServiceAction serviceAction;
	private final String bodyElementName;
	private boolean faultResponse = false;
	private UPNPResponseException msgEx;

	private boolean readFaultCode = false;
	private boolean readFaultString = false;
	private boolean readErrorCode = false;
	private boolean readErrorDescription = false;
	private boolean parseOutputParams = false;
	private ActionResponse result;
	private ServiceActionArgument parsedResultOutArg;

	protected ActionMessageResponseParser(ServiceAction serviceAction) {
		this.serviceAction = serviceAction;
		bodyElementName = serviceAction.getName() + "Response";
	}

	protected UPNPResponseException getUPNPResponseException() {
		return msgEx;
	}

	protected ActionResponse getActionResponse() {
		return result;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (parseOutputParams) {
			if (parsedResultOutArg != null) {
				String origChars = result.getOutActionArgumentValue(parsedResultOutArg.getName());
				String newChars = new String(ch, start, length);
				if (origChars == null) {
					result.addResult(parsedResultOutArg, newChars);
				} else {
					result.addResult(parsedResultOutArg, origChars + newChars);
				}
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
		if (parseOutputParams) {
			ServiceActionArgument arg = serviceAction.getActionArgument(localName);
			if (arg != null && arg.getDirection() == ServiceActionArgument.DIRECTION_OUT) {
				parsedResultOutArg = arg;
				result.addResult(parsedResultOutArg, null);
			} else {
				parsedResultOutArg = null;
			}
		} else if (faultResponse) {
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
		} else if (localName.equals(bodyElementName)) {
			parseOutputParams = true;
			result = new ActionResponse();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (parsedResultOutArg != null && parsedResultOutArg.getName().equals(localName)) {
			parsedResultOutArg = null;
		} else if (localName.equals(bodyElementName)) {
			parseOutputParams = false;
		}
	}
}

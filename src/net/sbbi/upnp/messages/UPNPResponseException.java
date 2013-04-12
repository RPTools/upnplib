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

/**
 * An exception throws when parsing a message if a SOAP fault exception is returned.
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class UPNPResponseException extends Exception {
	private static final long serialVersionUID = 8313107558129180594L;

	protected String faultCode;
	protected String faultString;
	protected int detailErrorCode;
	protected String detailErrorDescription;

	public UPNPResponseException() {
	}

	public UPNPResponseException(int detailErrorCode, String detailErrorDescription) {
		this.detailErrorCode = detailErrorCode;
		this.detailErrorDescription = detailErrorDescription;
	}

	public String getFaultCode() {
		return faultCode == null ? "Client" : faultCode;
	}

	public String getFaultString() {
		return faultString == null ? "UPnPError" : faultString;
	}

	public int getDetailErrorCode() {
		return detailErrorCode;
	}

	public String getDetailErrorDescription() {
		return detailErrorDescription;
	}

	@Override
	public String getMessage() {
		return "Detailed error code :" + detailErrorCode + ", Detailed error description :" + detailErrorDescription;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}
}

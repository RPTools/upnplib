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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sbbi.upnp.services.ServiceStateVariable;
import net.sbbi.upnp.services.UPNPService;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is used to create state variable messages to comminicate with the device
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class StateVariableMessage {
	private final static Logger log = Logger.getLogger(StateVariableMessage.class);

	private final UPNPService service;
	private final ServiceStateVariable serviceStateVar;

	protected StateVariableMessage(UPNPService service, ServiceStateVariable serviceStateVar) {
		this.service = service;
		this.serviceStateVar = serviceStateVar;
	}

	/**
	 * Executes the state variable query and retuns the UPNP device response, according to the UPNP specs, this method
	 * could take up to 30 secs to process ( time allowed for a device to respond to a request )
	 * 
	 * @return a state variable response object containing the variable value
	 * @throws IOException
	 *             if some IOException occurs during message send and reception process
	 * @throws UPNPResponseException
	 *             if an UPNP error message is returned from the server or if some parsing exception occurs (
	 *             detailErrorCode = 899, detailErrorDescription = SAXException message )
	 */
	public StateVariableResponse service() throws IOException, UPNPResponseException {
		StateVariableResponse rtrVal = null;
		UPNPResponseException upnpEx = null;
		IOException ioEx = null;
		StringBuffer body = new StringBuffer(256);

		body.append("<?xml version=\"1.0\"?>\r\n");
		body.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"");
		body.append(" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">");
		body.append("<s:Body>");
		body.append("<u:QueryStateVariable xmlns:u=\"urn:schemas-upnp-org:control-1-0\">");
		body.append("<u:varName>").append(serviceStateVar.getName()).append("</u:varName>");
		body.append("</u:QueryStateVariable>");
		body.append("</s:Body>");
		body.append("</s:Envelope>");

		if (log.isDebugEnabled())
			log.debug("POST prepared for URL " + service.getControlURL());
		URL url = new URL(service.getControlURL().toString());
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		HttpURLConnection.setFollowRedirects(false);
		//conn.setConnectTimeout( 30000 );
		conn.setRequestProperty("HOST", url.getHost() + ":" + url.getPort());
		conn.setRequestProperty("SOAPACTION", "\"urn:schemas-upnp-org:control-1-0#QueryStateVariable\"");
		conn.setRequestProperty("CONTENT-TYPE", "text/xml; charset=\"utf-8\"");
		conn.setRequestProperty("CONTENT-LENGTH", Integer.toString(body.length()));
		OutputStream out = conn.getOutputStream();
		out.write(body.toString().getBytes());
		out.flush();
		conn.connect();
		InputStream input = null;

		if (log.isDebugEnabled())
			log.debug("executing query :\n" + body);
		try {
			input = conn.getInputStream();
		} catch (IOException ex) {
			// java can throw an exception if he error code is 500 or 404 or something else than 200
			// but the device sends 500 error message with content that is required
			// this content is accessible with the getErrorStream
			input = conn.getErrorStream();
		}

		if (input != null) {
			int response = conn.getResponseCode();
			String responseBody = getResponseBody(input);
			if (log.isDebugEnabled())
				log.debug("received response :\n" + responseBody);
			SAXParserFactory saxParFact = SAXParserFactory.newInstance();
			saxParFact.setValidating(false);
			saxParFact.setNamespaceAware(true);
			StateVariableResponseParser msgParser = new StateVariableResponseParser(serviceStateVar);
			StringReader stringReader = new StringReader(responseBody);
			InputSource src = new InputSource(stringReader);
			try {
				SAXParser parser = saxParFact.newSAXParser();
				parser.parse(src, msgParser);
			} catch (ParserConfigurationException confEx) {
				// should never happen
				// we throw a runtimeException to notify the env problem
				throw new RuntimeException("ParserConfigurationException during SAX parser creation, please check your env settings:" + confEx.getMessage());
			} catch (SAXException saxEx) {
				// kind of tricky but better than nothing..
				upnpEx = new UPNPResponseException(899, saxEx.getMessage());
			} finally {
				try {
					input.close();
				} catch (IOException ex) {
					// ignoring
				}
			}
			if (upnpEx == null) {
				if (response == HttpURLConnection.HTTP_OK) {
					rtrVal = msgParser.getStateVariableResponse();
				} else if (response == HttpURLConnection.HTTP_INTERNAL_ERROR) {
					upnpEx = msgParser.getUPNPResponseException();
				} else {
					ioEx = new IOException("Unexpected server HTTP response:" + response);
				}
			}
		}
		try {
			out.close();
		} catch (IOException ex) {
			// ignore
		}
		conn.disconnect();
		if (upnpEx != null) {
			throw upnpEx;
		}
		if (rtrVal == null && ioEx == null) {
			ioEx = new IOException("Unable to receive a response from the UPNP device");
		}
		if (ioEx != null) {
			throw ioEx;
		}
		return rtrVal;
	}

	private String getResponseBody(InputStream in) throws IOException {
		byte[] buffer = new byte[256];
		int readen = 0;
		StringBuffer content = new StringBuffer(256);
		while ((readen = in.read(buffer)) != -1) {
			content.append(new String(buffer, 0, readen));
		}
		// some devices add \0 chars at XML message end
		// which causes XML parsing errors...
		int len = content.length();
		while (content.charAt(len - 1) == '\0') {
			len--;
			content.setLength(len);
		}
		return content.toString().trim();
	}
}

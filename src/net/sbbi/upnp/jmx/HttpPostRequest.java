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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.ServiceStateVariable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class to handle HTTP POST requests on UPNPMBeanDevices
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class HttpPostRequest implements HttpRequestHandler {

	private final static HttpPostRequest instance = new HttpPostRequest();

	private static final String STATE_VAR_ACTION_URN = "urn:schemas-upnp-org:control-1-0#QueryStateVariable";

	private final DocumentBuilderFactory builder = DocumentBuilderFactory.newInstance();

	private HttpPostRequest() {
		builder.setNamespaceAware(true);
	}

	public static HttpRequestHandler getInstance() {
		return instance;
	}

	public String service(Set<UPNPMBeanDevice> devices, HttpRequest request) {
		String rtr = null;
		String filePath = request.getHttpCommandArg();
		// Request are : /uuid/serviceId/control || /uuid/serviceId/events
		boolean validPostUrl = (filePath.startsWith("/") && filePath.endsWith("/control")) ||
				(filePath.startsWith("/") && filePath.endsWith("/events"));

		if (validPostUrl) {
			String uuid = null;
			String serviceUuid = null;
			int lastSlash = filePath.lastIndexOf('/');
			if (lastSlash != -1) {
				serviceUuid = filePath.substring(1, lastSlash);
				// check if this is an uuid/desc.xml or uuid/serviceId/scpd.xml request type
				int slashIndex = serviceUuid.indexOf("/");
				if (slashIndex != -1) {
					uuid = serviceUuid.substring(0, slashIndex);
				}
			}

			if (uuid != null) {
				// search now the bean within the set
				UPNPMBeanDevice device = null;
				UPNPMBeanService service = null;
				synchronized (devices) {
					for (Iterator<UPNPMBeanDevice> i = devices.iterator(); i.hasNext();) {
						device = i.next();
						if (device.getUuid().equals(uuid)) {
							// found
							service = device.getUPNPMBeanService(serviceUuid);
							break;
						}
					}
				}

				if (service != null) {
					try {
						if (filePath.endsWith("/control")) {
							String soapAction = request.getHTTPHeaderField("SOAPACTION");
							String soapRequest = request.getBody();
							if (soapRequest == null || soapRequest.trim().length() == 0) {
								throw new IllegalArgumentException("No SOAP request provided");
							}
							if (soapAction.indexOf(STATE_VAR_ACTION_URN) != -1) {
								String requestedVar = getQueryStateVariableVarName(soapRequest);
								if (requestedVar == null || requestedVar.trim().length() == 0) {
									throw new IllegalArgumentException("No varname content provided");
								}
								Object result = null;
								// if the state variable has been created for an operation (input or output argument)
								// then we cannot call the method on the mBean since this is not a callable jmx attribute.
								if (service.getOperationsStateVariables().get(requestedVar) == null) {
									try {
										result = service.getAttribute(requestedVar);
									} catch (AttributeNotFoundException ex) {
										throw new UPNPResponseException(404, "State varibale " + requestedVar + " unknown");
									}
								}
								rtr = getQueryStateVariableResult(result);
							} else {
								// this is an operation that is called
								if (soapAction == null || soapAction.trim().length() == 0) {
									throw new IllegalArgumentException("Missing SOAPACTION HTTP header");
								}
								if (!soapAction.startsWith("\"") ||
										!soapAction.endsWith("\"") ||
										soapAction.indexOf("#") == -1) {
									throw new IllegalArgumentException("Invalid SOAPACTION HTTP header (" + soapAction + ") check your specs");
								}
								// ok this is an action. first let's parse the XML message
								String actionName = getActionName(soapAction, service.getServiceType());
								if (actionName == null) {
									throw new UPNPResponseException(401, "Provided SOAPACTION (" + soapAction + ") wrongly " +
											"formatted or does not match target device type (" + device.getDeviceType() + ")");
								}
								String[] providedParams = getActionParams(soapRequest, actionName, service.getServiceType());
								Object result = null;
								String[] signature = null;
								Object[] parameters = null;
								Object[] signatureAndVals = getSignatureAndParamsVals(providedParams, actionName, service);
								if (signatureAndVals != null) {
									signature = (String[]) signatureAndVals[0];
									parameters = (Object[]) signatureAndVals[1];
								}
								try {
									result = service.invoke(actionName, parameters, signature);
								} catch (ReflectionException ex) {
									throw ex.getTargetException();
								}
								rtr = getActionResult(result, service.getServiceType(), actionName);
							}
						} else if (filePath.endsWith("/events")) {
							// TODO implement eventing
							throw new Exception("Not yet implemented :o(, try again with the next software version");
						}
					} catch (Exception ex) {
						rtr = createSOAPError(ex);
					}
				}
			}
		}
		return rtr;

	}

	private MBeanOperationInfo getOperationInfo(MBeanInfo beanInfo, String methodName) {
		MBeanOperationInfo[] ops = beanInfo.getOperations();
		for (int i = 0; i < ops.length; i++) {
			if (ops[i].getName().equals(methodName)) {
				return ops[i];
			}
		}
		return null;
	}

	private Object[] getParameterInfo(MBeanOperationInfo opInfo, String paramName) {
		MBeanParameterInfo[] args = opInfo.getSignature();
		for (int i = 0; i < args.length; i++) {
			if (args[i].getName().equals(paramName)) {
				return new Object[] { args[i], new Integer(i) };
			}
		}
		return null;
	}

	private Object[] getSignatureAndParamsVals(String[] providedParams, String methodName, UPNPMBeanService service) throws UPNPResponseException {

		MBeanOperationInfo opInfo = getOperationInfo(service.getMBeanInfo(), methodName);
		if (opInfo == null) {
			// OUPS have a serious problem here !
			throw new RuntimeException("Unexpected null MBeanOperationInfo for operation " + methodName);
		}

		int providedParamsCount = 0;
		if (providedParams != null) {
			providedParamsCount = providedParams.length / 2;
		}
		if (opInfo.getSignature().length != providedParamsCount) {
			throw new UPNPResponseException(402, "Invalid provided parameter(s) count (" + providedParamsCount + ") for action " +
					methodName + ", " + opInfo.getSignature().length + " parameter(s) are needed");
		}
		// checks done, no params provided, returning null
		if (providedParamsCount == 0) {
			return null;
		}

		Object[] rtrVal = new Object[2];
		rtrVal[0] = new String[providedParamsCount];
		rtrVal[1] = new Object[providedParamsCount];

		for (int i = 0; i < providedParams.length; i += 2) {
			String paramName = providedParams[i];
			String paramValue = providedParams[i + 1];
			String paramType = service.getOperationsStateVariables().get(paramName);
			if (paramType == null) {
				throw new UPNPResponseException(402, "Unknown action " + methodName + " parameter " + paramName);
			}
			Object[] beanParamInfos = getParameterInfo(opInfo, paramName);
			if (beanParamInfos == null) {
				// should never happen
				throw new UPNPResponseException(402, "Unknown action " + methodName + " parameter " + paramName);
			}
			MBeanParameterInfo beanParamInfo = (MBeanParameterInfo) beanParamInfos[0];
			int paramSignaturePos = ((Integer) beanParamInfos[1]).intValue();
			Object value = null;
			try {
				// TODO what about null or empty params ?
				value = ServiceStateVariable.UPNPToJavaObject(paramType, paramValue);
			} catch (Throwable t) {
				throw new UPNPResponseException(501, "Error occured during parameter " + paramName + "(" + paramType + ") value " + paramValue + " parsing:" + t.getMessage());
			}
			String[] sign = (String[]) rtrVal[0];
			sign[paramSignaturePos] = beanParamInfo.getType();
			Object[] vals = (Object[]) rtrVal[1];
			vals[paramSignaturePos] = value;
		}
		return rtrVal;
	}

	private String getActionName(String SOAPAction, String serviceType) {
		int index = SOAPAction.indexOf(serviceType);
		if (index != -1) {
			try {
				return SOAPAction.substring(serviceType.length() + 2, SOAPAction.length() - 1);
			} catch (Throwable t) {
				// probable wrongly formatted
			}
		}
		return null;
	}

	private String[] getActionParams(String xmlRequest, String actionName, String serviceType) throws Exception {
		String[] rtrVal = null;
		ByteArrayInputStream in = new ByteArrayInputStream(xmlRequest.getBytes());
		InputSource src = new InputSource(in);
		Document doc = null;
		synchronized (builder) {
			doc = builder.newDocumentBuilder().parse(src);
		}
		Element root = doc.getDocumentElement();
		Element body = (Element) root.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);
		if (body == null) {
			throw new IllegalArgumentException("Missing body tag");
		}
		Element action = (Element) body.getElementsByTagNameNS(serviceType, actionName).item(0);
		if (action == null) {
			throw new IllegalArgumentException("Missing action tag " + actionName);
		}
		NodeList params = action.getChildNodes();
		int length = 0;
		for (int i = 0; i < params.getLength(); i++) {
			if (params.item(i) instanceof Element) {
				length++;
			}
		}
		if (length > 0) {
			rtrVal = new String[length * 2];
			int j = 0;
			for (int i = 0; i < params.getLength(); i++) {
				if (params.item(i) instanceof Element) {
					Element arg = (Element) params.item(i);
					rtrVal[j] = arg.getNodeName();
					rtrVal[j + 1] = arg.getFirstChild().getNodeValue();
					j += 2;
				}
			}
		}
		return rtrVal;
	}

	private String getQueryStateVariableVarName(String xmlRequest) throws Exception {

		ByteArrayInputStream in = new ByteArrayInputStream(xmlRequest.getBytes());
		InputSource src = new InputSource(in);
		Document doc = null;
		synchronized (builder) {
			doc = builder.newDocumentBuilder().parse(src);
		}
		Element root = doc.getDocumentElement();

		Element body = (Element) root.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body").item(0);
		if (body == null) {
			throw new IllegalArgumentException("Missing body tag");
		}
		Element query = (Element) body.getElementsByTagNameNS("urn:schemas-upnp-org:control-1-0", "QueryStateVariable").item(0);
		if (query == null) {
			throw new IllegalArgumentException("Missing query tag");
		}
		Element varName = (Element) query.getElementsByTagNameNS("urn:schemas-upnp-org:control-1-0", "varName").item(0);
		if (varName == null) {
			throw new IllegalArgumentException("Missing varName tag");
		}
		return varName.getFirstChild().getNodeValue();
	}

	private String createSOAPError(Throwable ex) {
		StringBuffer rtr = new StringBuffer();
		int errorCode;
		String errorDescription = null;

		if (ex instanceof UPNPResponseException) {
			UPNPResponseException upnpEx = (UPNPResponseException) ex;
			errorCode = upnpEx.getDetailErrorCode();
			errorDescription = upnpEx.getDetailErrorDescription();
			if (upnpEx.getCause() != null) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PrintWriter writer = new PrintWriter(out);
				upnpEx.getCause().printStackTrace(writer);
				writer.flush();
				writer.close();
				errorDescription += "\nAttached stack trace:\n" + new String(out.toByteArray());
			}
		} else {
			errorCode = 501;
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(out);
			ex.printStackTrace(writer);
			writer.flush();
			writer.close();
			errorDescription = new String(out.toByteArray());
		}

		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\"?>\r\n")
				.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
				.append("s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n")
				.append("<s:Body>\r\n<s:Fault>\r\n")
				.append("<faultcode>s:Client</faultcode>\r\n")
				.append("<faultstring>UPnPError</faultstring>\r\n")
				.append("<detail>\r\n")
				.append("<UPnPError xmlns=\"urn:schemas-upnp-org:control-1-0\">\r\n")
				.append("<errorCode>").append(errorCode).append("</errorCode>\r\n")
				.append("<errorDescription>").append(errorDescription).append("</errorDescription>\r\n")
				.append("</UPnPError>\r\n")
				.append("</detail>\r\n")
				.append("</s:Fault></s:Body>\r\n")
				.append("</s:Envelope>");

		rtr.append("HTTP/1.1 500 Server error\r\n")
				.append("CONTENT-LENGTH: ").append(xml.length()).append("\r\n")
				.append("CONTENT-TYPE: text/xml\r\n\r\n")
				.append(xml);

		return rtr.toString();
	}

	private String getActionResult(Object result, String serviceType, String actionName) {
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\"?>\r\n")
				.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
				.append("s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n")
				.append("<s:Body>\r\n")
				.append("<u:").append(actionName).append("Response xmlns:u=\"").append(serviceType).append("\">\r\n")
				.append("<").append(actionName).append("_out>").append(getResultAsString(result))
				.append("</").append(actionName).append("_out>\r\n")
				.append("</u:").append(actionName).append("Response>\r\n")
				.append("</s:Body>\r\n")
				.append("</s:Envelope>");

		StringBuffer rtr = new StringBuffer();
		rtr.append("HTTP/1.1 200 OK\r\n")
				.append("CONTENT-LENGTH: ").append(xml.length()).append("\r\n")
				.append("CONTENT-TYPE: text/xml; charset=\"utf-8\"\r\n")
				.append("EXT:\r\n")
				.append("SERVER: ").append(UPNPMBeanDevice.IMPL_NAME).append("\r\n\r\n")
				.append(xml);

		return rtr.toString();
	}

	private String getQueryStateVariableResult(Object result) {
		StringBuffer xml = new StringBuffer();
		xml.append("<?xml version=\"1.0\"?>\r\n")
				.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" ")
				.append("s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n")
				.append("<s:Body>\r\n")
				.append("<u:QueryStateVariableResponse xmlns:u=\"urn:schemas-upnp-org:control-1-0\">\r\n")
				.append("<return>").append(getResultAsString(result))
				.append("</return>\r\n")
				.append("</u:QueryStateVariableResponse>\r\n")
				.append("</s:Body>\r\n")
				.append("</s:Envelope>");

		StringBuffer rtr = new StringBuffer();
		rtr.append("HTTP/1.1 200 OK\r\n")
				.append("CONTENT-LENGTH: ").append(xml.length()).append("\r\n")
				.append("CONTENT-TYPE: text/xml; charset=\"utf-8\"\r\n")
				.append("EXT:\r\n")
				.append("SERVER: ").append(UPNPMBeanDevice.IMPL_NAME).append("\r\n\r\n")
				.append(xml);

		return rtr.toString();
	}

	private String getResultAsString(Object result) {
		// TODO result to utf-8
		if (result == null)
			return "";
		String rtrVal = null;

		if (result instanceof Object[]) {
			StringBuffer tmp = new StringBuffer();
			Object[] array = (Object[]) result;
			for (int i = 0; i < array.length; i++) {
				Object val = array[i];
				if (val != null) {
					tmp.append(val.toString());
				} else {
					tmp.append("null");
				}
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof long[]) {
			StringBuffer tmp = new StringBuffer();
			long[] array = (long[]) result;
			for (int i = 0; i < array.length; i++) {
				tmp.append(array[i]);
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof double[]) {
			StringBuffer tmp = new StringBuffer();
			double[] array = (double[]) result;
			for (int i = 0; i < array.length; i++) {
				tmp.append(array[i]);
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof float[]) {
			StringBuffer tmp = new StringBuffer();
			float[] array = (float[]) result;
			for (int i = 0; i < array.length; i++) {
				tmp.append(array[i]);
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof short[]) {
			StringBuffer tmp = new StringBuffer();
			short[] array = (short[]) result;
			for (int i = 0; i < array.length; i++) {
				tmp.append(array[i]);
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof int[]) {
			StringBuffer tmp = new StringBuffer();
			int[] array = (int[]) result;
			for (int i = 0; i < array.length; i++) {
				tmp.append(array[i]);
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof byte[]) {
			StringBuffer tmp = new StringBuffer();
			byte[] array = (byte[]) result;
			for (int i = 0; i < array.length; i++) {
				tmp.append(array[i]);
				if (i < array.length)
					tmp.append("\n");
			}
			rtrVal = tmp.toString();
		} else if (result instanceof char[]) {
			rtrVal = new String((char[]) result);
		} else {
			rtrVal = result.toString();
		}
		if (rtrVal.indexOf("<") != -1 || rtrVal.indexOf(">") != -1) {
			rtrVal = "<![CDATA[" + rtrVal + "]]>";
		}
		return rtrVal;
	}

}

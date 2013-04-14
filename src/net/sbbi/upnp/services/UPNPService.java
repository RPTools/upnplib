/*
 * This software copyright by various authors including the RPTools.net development team, and licensed under the LGPL
 * Version 3 or, at your option, any later version.
 * 
 * Portions of this software were originally covered under the Apache Software License, Version 1.1 or Version 2.0.
 * 
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.sbbi.upnp.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sbbi.upnp.JXPathParser;
import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;

import org.apache.commons.jxpath.Container;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.xml.DocumentContainer;
import org.apache.log4j.Logger;

/**
 * Representation of an UPNP service
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */
public class UPNPService {
	private final static Logger log = Logger.getLogger(UPNPService.class);

	protected String serviceType;
	protected String serviceId;
	private int specVersionMajor;
	private int specVersionMinor;
	protected URL SCPDURL;
	protected String SCPDURLData;
	protected URL controlURL;
	protected URL eventSubURL;
	protected UPNPDevice serviceOwnerDevice;

	protected Map<String, ServiceAction> UPNPServiceActions;
	protected Map<String, ServiceStateVariable> UPNPServiceStateVariables;
	private final String USN;

	private boolean parsedSCPD = false;
	private DocumentContainer UPNPService;

	public UPNPService(JXPathContext serviceCtx, URL baseDeviceURL, UPNPDevice serviceOwnerDevice) throws MalformedURLException {
		this.serviceOwnerDevice = serviceOwnerDevice;
		serviceType = (String) serviceCtx.getValue("upnp:serviceType");
		serviceId = (String) serviceCtx.getValue("upnp:serviceId");
		SCPDURL = UPNPRootDevice.getURL((String) serviceCtx.getValue("upnp:SCPDURL"), baseDeviceURL);
		controlURL = UPNPRootDevice.getURL((String) serviceCtx.getValue("upnp:controlURL"), baseDeviceURL);
		eventSubURL = UPNPRootDevice.getURL((String) serviceCtx.getValue("upnp:eventSubURL"), baseDeviceURL);
		USN = serviceOwnerDevice.getUDN().concat("::").concat(serviceType);
	}

	public String getServiceType() {
		return serviceType;
	}

	public String getServiceId() {
		return serviceId;
	}

	public String getUSN() {
		return USN;
	}

	public URL getSCPDURL() {
		return SCPDURL;
	}

	public URL getControlURL() {
		return controlURL;
	}

	public URL getEventSubURL() {
		return eventSubURL;
	}

	public int getSpecVersionMajor() {
		lazyInitiate();
		return specVersionMajor;
	}

	public int getSpecVersionMinor() {
		lazyInitiate();
		return specVersionMinor;
	}

	public UPNPDevice getServiceOwnerDevice() {
		return serviceOwnerDevice;
	}

	/**
	 * Retreives a service action for its given name
	 * 
	 * @param actionName
	 *            the service action name
	 * @return a ServiceAction object or null if no matching action for this service has been found
	 */
	public ServiceAction getUPNPServiceAction(String actionName) {
		lazyInitiate();
		return UPNPServiceActions.get(actionName);
	}

	/**
	 * Retreives a service state variable for its given name
	 * 
	 * @param stateVariableName
	 *            the state variable name
	 * @return a ServiceStateVariable object or null if no matching state variable has been found
	 */
	public ServiceStateVariable getUPNPServiceStateVariable(String stateVariableName) {
		lazyInitiate();
		return UPNPServiceStateVariables.get(stateVariableName);
	}

	public Iterator<String> getAvailableActionsName() {
		lazyInitiate();
		return UPNPServiceActions.keySet().iterator();
	}

	public int getAvailableActionsSize() {
		lazyInitiate();
		return UPNPServiceActions.keySet().size();
	}

	public Iterator<String> getAvailableStateVariableName() {
		lazyInitiate();
		return UPNPServiceStateVariables.keySet().iterator();
	}

	public int getAvailableStateVariableSize() {
		lazyInitiate();
		return UPNPServiceStateVariables.keySet().size();
	}

	private void parseSCPD() {
		try {
			DocumentContainer.registerXMLParser(DocumentContainer.MODEL_DOM, new JXPathParser());
			UPNPService = new DocumentContainer(SCPDURL, DocumentContainer.MODEL_DOM);
			JXPathContext context = JXPathContext.newContext(this);
			context.registerNamespace("upnp", "urn:schemas-upnp-org:service-1-0");
			Pointer rootPtr = null;
			rootPtr = context.getPointer("UPNPService/upnp:scpd");
			JXPathContext rootCtx = context.getRelativeContext(rootPtr);

			specVersionMajor = Integer.parseInt((String) rootCtx.getValue("upnp:specVersion/upnp:major"));
			specVersionMinor = Integer.parseInt((String) rootCtx.getValue("upnp:specVersion/upnp:minor"));
			if (log.isDebugEnabled())
				log.debug("Found SCPD major,minor version " + specVersionMajor + "," + specVersionMinor);

			parseServiceStateVariables(rootCtx);

			Pointer actionsListPtr = rootCtx.getPointer("upnp:actionList");
			JXPathContext actionsListCtx = context.getRelativeContext(actionsListPtr);
			Double arraySize = (Double) actionsListCtx.getValue("count( upnp:action )");
			if (log.isDebugEnabled())
				log.debug("child actions count is " + arraySize.toString());

			UPNPServiceActions = new HashMap<String, ServiceAction>();
			for (int idx = 1; idx <= arraySize.intValue(); idx++) {
				ServiceAction action = new ServiceAction();
				action.name = (String) actionsListCtx.getValue("upnp:action[" + idx + "]/upnp:name");
				action.parent = this;
				Pointer argumentListPtr = null;
				try {
					argumentListPtr = actionsListCtx.getPointer("upnp:action[" + idx + "]/upnp:argumentList");
				} catch (JXPathException ex) {
					// there is no arguments list.
				}
				if (log.isDebugEnabled())
					log.debug("Processing name,argumentList of " + action.name + "," + (argumentListPtr == null ? "null" : argumentListPtr));
				if (argumentListPtr != null) {
					JXPathContext argumentListCtx = actionsListCtx.getRelativeContext(argumentListPtr);
					Double arraySizeArgs = (Double) argumentListCtx.getValue("count( upnp:argument )");
					if (log.isDebugEnabled())
						log.debug("number of argument elements is " + arraySizeArgs.toString());

					List<ServiceActionArgument> orderedActionArguments = new ArrayList<ServiceActionArgument>();
					for (int zed = 1; zed <= arraySizeArgs.intValue(); zed++) {
						ServiceActionArgument arg = new ServiceActionArgument();
						arg.name = (String) argumentListCtx.getValue("upnp:argument[" + zed + "]/upnp:name");
						String direction = (String) argumentListCtx.getValue("upnp:argument[" + zed + "]/upnp:direction");
						arg.direction = direction.equals(ServiceActionArgument.DIRECTION_IN) ? ServiceActionArgument.DIRECTION_IN : ServiceActionArgument.DIRECTION_OUT;
						String stateVarName = (String) argumentListCtx.getValue("upnp:argument[" + zed + "]/upnp:relatedStateVariable");
						ServiceStateVariable stateVar = UPNPServiceStateVariables.get(stateVarName);
						if (stateVar == null) {
							throw new IllegalArgumentException("Unable to find any state variable named " + stateVarName
									+ " for service " + getServiceId() + " action " + action.name + " argument "
									+ arg.name);
						}
						arg.relatedStateVariable = stateVar;
						orderedActionArguments.add(arg);
						if (log.isDebugEnabled())
							log.debug("found argument name,direction of " + arg.name + "," + arg.direction);
					}
					if (arraySizeArgs.intValue() > 0) {
						action.setActionArguments(orderedActionArguments);
					}
				}
				UPNPServiceActions.put(action.getName(), action);
			}
			parsedSCPD = true;
		} catch (Throwable t) {
			throw new RuntimeException("Error during lazy SCDP file parsing at " + SCPDURL, t);
		}
	}

	private void parseServiceStateVariables(JXPathContext rootContext) {
		Pointer serviceStateTablePtr = rootContext.getPointer("upnp:serviceStateTable");
		JXPathContext serviceStateTableCtx = rootContext.getRelativeContext(serviceStateTablePtr);
		Double arraySize = (Double) serviceStateTableCtx.getValue("count( upnp:stateVariable )");
		if (log.isDebugEnabled())
			log.debug("child stateVariable count is " + arraySize.toString());

		UPNPServiceStateVariables = new HashMap<String, ServiceStateVariable>();
		for (int idx = 1; idx <= arraySize.intValue(); idx++) {
			ServiceStateVariable srvStateVar = new ServiceStateVariable();
			String sendEventsLcl = null;
			try {
				sendEventsLcl = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/@upnp:sendEvents");
			} catch (JXPathException defEx) {
				// sendEvents not provided defaulting according to specs to "yes"
				sendEventsLcl = "yes";
			}
			srvStateVar.parent = this;
			srvStateVar.sendEvents = sendEventsLcl.equalsIgnoreCase("no") ? false : true;
			srvStateVar.name = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/upnp:name");
			srvStateVar.dataType = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/upnp:dataType");
			try {
				srvStateVar.defaultValue = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/upnp:defaultValue");
			} catch (JXPathException defEx) {
				// can happen since default value is not mandatory
			}
			Pointer allowedValuesPtr = null;
			try {
				allowedValuesPtr = serviceStateTableCtx.getPointer("upnp:stateVariable[" + idx + "]/upnp:allowedValueList");
			} catch (JXPathException ex) {
				// there is no allowed values list.
			}
			if (allowedValuesPtr != null) {
				JXPathContext allowedValuesCtx = serviceStateTableCtx.getRelativeContext(allowedValuesPtr);
				Double arraySizeAllowed = (Double) allowedValuesCtx.getValue("count( upnp:allowedValue )");
				if (log.isDebugEnabled())
					log.debug("child allowedValue count is " + arraySizeAllowed.toString());
				srvStateVar.allowedvalues = new HashSet<String>();
				for (int zed = 1; zed <= arraySizeAllowed.intValue(); zed++) {
					String allowedValue = (String) allowedValuesCtx.getValue("upnp:allowedValue[" + zed + "]");
					srvStateVar.allowedvalues.add(allowedValue);
				}
			}
			Pointer allowedValueRangePtr = null;
			try {
				allowedValueRangePtr = serviceStateTableCtx.getPointer("upnp:stateVariable[" + idx + "]/upnp:allowedValueRange");
			} catch (JXPathException ex) {
				// there is no allowed values list, can happen
			}
			if (allowedValueRangePtr != null) {
				srvStateVar.minimumRangeValue = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/upnp:allowedValueRange/upnp:minimum");
				srvStateVar.maximumRangeValue = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/upnp:allowedValueRange/upnp:maximum");
				try {
					srvStateVar.stepRangeValue = (String) serviceStateTableCtx.getValue("upnp:stateVariable[" + idx + "]/upnp:allowedValueRange/upnp:step");
				} catch (JXPathException stepEx) {
					// can happen since step is not mandatory
				}
				if (log.isDebugEnabled())
					log.debug("allowedRange is " + srvStateVar.minimumRangeValue + "-" + srvStateVar.maximumRangeValue + " step " + srvStateVar.stepRangeValue);
			}
			UPNPServiceStateVariables.put(srvStateVar.getName(), srvStateVar);
		}
	}

	private void lazyInitiate() {
		if (!parsedSCPD)
			synchronized (this) {
				if (!parsedSCPD)
					parseSCPD();
			}
	}

	/**
	 * Used for JXPath parsing, do not use this method
	 * 
	 * @return a Container object for Xpath parsing capabilities
	 */
	public Container getUPNPService() {
		return UPNPService;
	}

	public String getSCDPData() {
		if (SCPDURLData == null) {
			try {
				java.io.InputStream in = SCPDURL.openConnection().getInputStream();
				int readen = 0;
				byte[] buff = new byte[512];
				StringBuffer strBuff = new StringBuffer();
				while ((readen = in.read(buff)) != -1) {
					strBuff.append(new String(buff, 0, readen));
				}
				in.close();
				SCPDURLData = strBuff.toString();
			} catch (IOException ioEx) {
				return null;
			}
		}
		return SCPDURLData;
	}
}

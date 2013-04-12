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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import net.sbbi.upnp.devices.UPNPDevice;
import net.sbbi.upnp.devices.UPNPRootDevice;
import net.sbbi.upnp.messages.ActionMessage;
import net.sbbi.upnp.messages.ActionResponse;
import net.sbbi.upnp.messages.StateVariableMessage;
import net.sbbi.upnp.messages.StateVariableResponse;
import net.sbbi.upnp.messages.UPNPMessageFactory;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sbbi.upnp.services.ServiceAction;
import net.sbbi.upnp.services.ServiceActionArgument;
import net.sbbi.upnp.services.ServiceStateVariable;
import net.sbbi.upnp.services.UPNPService;

/**
 * This is a dynamic MBean for UPNP services The dynamic MBean will be populated with basic informations about UPNP
 * service attributes and operations.</br> You can provide a locale object that will try to lookup a resource bundle to
 * generate attributes and operations informations.</br> The bundle must contains the following data to match the UPNP
 * devices actions and state variables:</br> service.name=My Service description</br> attribute.ServiceAttribute=My
 * Attribute Desc</br> operation.ServiceOperation=My Action de description</br>
 * operation.ServiceOperation.ActionArgument1=My Action first argument description</br>
 * operation.ServiceOperation.ActionArgument2=My Action second argument description</br> and must be named with the UPNP
 * service Id + locale, the service id ':' char must be replaced with '_' chars. </br>
 * urn:upnp-org:serviceId:LANHostCfg1 will be translated into : </br> urn_upnp-org_serviceId_LANHostCfg1_fr.properties
 * 
 * @author <a href="mailto:superbonbon@sbbi.net">SuperBonBon</a>
 * @version 1.0
 */

public class UPNPServiceMBean implements DynamicMBean {

	private final UPNPService service;
	private final UPNPDevice device;
	private final UPNPMessageFactory fact;

	private ResourceBundle bundle;

	/**
	 * Creates a dynamic MBean from an UPNP service with no resource bundle to describe the service
	 * 
	 * @param device
	 *            the device that is hosting the service
	 * @param service
	 *            the service that will be exposed as an MBean
	 */
	public UPNPServiceMBean(UPNPDevice device, UPNPService service) {
		this(device, service, null, null);
	}

	/**
	 * Creates a dynamic MBean from an UPNP service
	 * 
	 * @param device
	 *            the device that is hosting the service
	 * @param service
	 *            the service that will be exposed as an MBean
	 * @param locale
	 *            the resource bundle locale, null if no bundle needs to be used..
	 * @param bundlePackage
	 *            the resource bundle package location into the classpath I.E net/sbbi/upnplib/myDevicesBundlesPackage,
	 *            null if the bundles are located at the classpath root.
	 */
	public UPNPServiceMBean(UPNPDevice device, UPNPService service, Locale locale, String bundlePackage) {
		this.service = service;
		this.device = device;
		if (locale != null) {
			if (bundlePackage != null && bundlePackage.length() == 0)
				bundlePackage = null;
			String bundlename = service.getServiceId().replace(':', '_');
			if (bundlePackage != null) {
				if (!bundlePackage.endsWith("/")) {
					bundlePackage += "/";
				}
				bundlename = bundlePackage + bundlename;
			}
			try {
				bundle = ResourceBundle.getBundle(bundlename, locale);
			} catch (Exception ex) {
				// silently ignoring..
			}
		}

		fact = UPNPMessageFactory.getNewInstance(service);
	}

	public Object getAttribute(String attributeName) throws AttributeNotFoundException, MBeanException, ReflectionException {

		StateVariableMessage msg = fact.getStateVariableMessage(attributeName);
		if (msg != null) {
			try {
				StateVariableResponse resp = msg.service();
				return resp.getStateVariableValue();
			} catch (Exception ex) {
				if (ex instanceof UPNPResponseException) {
					UPNPResponseException respEx = (UPNPResponseException) ex;
					if (respEx.getDetailErrorCode() == 404) {
						// some devices use that code to say that quering state variables is not supported
						throw new AttributeNotFoundException(respEx.getDetailErrorCode() + ":" + respEx.getDetailErrorDescription());
					}
					throw new MBeanException(ex, respEx.getDetailErrorCode() + ":" + respEx.getDetailErrorDescription());
				}
				throw new MBeanException(ex);
			}

		}
		throw new AttributeNotFoundException("Unable to find attribute " + attributeName);
	}

	public AttributeList getAttributes(String[] attributeNames) {
		AttributeList list = new AttributeList();
		for (int i = 0; i < attributeNames.length; i++) {
			try {
				Attribute attr = new Attribute(attributeNames[i], getAttribute(attributeNames[i]));
				list.add(attr);
			} catch (Exception ex) {
				// skip it, that kind of suck since we cannot throw any exception
			}
		}
		return list;
	}

	public MBeanInfo getMBeanInfo() {
		Iterator<String> itr = service.getAvailableStateVariableName();
		MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[service.getAvailableStateVariableSize()];
		int i = 0;

		while (itr.hasNext()) {
			String stateVariable = itr.next();

			ServiceStateVariable var = service.getUPNPServiceStateVariable(stateVariable);
			Class<?> type = var.getDataTypeAsClass();

			String variableName = null;
			if (bundle != null) {
				try {
					variableName = bundle.getString("attribute." + stateVariable);
				} catch (Exception ex) {
					//silently ignoring..
				}
			}
			if (variableName == null) {
				variableName = stateVariable + " description";
			}

			MBeanAttributeInfo info = new MBeanAttributeInfo(stateVariable,
					type != null ? type.getName() : String.class.getName(),
					variableName,
					true, false, false);

			attrs[i++] = info;
		}

		itr = service.getAvailableActionsName();
		MBeanOperationInfo[] operations = new MBeanOperationInfo[service.getAvailableActionsSize()];
		i = 0;
		while (itr.hasNext()) {
			String serviceAction = itr.next();
			ServiceAction action = service.getUPNPServiceAction(serviceAction);
			List<ServiceActionArgument> args = action.getInputActionArguments();
			MBeanParameterInfo[] params = null;
			if (args != null) {
				List<MBeanParameterInfo> tmp = new ArrayList<MBeanParameterInfo>();

				params = new MBeanParameterInfo[args.size()];
				int z = 0;
				for (Iterator<ServiceActionArgument> itr2 = args.iterator(); itr2.hasNext();) {
					ServiceActionArgument actArg = itr2.next();

					Class<?> type = actArg.getRelatedStateVariable().getDataTypeAsClass();
					String className = type != null ? type.getName() : String.class.getName();

					String actionArgName = null;
					if (bundle != null) {
						try {
							actionArgName = bundle.getString("operation." + action.getName() + "." + actArg.getName());
						} catch (Exception ex) {
							//silently ignoring..
						}
					}
					if (actionArgName == null) {
						actionArgName = actArg.getName() + " description";
					}

					MBeanParameterInfo param = new MBeanParameterInfo(actArg.getName(), className, actionArgName);
					tmp.add(param);
					params[z++] = param;
				}
				z = 0;
				params = new MBeanParameterInfo[tmp.size()];
				for (Iterator<MBeanParameterInfo> itr3 = tmp.iterator(); itr3.hasNext();) {
					params[z++] = itr3.next();
				}
			} else {
				params = new MBeanParameterInfo[0];
			}

			String returnType = Map.class.getName();
			if (action.getOutputActionArguments() == null) {
				returnType = void.class.getName();
			}

			String actionName = null;
			if (bundle != null) {
				try {
					actionName = bundle.getString("operation." + action.getName());
				} catch (Exception ex) {
					//silently ignoring..
				}
			}
			if (actionName == null) {
				actionName = action.getName() + " description";
			}

			MBeanOperationInfo info = new MBeanOperationInfo(
					action.getName(),
					actionName,
					params, returnType, MBeanOperationInfo.ACTION);
			operations[i++] = info;
		}

		String serviceDescr = null;
		if (bundle != null) {
			try {
				serviceDescr = bundle.getString("service.name");
			} catch (Exception ex) {
				//silently ignoring..
			}
		}
		if (serviceDescr == null) {
			serviceDescr = "Service Description";
		}

		return new MBeanInfo(this.getClass().getName(), serviceDescr, attrs, null, operations, null);
	}

	public Object invoke(String operationName, Object[] paramsValue, String[] signature) throws MBeanException, ReflectionException {

		ActionMessage msg = fact.getMessage(operationName);
		if (msg != null) {
			try {
				List<String> msgParams = msg.getInputParameterNames();
				if (paramsValue != null && msgParams != null) {
					if (paramsValue.length != msgParams.size()) {
						return null;
					}
					int i = 0;
					for (Iterator<String> itr = msgParams.iterator(); itr.hasNext(); i++) {
						String argName = itr.next();
						try {
							msg.setInputParameter(argName, paramsValue[i]);
						} catch (IllegalArgumentException ex) {
							throw new MBeanException(ex);
						}
					}
				}

				ActionResponse resp = msg.service();
				List<String> outParams = msg.getOutputParameterNames();
				if (outParams != null) {
					Map<String, String> rtrVal = new HashMap<String, String>();
					for (Iterator<String> i = outParams.iterator(); i.hasNext();) {
						String argName = i.next();
						String val = resp.getOutActionArgumentValue(argName);
						rtrVal.put(argName, val);
					}
					return rtrVal;
				}
				return null;
			} catch (Exception ex) {
				if (ex instanceof UPNPResponseException) {
					UPNPResponseException upnpEx = (UPNPResponseException) ex;
					throw new MBeanException(upnpEx, upnpEx.getMessage());
				} else {
					throw new MBeanException(ex);
				}
			}
		}
		return null;
	}

	public void setAttribute(Attribute attributeName) throws AttributeNotFoundException,
			InvalidAttributeValueException,
			MBeanException,
			ReflectionException {
		throw new AttributeNotFoundException("Unable to set attributes on an UPNP device");
	}

	public AttributeList setAttributes(AttributeList attributeNames) {
		return null;
	}

	/**
	 * Creates an object name for this device, this name should be used ot avoid any names collision especially with
	 * multiple sames UPNP devices type on the network
	 * 
	 * @return a new Object Name
	 */
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return new ObjectName(getDeviceDomainName(device.getDeviceType()) + ":name=" + getDeviceServiceName(service.getServiceId()) + "_" + this.hashCode());
	}

	private String getDeviceDomainName(String deviceType) {
		// takes an string like this urn:schemas-upnp-org:device:WANDevice:1
		// and must return only WANDevice
		String[] tokens = deviceType.split(":");
		return tokens[tokens.length - 2].replace(':', '_');
	}

	private String getDeviceServiceName(String serviceId) {
		// takes an stirng like this urn:upnp-org:serviceId:LANHostCfg1
		// and must return only LANHostCfg1
		String[] tokens = serviceId.split(":");
		return tokens[tokens.length - 1];
	}

	/**
	 * Creates UPNPServiceMBean device service mBeans for a given UPNPRootDevice
	 * 
	 * @param device
	 *            the root device
	 * @return an array of UPNPServiceMBean objects representing the UPNP device services MBeans
	 */
	public static UPNPServiceMBean[] getUPNPRootDeviceAsMBeans(UPNPRootDevice device) {
		List<UPNPService> services = device.getServices();
		Set<UPNPServiceMBean> mBeans = new HashSet<UPNPServiceMBean>();
		if (services != null) {
			registerServices(device, services, mBeans);
		}
		registerChildDevice(device.getChildDevices(), mBeans);

		UPNPServiceMBean[] rtrVal = new UPNPServiceMBean[mBeans.size()];
		int z = 0;
		for (Iterator<UPNPServiceMBean> i = mBeans.iterator(); i.hasNext();) {
			rtrVal[z++] = i.next();
		}
		return rtrVal;
	}

	private static void registerServices(UPNPDevice device, List<UPNPService> services, Set<UPNPServiceMBean> container) {
		for (Iterator<UPNPService> i = services.iterator(); i.hasNext();) {
			UPNPService srv = i.next();
			UPNPServiceMBean mBean = new UPNPServiceMBean(device, srv, null, null);
			container.add(mBean);
		}
	}

	private static void registerChildDevice(List<UPNPDevice> childDevices, Set<UPNPServiceMBean> container) {
		if (childDevices != null) {
			for (Iterator<UPNPDevice> itr = childDevices.iterator(); itr.hasNext();) {
				UPNPDevice device = itr.next();
				List<UPNPService> services = device.getServices();
				if (services != null) {
					registerServices(device, services, container);
				}
			}
		}
	}
}

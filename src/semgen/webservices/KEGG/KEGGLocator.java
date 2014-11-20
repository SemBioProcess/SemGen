/**
 * KEGGLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package semgen.webservices.KEGG;

import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;

public class KEGGLocator extends org.apache.axis.client.Service implements KEGG {
	private static final long serialVersionUID = 1L;

	public KEGGLocator() {
    }


    public KEGGLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public KEGGLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for KEGGPort
    private String KEGGPort_address = "http://soap.genome.jp/keggapi/request_v6.2.cgi";

    public String getKEGGPortAddress() {
        return KEGGPort_address;
    }

    // The WSDD service name defaults to the port name.
    private String KEGGPortWSDDServiceName = "KEGGPort";

    public String getKEGGPortWSDDServiceName() {
        return KEGGPortWSDDServiceName;
    }

    public void setKEGGPortWSDDServiceName(String name) {
        KEGGPortWSDDServiceName = name;
    }

    public KEGGPortType getKEGGPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(KEGGPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getKEGGPort(endpoint);
    }

    public KEGGPortType getKEGGPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            KEGGBindingStub _stub = new KEGGBindingStub(portAddress, this);
            _stub.setPortName(getKEGGPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setKEGGPortEndpointAddress(String address) {
        KEGGPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (KEGGPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                KEGGBindingStub _stub = new KEGGBindingStub(new java.net.URL(KEGGPort_address), this);
                _stub.setPortName(getKEGGPortWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("KEGGPort".equals(inputPortName)) {
            return getKEGGPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("SOAP/KEGG", "KEGG");
    }

    private HashSet<QName> ports = null;

    public Iterator<QName> getPorts() {
        if (ports == null) {
            ports = new HashSet<QName>();
            ports.add(new QName("SOAP/KEGG", "KEGGPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        
if ("KEGGPort".equals(portName)) {
            setKEGGPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }
}

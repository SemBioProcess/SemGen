/**
 * KEGGLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package semgen.webservices.KEGG;

<<<<<<< HEAD
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.Remote;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.client.Service;
import org.apache.axis.client.Stub;

public class KEGGLocator extends Service implements KEGG {

	private static final long serialVersionUID = 1L;

	public KEGGLocator() {
    }


    public KEGGLocator(EngineConfiguration config) {
        super(config);
    }

    public KEGGLocator(String wsdlLoc, QName sName) throws ServiceException {
=======
import java.net.URL;

import javax.xml.rpc.ServiceException;

public class KEGGLocator extends org.apache.axis.client.Service implements KEGG {

    public KEGGLocator() {
    }


    public KEGGLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public KEGGLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for KEGGPort
    private java.lang.String KEGGPort_address = "http://soap.genome.jp/keggapi/request_v6.2.cgi";

    public java.lang.String getKEGGPortAddress() {
        return KEGGPort_address;
    }

    // The WSDD service name defaults to the port name.
<<<<<<< HEAD
    private String KEGGPortWSDDServiceName = "KEGGPort";

    public String getKEGGPortWSDDServiceName() {
        return KEGGPortWSDDServiceName;
    }

    public void setKEGGPortWSDDServiceName(String name) {
        KEGGPortWSDDServiceName = name;
    }

    public KEGGPortType getKEGGPort() throws ServiceException {
       URL endpoint;
        try {
            endpoint = new URL(KEGGPort_address);
        }
        catch (MalformedURLException e) {
            throw new ServiceException(e);
=======
    private java.lang.String KEGGPortWSDDServiceName = "KEGGPort";

    public java.lang.String getKEGGPortWSDDServiceName() {
        return KEGGPortWSDDServiceName;
    }

    public void setKEGGPortWSDDServiceName(java.lang.String name) {
        KEGGPortWSDDServiceName = name;
    }

    public KEGGPortType getKEGGPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(KEGGPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        }
        return getKEGGPort(endpoint);
    }

<<<<<<< HEAD
    public KEGGPortType getKEGGPort(URL portAddress) throws ServiceException {
=======
    public KEGGPortType getKEGGPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        try {
            KEGGBindingStub _stub = new KEGGBindingStub(portAddress, this);
            _stub.setPortName(getKEGGPortWSDDServiceName());
            return _stub;
        }
<<<<<<< HEAD
        catch (AxisFault e) {
=======
        catch (org.apache.axis.AxisFault e) {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            return null;
        }
    }

    public void setKEGGPortEndpointAddress(java.lang.String address) {
        KEGGPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
<<<<<<< HEAD
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws ServiceException {
        try {
            if (KEGGPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                KEGGBindingStub _stub = new KEGGBindingStub(new URL(KEGGPort_address), this);
=======
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (KEGGPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                KEGGBindingStub _stub = new KEGGBindingStub(new java.net.URL(KEGGPort_address), this);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
                _stub.setPortName(getKEGGPortWSDDServiceName());
                return _stub;
            }
        }
<<<<<<< HEAD
        catch (Throwable t) {
            throw new ServiceException(t);
        }
        throw new ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
=======
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
<<<<<<< HEAD
    public Remote getPort(QName portName, Class serviceEndpointInterface) throws ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
=======
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        if ("KEGGPort".equals(inputPortName)) {
            return getKEGGPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
<<<<<<< HEAD
            ((Stub) _stub).setPortName(portName);
=======
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
            return _stub;
        }
    }

<<<<<<< HEAD
    public QName getServiceName() {
        return new QName("SOAP/KEGG", "KEGG");
    }

    private HashSet<QName> ports = null;

    public Iterator<QName> getPorts() {
        if (ports == null) {
            ports = new HashSet<QName>();
            ports.add(new QName("SOAP/KEGG", "KEGGPort"));
=======
    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("SOAP/KEGG", "KEGG");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("SOAP/KEGG", "KEGGPort"));
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
<<<<<<< HEAD
    public void setEndpointAddress(String portName, String address) throws ServiceException {
=======
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        
if ("KEGGPort".equals(portName)) {
            setKEGGPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
<<<<<<< HEAD
            throw new ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
=======
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
<<<<<<< HEAD
    public void setEndpointAddress(QName portName, String address) throws ServiceException {
=======
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
>>>>>>> 2eb394907b98577f1b916408cf22a2de6952b22d
        setEndpointAddress(portName.getLocalPart(), address);
    }
}

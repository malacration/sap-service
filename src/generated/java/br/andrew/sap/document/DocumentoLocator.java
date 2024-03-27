/**
 * DocumentoLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DocumentoLocator extends org.apache.axis.client.Service implements br.andrew.sap.document.Documento {

    public DocumentoLocator() {
    }


    public DocumentoLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DocumentoLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DocumentoPort
    private java.lang.String DocumentoPort_address = "https://se.gruporovema.com.br/apigateway/se/ws/dc_ws.php";

    public java.lang.String getDocumentoPortAddress() {
        return DocumentoPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DocumentoPortWSDDServiceName = "DocumentoPort";

    public java.lang.String getDocumentoPortWSDDServiceName() {
        return DocumentoPortWSDDServiceName;
    }

    public void setDocumentoPortWSDDServiceName(java.lang.String name) {
        DocumentoPortWSDDServiceName = name;
    }

    public br.andrew.sap.document.DocumentoPortType getDocumentoPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DocumentoPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDocumentoPort(endpoint);
    }

    public br.andrew.sap.document.DocumentoPortType getDocumentoPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            br.andrew.sap.document.DocumentoBindingStub _stub = new br.andrew.sap.document.DocumentoBindingStub(portAddress, this);
            _stub.setPortName(getDocumentoPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDocumentoPortEndpointAddress(java.lang.String address) {
        DocumentoPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (br.andrew.sap.document.DocumentoPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                br.andrew.sap.document.DocumentoBindingStub _stub = new br.andrew.sap.document.DocumentoBindingStub(new java.net.URL(DocumentoPort_address), this);
                _stub.setPortName(getDocumentoPortWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
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
        java.lang.String inputPortName = portName.getLocalPart();
        if ("DocumentoPort".equals(inputPortName)) {
            return getDocumentoPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:document", "Documento");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:document", "DocumentoPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DocumentoPort".equals(portName)) {
            setDocumentoPortEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}

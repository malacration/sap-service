/**
 * WorkflowLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class WorkflowLocator extends org.apache.axis.client.Service implements br.andrew.sap.workflow.Workflow {

    public WorkflowLocator() {
    }


    public WorkflowLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WorkflowLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WorkflowPort
    private java.lang.String WorkflowPort_address = "https://se.gruporovema.com.br/apigateway/se/ws/wf_ws.php";

    public java.lang.String getWorkflowPortAddress() {
        return WorkflowPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WorkflowPortWSDDServiceName = "WorkflowPort";

    public java.lang.String getWorkflowPortWSDDServiceName() {
        return WorkflowPortWSDDServiceName;
    }

    public void setWorkflowPortWSDDServiceName(java.lang.String name) {
        WorkflowPortWSDDServiceName = name;
    }

    public br.andrew.sap.workflow.WorkflowPortType getWorkflowPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WorkflowPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWorkflowPort(endpoint);
    }

    public br.andrew.sap.workflow.WorkflowPortType getWorkflowPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            br.andrew.sap.workflow.WorkflowBindingStub _stub = new br.andrew.sap.workflow.WorkflowBindingStub(portAddress, this);
            _stub.setPortName(getWorkflowPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWorkflowPortEndpointAddress(java.lang.String address) {
        WorkflowPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (br.andrew.sap.workflow.WorkflowPortType.class.isAssignableFrom(serviceEndpointInterface)) {
                br.andrew.sap.workflow.WorkflowBindingStub _stub = new br.andrew.sap.workflow.WorkflowBindingStub(new java.net.URL(WorkflowPort_address), this);
                _stub.setPortName(getWorkflowPortWSDDServiceName());
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
        if ("WorkflowPort".equals(inputPortName)) {
            return getWorkflowPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:workflow", "Workflow");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:workflow", "WorkflowPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WorkflowPort".equals(portName)) {
            setWorkflowPortEndpointAddress(address);
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

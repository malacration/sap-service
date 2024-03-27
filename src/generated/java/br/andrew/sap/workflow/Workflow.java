/**
 * Workflow.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public interface Workflow extends javax.xml.rpc.Service {
    public java.lang.String getWorkflowPortAddress();

    public br.andrew.sap.workflow.WorkflowPortType getWorkflowPort() throws javax.xml.rpc.ServiceException;

    public br.andrew.sap.workflow.WorkflowPortType getWorkflowPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

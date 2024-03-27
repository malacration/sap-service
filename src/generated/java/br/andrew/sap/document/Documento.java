/**
 * Documento.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public interface Documento extends javax.xml.rpc.Service {
    public java.lang.String getDocumentoPortAddress();

    public br.andrew.sap.document.DocumentoPortType getDocumentoPort() throws javax.xml.rpc.ServiceException;

    public br.andrew.sap.document.DocumentoPortType getDocumentoPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}

/**
 * DocumentoBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DocumentoBindingStub extends org.apache.axis.client.Stub implements br.andrew.sap.document.DocumentoPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[35];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
        _initOperationDesc4();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newCaptureInstance");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newCaptureInstance"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newCaptureInstanceRequestType"), br.andrew.sap.document.NewCaptureInstanceRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newCaptureInstanceResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewCaptureInstanceResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newCaptureInstanceResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "cancelDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "cancelDocumentRequestType"), br.andrew.sap.document.CancelDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "cancelDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.CancelDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "cancelDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("checkAccessPermission");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "checkAccessPermission"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "checkAccessPermissionRequestType"), br.andrew.sap.document.CheckAccessPermissionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "checkAccessPermissionResponseType"));
        oper.setReturnClass(br.andrew.sap.document.CheckAccessPermissionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "checkAccessPermissionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteAccessPermission");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "deleteAccessPermission"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "deleteAccessPermissionRequestType"), br.andrew.sap.document.DeleteAccessPermissionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "deleteAccessPermissionResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DeleteAccessPermissionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "deleteAccessPermissionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "deleteDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "deleteDocumentRequestType"), br.andrew.sap.document.DeleteDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "deleteDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DeleteDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "deleteDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteRevision");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "deleteRevision"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "deleteRevisionRequestType"), br.andrew.sap.document.DeleteRevisionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "deleteRevisionResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DeleteRevisionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "deleteRevisionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("downloadEletronicFile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "downloadEletronicFile"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "downloadEletronicFileRequestType"), br.andrew.sap.document.DownloadEletronicFileRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "downloadEletronicFileResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DownloadEletronicFileResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "downloadEletronicFileResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "editDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "editDocumentRequestType"), br.andrew.sap.document.EditDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "editDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.EditDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "editDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getKeyWords");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "getKeyWords"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "getKeyWordsRequestType"), br.andrew.sap.document.GetKeyWordsRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "getKeyWordsResponseType"));
        oper.setReturnClass(br.andrew.sap.document.GetKeyWordsResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "getKeyWordsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getKeyWordsData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "getKeyWordsData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "getKeyWordsDataRequestType"), br.andrew.sap.document.GetKeyWordsDataRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "getKeyWordsDataResponseType"));
        oper.setReturnClass(br.andrew.sap.document.GetKeyWordsDataResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "getKeyWordsDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("listAccessPermission");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "listAccessPermission"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "listAccessPermissionRequestType"), br.andrew.sap.document.ListAccessPermissionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "listAccessPermissionResponseType"));
        oper.setReturnClass(br.andrew.sap.document.ListAccessPermissionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "listAccessPermissionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("listPendencyDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "listPendencyDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "listPendencyDocumentRequestType"), br.andrew.sap.document.ListPendencyDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "listPendencyDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.ListPendencyDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "listPendencyDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newAccessPermission");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newAccessPermission"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newAccessPermissionRequestType"), br.andrew.sap.document.NewAccessPermissionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newAccessPermissionResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewAccessPermissionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newAccessPermissionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newDocumentRequestType"), br.andrew.sap.document.NewDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newDocument2");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newDocument2"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newDocument2RequestType"), br.andrew.sap.document.NewDocument2RequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newDocument2ResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewDocument2ResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newDocument2Response"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("shareDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "shareDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "shareDocumentRequestType"), br.andrew.sap.document.ShareDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "shareDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.ShareDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "shareDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newRevision");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newRevision"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newRevisionRequestType"), br.andrew.sap.document.NewRevisionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newRevisionResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewRevisionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newRevisionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("releasePendencyDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "releasePendencyDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentRequestType"), br.andrew.sap.document.ReleasePendencyDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.ReleasePendencyDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("searchAccessAudit");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "searchAccessAudit"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "searchAccessAuditRequestType"), br.andrew.sap.document.SearchAccessAuditRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "searchAccessAuditResponseType"));
        oper.setReturnClass(br.andrew.sap.document.SearchAccessAuditResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "searchAccessAuditResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("searchCategory");
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "searchCategoryResponseType"));
        oper.setReturnClass(br.andrew.sap.document.SearchCategoryResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "searchCategoryResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("searchDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "searchDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "searchDocumentRequestType"), br.andrew.sap.document.SearchDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "searchDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.document.SearchDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "searchDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("setAttributeValue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "setAttributeValue"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "setAttributeValueRequestType"), br.andrew.sap.document.SetAttributeValueRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "setAttributeValueResponseType"));
        oper.setReturnClass(br.andrew.sap.document.SetAttributeValueResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "setAttributeValueResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("uploadEletronicFile");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "uploadEletronicFile"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "uploadEletronicFileRequestType"), br.andrew.sap.document.UploadEletronicFileRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "uploadEletronicFileResponseType"));
        oper.setReturnClass(br.andrew.sap.document.UploadEletronicFileResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "uploadEletronicFileResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("viewDocumentData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "viewDocumentData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "viewDocumentDataRequestType"), br.andrew.sap.document.ViewDocumentDataRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "viewDocumentDataResponseType"));
        oper.setReturnClass(br.andrew.sap.document.ViewDocumentDataResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "viewDocumentDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newDocumentContainerAssociation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociationRequestType"), br.andrew.sap.document.NewDocumentContainerAssociationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewDocumentContainerAssociationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[24] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteDocumentContainerAssociation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociationRequestType"), br.andrew.sap.document.DeleteDocumentContainerAssociationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[25] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("associateProcess");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "associateProcess"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "associateProcessRequestType"), br.andrew.sap.document.AssociateProcessRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "associateProcessResponseType"));
        oper.setReturnClass(br.andrew.sap.document.AssociateProcessResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "associateProcessResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[26] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("changeCopyStationStatus");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "changeCopyStationStatus"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "changeCopyStationStatusRequestType"), br.andrew.sap.document.ChangeCopyStationStatusRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "changeCopyStationStatusResponseType"));
        oper.setReturnClass(br.andrew.sap.document.ChangeCopyStationStatusResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "changeCopyStationStatusResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[27] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newCopyStation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newCopyStation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newCopyStationRequestType"), br.andrew.sap.document.NewCopyStationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newCopyStationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewCopyStationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newCopyStationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[28] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editCopyStation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "editCopyStation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "editCopyStationRequestType"), br.andrew.sap.document.EditCopyStationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "editCopyStationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.EditCopyStationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "editCopyStationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[29] = oper;

    }

    private static void _initOperationDesc4(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("relateCopyStation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "relateCopyStation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "relateCopyStationRequestType"), br.andrew.sap.document.RelateCopyStationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "relateCopyStationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.RelateCopyStationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "relateCopyStationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[30] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteCopyStation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "deleteCopyStation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "deleteCopyStationRequestType"), br.andrew.sap.document.DeleteCopyStationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "deleteCopyStationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DeleteCopyStationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "deleteCopyStationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[31] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newCopyStationResp");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newCopyStationResp"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newCopyStationRespRequestType"), br.andrew.sap.document.NewCopyStationRespRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newCopyStationRespResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewCopyStationRespResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newCopyStationRespResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[32] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteRespCopyStation");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "deleteRespCopyStation"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "deleteRespCopyStationRequestType"), br.andrew.sap.document.DeleteRespCopyStationRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "deleteRespCopyStationResponseType"));
        oper.setReturnClass(br.andrew.sap.document.DeleteRespCopyStationResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "deleteRespCopyStationResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[33] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newProtocolPrintedCopy");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopy"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopyRequestType"), br.andrew.sap.document.NewProtocolPrintedCopyRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopyResponseType"));
        oper.setReturnClass(br.andrew.sap.document.NewProtocolPrintedCopyResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopyResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[34] = oper;

    }

    public DocumentoBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public DocumentoBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public DocumentoBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
        addBindings0();
        addBindings1();
    }

    private void addBindings0() {
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("urn:document", "arrayOfAttribute");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AttributeData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "attributeData");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "ArrayOfstring");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("urn:document", "document");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "associateProcessRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AssociateProcessRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "associateProcessResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AssociateProcessResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "attribData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AttribData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "attribItem");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AttribValue[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "attribValue");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "attributeData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AttributeData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "AttributesArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AttribData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "attribData");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "attributtes");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.Attributtes.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "attributtesArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.Attributtes[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "attributtes");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "attribValue");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.AttribValue.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "cancelDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.CancelDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "cancelDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.CancelDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "categoryReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.CategoryReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "changeCopyStationStatusRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ChangeCopyStationStatusRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "changeCopyStationStatusResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ChangeCopyStationStatusResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "checkAccessPermissionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.CheckAccessPermissionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "checkAccessPermissionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.CheckAccessPermissionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteAccessPermissionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteAccessPermissionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteAccessPermissionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteAccessPermissionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteCopyStationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteCopyStationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteCopyStationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteCopyStationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteDocumentContainerAssociationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteRespCopyStationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteRespCopyStationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteRespCopyStationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteRespCopyStationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteRevisionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteRevisionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "deleteRevisionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DeleteRevisionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "documentData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DocumentData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "documentDataReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DocumentDataReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "documentReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DocumentReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "DocumentsArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DocumentsData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "documentsData");
            qName2 = new javax.xml.namespace.QName("urn:document", "Documents");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "documentsData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DocumentsData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "downloadEletronicFileRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DownloadEletronicFileRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "downloadEletronicFileResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DownloadEletronicFileResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "editCopyStationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.EditCopyStationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "editCopyStationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.EditCopyStationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "editDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.EditDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "editDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.EditDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "electronicFile");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ElectronicFile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "electronicFileArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ElectronicFile[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "electronicFile");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "eletronicFile");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.EletronicFile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "eletronicFileArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.EletronicFile[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "eletronicFile");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "File");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.File.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "FileArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.File[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "File");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "getKeyWordsDataRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.GetKeyWordsDataRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "getKeyWordsDataResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.GetKeyWordsDataResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "getKeyWordsRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.GetKeyWordsRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "getKeyWordsResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.GetKeyWordsResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "keyWordArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.KeyWordData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "keyWordData");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "keyWordData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.KeyWordData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "listAccessPermissionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListAccessPermissionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "listAccessPermissionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListAccessPermissionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "listAuditSystem");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListAuditSystem.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "listAuditSystemArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListAuditSystem[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "listAuditSystem");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "listPendencyDocumentArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListPendencyDocumentReturn[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "listPendencyDocumentReturn");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "listPendencyDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListPendencyDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "listPendencyDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListPendencyDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "listPendencyDocumentReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ListPendencyDocumentReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "MembersData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.MembersData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newAccessPermissionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewAccessPermissionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newAccessPermissionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewAccessPermissionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newCaptureInstanceRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewCaptureInstanceRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newCaptureInstanceResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewCaptureInstanceResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newCopyStationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewCopyStationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newCopyStationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewCopyStationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newCopyStationRespRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewCopyStationRespRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newCopyStationRespResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewCopyStationRespResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newDocument2RequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewDocument2RequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newDocument2ResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewDocument2ResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewDocumentContainerAssociationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewDocumentContainerAssociationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopyRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewProtocolPrintedCopyRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopyResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewProtocolPrintedCopyResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newRevisionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewRevisionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "newRevisionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.NewRevisionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "participantsArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ParticipantsData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "participantsData");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "participantsData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ParticipantsData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "relateCopyStationRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.RelateCopyStationRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "relateCopyStationResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.RelateCopyStationResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ReleasePendencyDocumentReturn[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentReturn");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ReleasePendencyDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ReleasePendencyDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "releasePendencyDocumentReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ReleasePendencyDocumentReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "RevisionMembersArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.MembersData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "MembersData");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "searchAccessAuditRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchAccessAuditRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchAccessAuditResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchAccessAuditResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchCategoryArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.CategoryReturn[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "categoryReturn");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "searchCategoryResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchCategoryResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchCategoryReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchCategoryReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchDocumentFilter");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchDocumentFilter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchDocumentReturn");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SearchDocumentReturn.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "searchResultsArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.DocumentReturn[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "documentReturn");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:document", "setAttributeValueRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SetAttributeValueRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }
    private void addBindings1() {
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("urn:document", "setAttributeValueResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.SetAttributeValueResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "shareDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ShareDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "shareDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ShareDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "uploadEletronicFileRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.UploadEletronicFileRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "uploadEletronicFileResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.UploadEletronicFileResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "viewDocumentDataRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ViewDocumentDataRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "viewDocumentDataResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.ViewDocumentDataResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "wordProperties");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.WordProperties.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:document", "wordPropertiesArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.document.WordProperties[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:document", "wordProperties");
            qName2 = new javax.xml.namespace.QName("urn:document", "item");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public br.andrew.sap.document.NewCaptureInstanceResponseType newCaptureInstance(br.andrew.sap.document.NewCaptureInstanceRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newCaptureInstance");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newCaptureInstance"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewCaptureInstanceResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewCaptureInstanceResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewCaptureInstanceResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.CancelDocumentResponseType cancelDocument(br.andrew.sap.document.CancelDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#cancelDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "cancelDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.CancelDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.CancelDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.CancelDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.CheckAccessPermissionResponseType checkAccessPermission(br.andrew.sap.document.CheckAccessPermissionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#checkAccessPermission");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "checkAccessPermission"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.CheckAccessPermissionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.CheckAccessPermissionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.CheckAccessPermissionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DeleteAccessPermissionResponseType deleteAccessPermission(br.andrew.sap.document.DeleteAccessPermissionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#deleteAccessPermission");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "deleteAccessPermission"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DeleteAccessPermissionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DeleteAccessPermissionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DeleteAccessPermissionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DeleteDocumentResponseType deleteDocument(br.andrew.sap.document.DeleteDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#deleteDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "deleteDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DeleteDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DeleteDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DeleteDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DeleteRevisionResponseType deleteRevision(br.andrew.sap.document.DeleteRevisionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#deleteRevision");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "deleteRevision"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DeleteRevisionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DeleteRevisionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DeleteRevisionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DownloadEletronicFileResponseType downloadEletronicFile(br.andrew.sap.document.DownloadEletronicFileRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#downloadEletronicFile");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "downloadEletronicFile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DownloadEletronicFileResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DownloadEletronicFileResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DownloadEletronicFileResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.EditDocumentResponseType editDocument(br.andrew.sap.document.EditDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#editDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "editDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.EditDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.EditDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.EditDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.GetKeyWordsResponseType getKeyWords(br.andrew.sap.document.GetKeyWordsRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#getKeyWords");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "getKeyWords"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.GetKeyWordsResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.GetKeyWordsResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.GetKeyWordsResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.GetKeyWordsDataResponseType getKeyWordsData(br.andrew.sap.document.GetKeyWordsDataRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#getKeyWordsData");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "getKeyWordsData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.GetKeyWordsDataResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.GetKeyWordsDataResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.GetKeyWordsDataResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.ListAccessPermissionResponseType listAccessPermission(br.andrew.sap.document.ListAccessPermissionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#listAccessPermission");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "listAccessPermission"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.ListAccessPermissionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.ListAccessPermissionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.ListAccessPermissionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.ListPendencyDocumentResponseType listPendencyDocument(br.andrew.sap.document.ListPendencyDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#listPendencyDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "listPendencyDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.ListPendencyDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.ListPendencyDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.ListPendencyDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewAccessPermissionResponseType newAccessPermission(br.andrew.sap.document.NewAccessPermissionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newAccessPermission");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newAccessPermission"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewAccessPermissionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewAccessPermissionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewAccessPermissionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewDocumentResponseType newDocument(br.andrew.sap.document.NewDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewDocument2ResponseType newDocument2(br.andrew.sap.document.NewDocument2RequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newDocument2");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newDocument2"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewDocument2ResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewDocument2ResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewDocument2ResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.ShareDocumentResponseType shareDocument(br.andrew.sap.document.ShareDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#shareDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "shareDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.ShareDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.ShareDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.ShareDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewRevisionResponseType newRevision(br.andrew.sap.document.NewRevisionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newRevision");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newRevision"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewRevisionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewRevisionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewRevisionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.ReleasePendencyDocumentResponseType releasePendencyDocument(br.andrew.sap.document.ReleasePendencyDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#releasePendencyDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "releasePendencyDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.ReleasePendencyDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.ReleasePendencyDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.ReleasePendencyDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.SearchAccessAuditResponseType searchAccessAudit(br.andrew.sap.document.SearchAccessAuditRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#searchAccessAudit");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "searchAccessAudit"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.SearchAccessAuditResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.SearchAccessAuditResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.SearchAccessAuditResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.SearchCategoryResponseType searchCategory() throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#searchCategory");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "searchCategory"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.SearchCategoryResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.SearchCategoryResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.SearchCategoryResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.SearchDocumentResponseType searchDocument(br.andrew.sap.document.SearchDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#searchDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "searchDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.SearchDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.SearchDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.SearchDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.SetAttributeValueResponseType setAttributeValue(br.andrew.sap.document.SetAttributeValueRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#setAttributeValue");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "setAttributeValue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.SetAttributeValueResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.SetAttributeValueResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.SetAttributeValueResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.UploadEletronicFileResponseType uploadEletronicFile(br.andrew.sap.document.UploadEletronicFileRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#uploadEletronicFile");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "uploadEletronicFile"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.UploadEletronicFileResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.UploadEletronicFileResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.UploadEletronicFileResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.ViewDocumentDataResponseType viewDocumentData(br.andrew.sap.document.ViewDocumentDataRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#viewDocumentData");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "viewDocumentData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.ViewDocumentDataResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.ViewDocumentDataResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.ViewDocumentDataResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewDocumentContainerAssociationResponseType newDocumentContainerAssociation(br.andrew.sap.document.NewDocumentContainerAssociationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newDocumentContainerAssociation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newDocumentContainerAssociation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewDocumentContainerAssociationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewDocumentContainerAssociationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewDocumentContainerAssociationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType deleteDocumentContainerAssociation(br.andrew.sap.document.DeleteDocumentContainerAssociationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[25]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#deleteDocumentContainerAssociation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "deleteDocumentContainerAssociation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DeleteDocumentContainerAssociationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.AssociateProcessResponseType associateProcess(br.andrew.sap.document.AssociateProcessRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[26]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#associateProcess");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "associateProcess"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.AssociateProcessResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.AssociateProcessResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.AssociateProcessResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.ChangeCopyStationStatusResponseType changeCopyStationStatus(br.andrew.sap.document.ChangeCopyStationStatusRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[27]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#changeCopyStationStatus");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "changeCopyStationStatus"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.ChangeCopyStationStatusResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.ChangeCopyStationStatusResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.ChangeCopyStationStatusResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewCopyStationResponseType newCopyStation(br.andrew.sap.document.NewCopyStationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[28]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newCopyStation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newCopyStation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewCopyStationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewCopyStationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewCopyStationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.EditCopyStationResponseType editCopyStation(br.andrew.sap.document.EditCopyStationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[29]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#editCopyStation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "editCopyStation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.EditCopyStationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.EditCopyStationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.EditCopyStationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.RelateCopyStationResponseType relateCopyStation(br.andrew.sap.document.RelateCopyStationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[30]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#relateCopyStation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "relateCopyStation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.RelateCopyStationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.RelateCopyStationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.RelateCopyStationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DeleteCopyStationResponseType deleteCopyStation(br.andrew.sap.document.DeleteCopyStationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[31]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#deleteCopyStation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "deleteCopyStation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DeleteCopyStationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DeleteCopyStationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DeleteCopyStationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewCopyStationRespResponseType newCopyStationResp(br.andrew.sap.document.NewCopyStationRespRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[32]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newCopyStationResp");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newCopyStationResp"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewCopyStationRespResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewCopyStationRespResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewCopyStationRespResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.DeleteRespCopyStationResponseType deleteRespCopyStation(br.andrew.sap.document.DeleteRespCopyStationRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[33]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#deleteRespCopyStation");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "deleteRespCopyStation"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.DeleteRespCopyStationResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.DeleteRespCopyStationResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.DeleteRespCopyStationResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.document.NewProtocolPrintedCopyResponseType newProtocolPrintedCopy(br.andrew.sap.document.NewProtocolPrintedCopyRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[34]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:document#newProtocolPrintedCopy");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:document", "newProtocolPrintedCopy"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.document.NewProtocolPrintedCopyResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.document.NewProtocolPrintedCopyResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.document.NewProtocolPrintedCopyResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}

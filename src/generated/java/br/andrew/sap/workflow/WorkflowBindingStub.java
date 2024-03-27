/**
 * WorkflowBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.workflow;

public class WorkflowBindingStub extends org.apache.axis.client.Stub implements br.andrew.sap.workflow.WorkflowPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[22];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newWorkflow");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newWorkflow"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newWorkflowRequestType"), br.andrew.sap.workflow.NewWorkflowRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newWorkflowResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewWorkflowResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newWorkflowResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newWorkflowEditData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditDataRequestType"), br.andrew.sap.workflow.NewWorkflowEditDataRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditDataResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewWorkflowEditDataResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newWorkflowTypeEditData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditDataRequestType"), br.andrew.sap.workflow.NewWorkflowTypeEditDataRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditDataResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editWorkflowData");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "editWorkflowData"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "editWorkflowDataRequestType"), br.andrew.sap.workflow.EditWorkflowDataRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "editWorkflowDataResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.EditWorkflowDataResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "editWorkflowDataResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editEntityRecord");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "editEntityRecord"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "editEntityRecordRequestType"), br.andrew.sap.workflow.EditEntityRecordRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "editEntityRecordResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.EditEntityRecordResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "editEntityRecordResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newChildEntityRecord");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecord"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordRequestType"), br.andrew.sap.workflow.NewChildEntityRecordRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewChildEntityRecordResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newChildEntityRecordList");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordList"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordListRequestType"), br.andrew.sap.workflow.NewChildEntityRecordListRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordListResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewChildEntityRecordListResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordListResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editChildEntityRecord");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecord"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecordRequestType"), br.andrew.sap.workflow.EditChildEntityRecordRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecordResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.EditChildEntityRecordResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecordResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("deleteChildEntityRecord");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecord"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecordRequestType"), br.andrew.sap.workflow.DeleteChildEntityRecordRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecordResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.DeleteChildEntityRecordResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecordResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("clearChildEntityRecord");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecord"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecordRequestType"), br.andrew.sap.workflow.ClearChildEntityRecordRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecordResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.ClearChildEntityRecordResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecordResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("editAttributeValue");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "editAttributeValue"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "editAttributeValueRequestType"), br.andrew.sap.workflow.EditAttributeValueRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "editAttributeValueResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.EditAttributeValueResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "editAttributeValueResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newAttachment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newAttachment"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newAttachmentRequestType"), br.andrew.sap.workflow.NewAttachmentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newAttachmentResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewAttachmentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newAttachmentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newAssocDocument");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newAssocDocument"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newAssocDocumentRequestType"), br.andrew.sap.workflow.NewAssocDocumentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newAssocDocumentResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewAssocDocumentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newAssocDocumentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newAssocWorkflow");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflow"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflowRequestType"), br.andrew.sap.workflow.NewAssocWorkflowRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflowResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewAssocWorkflowResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflowResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("executeSystemActivity");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "executeSystemActivity"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "executeSystemActivityRequestType"), br.andrew.sap.workflow.ExecuteSystemActivityRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "executeSystemActivityResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.ExecuteSystemActivityResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "executeSystemActivityResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("executeActivity");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "executeActivity"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "executeActivityRequestType"), br.andrew.sap.workflow.ExecuteActivityRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "executeActivityResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.ExecuteActivityResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "executeActivityResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newAccessException");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newAccessException"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newAccessExceptionRequestType"), br.andrew.sap.workflow.NewAccessExceptionRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newAccessExceptionResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewAccessExceptionResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newAccessExceptionResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("cancelWorkflow");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "cancelWorkflow"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "cancelWorkflowRequestType"), br.andrew.sap.workflow.CancelWorkflowRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "cancelWorkflowResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.CancelWorkflowResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "cancelWorkflowResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getWorkflow");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "getWorkflow"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "getWorkflowRequestType"), br.andrew.sap.workflow.GetWorkflowRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "getWorkflowResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.GetWorkflowResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "getWorkflowResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("unlinkActivityFromUser");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUser"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUserRequestType"), br.andrew.sap.workflow.UnlinkActivityFromUserRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUserResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.UnlinkActivityFromUserResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUserResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("newComment");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "newComment"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "newCommentRequestType"), br.andrew.sap.workflow.NewCommentRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "newCommentResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.NewCommentResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "newCommentResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("reactivateWorkflow");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflow"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowRequestType"), br.andrew.sap.workflow.ReactivateWorkflowRequestType.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowResponseType"));
        oper.setReturnClass(br.andrew.sap.workflow.ReactivateWorkflowResponseType.class);
        oper.setReturnQName(new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[21] = oper;

    }

    public WorkflowBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public WorkflowBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public WorkflowBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
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
            qName = new javax.xml.namespace.QName("urn:workflow", "accessExceptionArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AccessExceptionData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "accessExceptionData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "AccessException");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "accessExceptionData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AccessExceptionData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "assocWorkflowArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AssocWorkflowData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "assocWorkflowData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "AssocWorkflow");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "assocWorkflowData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AssocWorkflowData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "attributeArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeArray.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "attributeData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "attributeFileData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeFileData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "AttributeListArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeArray[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "attributeArray");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "Attribute");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "attributeValueData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeValueData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "cancelWorkflowRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.CancelWorkflowRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "cancelWorkflowResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.CancelWorkflowResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecordRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ClearChildEntityRecordRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecordResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ClearChildEntityRecordResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecordRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.DeleteChildEntityRecordRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecordResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.DeleteChildEntityRecordResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editAttributeValueRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditAttributeValueRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editAttributeValueResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditAttributeValueResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecordRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditChildEntityRecordRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecordResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditChildEntityRecordResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editEntityRecordRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditEntityRecordRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editEntityRecordResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditEntityRecordResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editWorkflowDataRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditWorkflowDataRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "editWorkflowDataResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EditWorkflowDataResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "EntityArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EntityArray.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "entityAttributeArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "attributeData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "EntityAttribute");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "entityAttributeFileArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.AttributeFileData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "attributeFileData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "EntityAttributeFile");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "EntityListArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EntityArray[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "EntityArray");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "Entity");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "EntityRecordArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EntityRecordArray.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "EntityRecordListArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.EntityRecordArray[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "EntityRecordArray");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "EntityRecord");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "executeActivityRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ExecuteActivityRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "executeActivityResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ExecuteActivityResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "executeSystemActivityRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ExecuteSystemActivityRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "executeSystemActivityResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ExecuteSystemActivityResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "getWorkflowRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.GetWorkflowRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "getWorkflowResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.GetWorkflowResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAccessExceptionRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAccessExceptionRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAccessExceptionResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAccessExceptionResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAssocDocumentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAssocDocumentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAssocDocumentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAssocDocumentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflowRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAssocWorkflowRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflowResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAssocWorkflowResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAttachmentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAttachmentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newAttachmentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewAttachmentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordListRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewChildEntityRecordListRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordListResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewChildEntityRecordListResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewChildEntityRecordRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewChildEntityRecordResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newCommentRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewCommentRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newCommentResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewCommentResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditDataRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewWorkflowEditDataRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditDataResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewWorkflowEditDataResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newWorkflowRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewWorkflowRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newWorkflowResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewWorkflowResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditDataRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewWorkflowTypeEditDataRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditDataResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ReactivateWorkflowRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflowResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.ReactivateWorkflowResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "RecordData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RecordData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "recordDetailArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RecordDetailData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "recordDetailData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "RecordDetail");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "recordDetailData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RecordDetailData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "RecordListArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RecordData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "RecordData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "Record");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "relationshipArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RelationshipArray.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "RelationshipArrayList");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RelationshipArrayList.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "relationshipAttributeData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RelationshipAttributeData.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "RelationshipAttributeListArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RelationshipArrayList[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "RelationshipArrayList");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "Relationship");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "relationshipListArray");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RelationshipArray[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "relationshipArray");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "Relationship");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "RelationshipListAttributeData");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RelationshipAttributeData[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("urn:workflow", "relationshipAttributeData");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "RelationshipAttribute");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "Requester");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.Requester.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "RequesterAsCustomer");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RequesterAsCustomer.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "RequesterAsUser");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.RequesterAsUser.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUserRequestType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.UnlinkActivityFromUserRequestType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUserResponseType");
            cachedSerQNames.add(qName);
            cls = br.andrew.sap.workflow.UnlinkActivityFromUserResponseType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("urn:workflow", "workflowArray");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "WorkflowID");
            cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
            cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());

            qName = new javax.xml.namespace.QName("urn:workflow", "workflowsResultArray");
            cachedSerQNames.add(qName);
            cls = java.lang.String[].class;
            cachedSerClasses.add(cls);
            qName = new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string");
            qName2 = new javax.xml.namespace.QName("urn:workflow", "WorkflowID");
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

    public br.andrew.sap.workflow.NewWorkflowResponseType newWorkflow(br.andrew.sap.workflow.NewWorkflowRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newWorkflow");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newWorkflow"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewWorkflowResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewWorkflowResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewWorkflowResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewWorkflowEditDataResponseType newWorkflowEditData(br.andrew.sap.workflow.NewWorkflowEditDataRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newWorkflowEditData");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newWorkflowEditData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewWorkflowEditDataResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewWorkflowEditDataResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewWorkflowEditDataResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType newWorkflowTypeEditData(br.andrew.sap.workflow.NewWorkflowTypeEditDataRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newWorkflowTypeEditData");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newWorkflowTypeEditData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewWorkflowTypeEditDataResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.EditWorkflowDataResponseType editWorkflowData(br.andrew.sap.workflow.EditWorkflowDataRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#editWorkflowData");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "editWorkflowData"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.EditWorkflowDataResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.EditWorkflowDataResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.EditWorkflowDataResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.EditEntityRecordResponseType editEntityRecord(br.andrew.sap.workflow.EditEntityRecordRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#editEntityRecord");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "editEntityRecord"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.EditEntityRecordResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.EditEntityRecordResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.EditEntityRecordResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewChildEntityRecordResponseType newChildEntityRecord(br.andrew.sap.workflow.NewChildEntityRecordRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newChildEntityRecord");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecord"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewChildEntityRecordResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewChildEntityRecordResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewChildEntityRecordResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewChildEntityRecordListResponseType newChildEntityRecordList(br.andrew.sap.workflow.NewChildEntityRecordListRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newChildEntityRecordList");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newChildEntityRecordList"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewChildEntityRecordListResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewChildEntityRecordListResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewChildEntityRecordListResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.EditChildEntityRecordResponseType editChildEntityRecord(br.andrew.sap.workflow.EditChildEntityRecordRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#editChildEntityRecord");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "editChildEntityRecord"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.EditChildEntityRecordResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.EditChildEntityRecordResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.EditChildEntityRecordResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.DeleteChildEntityRecordResponseType deleteChildEntityRecord(br.andrew.sap.workflow.DeleteChildEntityRecordRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#deleteChildEntityRecord");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "deleteChildEntityRecord"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.DeleteChildEntityRecordResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.DeleteChildEntityRecordResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.DeleteChildEntityRecordResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.ClearChildEntityRecordResponseType clearChildEntityRecord(br.andrew.sap.workflow.ClearChildEntityRecordRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#clearChildEntityRecord");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "clearChildEntityRecord"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.ClearChildEntityRecordResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.ClearChildEntityRecordResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.ClearChildEntityRecordResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.EditAttributeValueResponseType editAttributeValue(br.andrew.sap.workflow.EditAttributeValueRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#editAttributeValue");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "editAttributeValue"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.EditAttributeValueResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.EditAttributeValueResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.EditAttributeValueResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewAttachmentResponseType newAttachment(br.andrew.sap.workflow.NewAttachmentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newAttachment");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newAttachment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewAttachmentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewAttachmentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewAttachmentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewAssocDocumentResponseType newAssocDocument(br.andrew.sap.workflow.NewAssocDocumentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newAssocDocument");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newAssocDocument"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewAssocDocumentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewAssocDocumentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewAssocDocumentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewAssocWorkflowResponseType newAssocWorkflow(br.andrew.sap.workflow.NewAssocWorkflowRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newAssocWorkflow");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newAssocWorkflow"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewAssocWorkflowResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewAssocWorkflowResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewAssocWorkflowResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.ExecuteSystemActivityResponseType executeSystemActivity(br.andrew.sap.workflow.ExecuteSystemActivityRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#executeSystemActivity");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "executeSystemActivity"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.ExecuteSystemActivityResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.ExecuteSystemActivityResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.ExecuteSystemActivityResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.ExecuteActivityResponseType executeActivity(br.andrew.sap.workflow.ExecuteActivityRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#executeActivity");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "executeActivity"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.ExecuteActivityResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.ExecuteActivityResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.ExecuteActivityResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewAccessExceptionResponseType newAccessException(br.andrew.sap.workflow.NewAccessExceptionRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newAccessException");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newAccessException"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewAccessExceptionResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewAccessExceptionResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewAccessExceptionResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.CancelWorkflowResponseType cancelWorkflow(br.andrew.sap.workflow.CancelWorkflowRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#cancelWorkflow");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "cancelWorkflow"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.CancelWorkflowResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.CancelWorkflowResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.CancelWorkflowResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.GetWorkflowResponseType getWorkflow(br.andrew.sap.workflow.GetWorkflowRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#getWorkflow");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "getWorkflow"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.GetWorkflowResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.GetWorkflowResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.GetWorkflowResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.UnlinkActivityFromUserResponseType unlinkActivityFromUser(br.andrew.sap.workflow.UnlinkActivityFromUserRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#unlinkActivityFromUser");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "unlinkActivityFromUser"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.UnlinkActivityFromUserResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.UnlinkActivityFromUserResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.UnlinkActivityFromUserResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.NewCommentResponseType newComment(br.andrew.sap.workflow.NewCommentRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#newComment");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "newComment"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.NewCommentResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.NewCommentResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.NewCommentResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public br.andrew.sap.workflow.ReactivateWorkflowResponseType reactivateWorkflow(br.andrew.sap.workflow.ReactivateWorkflowRequestType parameters) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("urn:workflow#reactivateWorkflow");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("urn:workflow", "reactivateWorkflow"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {parameters});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (br.andrew.sap.workflow.ReactivateWorkflowResponseType) _resp;
            } catch (java.lang.Exception _exception) {
                return (br.andrew.sap.workflow.ReactivateWorkflowResponseType) org.apache.axis.utils.JavaUtils.convert(_resp, br.andrew.sap.workflow.ReactivateWorkflowResponseType.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}

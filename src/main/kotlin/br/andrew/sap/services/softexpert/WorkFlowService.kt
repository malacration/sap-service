package br.andrew.sap.services.softexpert

import br.andrew.sap.document.NewDocument2ResponseType
import br.andrew.sap.workflow.*
import org.springframework.stereotype.Service

@Service
class WorkFlowService(val wsdl : WorkflowPortType) {

    fun teste(){
        val resultado = wsdl.getWorkflow(GetWorkflowRequestType().also {
            it.workflowID = "FXP-N000430"
        })
        println(resultado)
    }

    fun criaFluxoAssinaatura(idProcess : String,
                             title : String,
                             data : Map<String,Map<String,String>> = mapOf()): NewWorkflowEditDataResponseType {
        val entrys = data.map { (k, v) ->
            EntityArray().also {
                it.entityID = k
                it.entityAttributeList = v.map { (k2, v2) ->
                    AttributeData().also {
                        it.entityAttributeID = k2
                        it.entityAttributeValue = v2
                    }
                }.toTypedArray()
            }
        }.toTypedArray()

        val newWorkFlowData = NewWorkflowEditDataRequestType().also {
            it.processID = idProcess;
            it.workflowTitle = title
            it.entityList = entrys
        }
        return wsdl.newWorkflowEditData(newWorkFlowData).also {
            if(it.status != "SUCCESS") throw Exception("Erro SE ${it.detail}")
        }
    }

    fun associa(activity : String, taskInstance: NewWorkflowEditDataResponseType, document: NewDocument2ResponseType): NewAssocDocumentResponseType {
        return associa(activity,taskInstance.recordID,document.recordID)
    }

    fun associa(activity : String, taskInstance: String, document: String): NewAssocDocumentResponseType {
        return wsdl.newAssocDocument(NewAssocDocumentRequestType().also {
            it.workflowID = taskInstance
            it.activityID = activity
            it.documentID = document
        }).also {
            if(it.status != "SUCCESS") throw Exception("Erro SE ${it.detail}")
        }
    }
}
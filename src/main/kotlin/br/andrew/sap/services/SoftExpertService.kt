package br.andrew.sap.services

import br.andrew.sap.*
import org.springframework.stereotype.Service

@Service
class SoftExpertService(val wsdl : WorkflowPortType) {

    fun teste(){
        val resultado = wsdl.getWorkflow(GetWorkflowRequestType().also {
            it.workflowID = "FXP-N000430"
        })
        println(resultado)
    }

    fun criaFluxoAssinaatura(){
        val atributeList = mapOf(
            "nome" to "Windson Filipe",
            "cpf" to "330.828.170-84",
            "email" to "andrewc3po@gmail.com",
//            "telefone" to "69996106666",
            "idpedido" to "123666",
        ).map { (k, v) ->
            AttributeData().also {
                it.entityAttributeID = k
                it.entityAttributeValue = v
            }
        }.toTypedArray()

        val entry = EntityArray().also {
            it.entityID = "assinapessoadoc"
            it.entityAttributeList = atributeList
        }
        val newWorkFlowData = NewWorkflowEditDataRequestType().also {
            it.processID = "assinatura-pedido";
            it.workflowTitle = "Processo de assinatura para Pedido numero XPTO"
            it.entityList = arrayOf(entry)
        }
        val resultado = wsdl.newWorkflowEditData(newWorkFlowData)
        println(resultado)

//        NewAssocDocumentRequestType().also {
//            it.workflowID = resultado.recordKey
//            it.activityID = resultado.recordID
//            it.documentID = "assinatura-pedido"
//
//        }
    }
}
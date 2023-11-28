package br.andrew.sap.infrastructure.configurations.softexpert

import br.andrew.sap.document.DocumentoBindingStub
import br.andrew.sap.document.DocumentoLocator
import br.andrew.sap.document.DocumentoPortType
import br.andrew.sap.workflow.WorkflowBindingStub
import br.andrew.sap.workflow.WorkflowLocator
import br.andrew.sap.workflow.WorkflowPortType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL


@Configuration
class SoapConfig(val envrioment : SoftExpertEnvrioment) {

    @Bean
    fun workFlowBinding(): WorkflowPortType {
        return WorkflowBindingStub(
            getUrl(WorkflowLocator().workflowPortAddress),
            WorkflowLocator()
        ).also {
            it.username = envrioment.user
            it.password = envrioment.password
        }
    }

    @Bean
    fun documentBinding(): DocumentoPortType {
        return DocumentoBindingStub(
            getUrl(DocumentoLocator().documentoPortAddress),
            DocumentoLocator()
        ).also {
            it.username = envrioment.user
            it.password = envrioment.password
        }
    }

    fun getUrl(initialUri : String): URL {
        val builder = UriComponentsBuilder.fromHttpUrl(initialUri)
        if(envrioment.host != "windson")
            builder.host(envrioment.host)
        return URL(builder.toUriString())
    }
}


package br.andrew.sap.infrastructure.configurations.softexpert

import br.andrew.sap.WorkflowBindingStub
import br.andrew.sap.WorkflowLocator
import br.andrew.sap.WorkflowPortType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL


@Configuration
class SoapConfig(val envrioment : SoftExpertEnvrioment) {

    @Bean
    fun soapConnector(): WorkflowPortType {
        return WorkflowBindingStub(URL(envrioment.host),
            WorkflowLocator().also { it.setWorkflowPortEndpointAddress(envrioment.host) }
        ).also {
            it.username = envrioment.user
            it.password = envrioment.password
        }
    }
}


package br.andrew.sap.services.document

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.sap.documents.Delivery
import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.journal.OriginalJournal
import br.andrew.sap.services.AuthService
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.journal.EntryOriginalJournal
import br.andrew.sap.services.journal.ServiceOriginalJournal
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class DeliveryService(env: SapEnvrioment,
                      restTemplate: RestTemplate,
                      authService: AuthService) :
        EntitiesService<Delivery>(env, restTemplate, authService), ServiceOriginalJournal {

    override fun path(): String {
        return "/b1s/v1/DeliveryNotes"
    }

    override fun getEntryOriginalJournal(jdtNum: Int): EntryOriginalJournal {
        val filter = Filter(
            Predicate("DocEntry", jdtNum, Condicao.EQUAL)
        )
        return get(filter).tryGetValues<Delivery>().first()
    }

    override fun getOriginalJournal(): OriginalJournal {
        return OriginalJournal.ttDelivery
    }

}
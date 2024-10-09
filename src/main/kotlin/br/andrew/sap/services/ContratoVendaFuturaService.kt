package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.self.vendafutura.Contrato
import br.andrew.sap.services.abstracts.EntitiesService
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ContratoVendaFuturaService(restTemplate: RestTemplate,
                                 env: SapEnvrioment,
                                 authService : AuthService) : EntitiesService<Contrato>(env,restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/AR_CONTRATO_FUTURO"
    }

    fun saveOnly(contrato: Contrato): Contrato {
        val resultado = get(Filter("U_orderDocEntry",contrato.U_orderDocEntry,Condicao.EQUAL))
            .tryGetValues<Contrato>()
        //TODO remover depois
        //        return if(resultado.size == 1)
//            throw Exception("Nao e possivel criar contrado de vende de pedido que ja tem contrato. Pedido entry [${contrato.U_orderDocEntry}]")
        return if(resultado.size == 1)
            resultado.get(0)
        else
            save(contrato).tryGetValue<Contrato>()
    }
}
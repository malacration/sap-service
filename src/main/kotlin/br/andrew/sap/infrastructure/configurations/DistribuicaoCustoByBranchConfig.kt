package br.andrew.sap.infrastructure.configurations

import br.andrew.sap.model.sap.documents.base.DistribuicaoCustoByBranch
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class DistribuicaoCustoByBranchConfig(@Value("\${distibuicao.custo:[]}") val distibuicaoCusto : String) {

    private val logger = LoggerFactory.getLogger(DistribuicaoCustoByBranchConfig::class.java)

    init {
        try {
            val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
            val lista = mapper.readValue(distibuicaoCusto, jacksonTypeRef<List<DistribuicaoCustoByBranch>>())
            distibucoesCustos = lista
        }catch (e : Throwable){
            logger.error("Erro ao criar distribuicao de cuscto",e)
        }
    }

    companion object{
        var distibucoesCustos : List<DistribuicaoCustoByBranch> = listOf(
                DistribuicaoCustoByBranch("2","500","50000201"),
                DistribuicaoCustoByBranch("4","501","50100401"),
                DistribuicaoCustoByBranch("11","502","50200501")
        )
    }
}

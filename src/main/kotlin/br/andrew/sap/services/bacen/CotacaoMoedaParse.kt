package br.andrew.sap.services.bacen

import br.andrew.sap.model.CurrencyRate
import br.andrew.sap.model.bacen.CotacaoMoeda
import java.time.ZoneId

class CotacaoMoedaParse {

    /**
     * Retorna os dias que nao estao na cotacao do bacen com o valor da ultima cotacao
     */

    fun toCurrencyRateRange(cotacoes: List<CotacaoMoeda>, currency : String? = null, futuro : Long = 0) : List<CurrencyRate>{
        if(cotacoes.isEmpty())
            return listOf()

        val startDate = cotacoes
            .sortedBy { it.dataHoraCotacao }
            .firstOrNull()
            ?.dataHoraCotacao?.toInstant()?.atZone(ZoneId.systemDefault())
            ?.toLocalDate() ?: throw Exception("nao e possivel encontrar data incial")

        val endDate = cotacoes
            .sortedByDescending { it.dataHoraCotacao }
            .firstOrNull()
            ?.dataHoraCotacao?.toInstant()?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()?.plusDays(futuro) ?: throw Exception("nao e possivel encontrar data final")

        val resultado = mutableListOf<CurrencyRate>()
        var currentDate = startDate
        lateinit var anterior : CotacaoMoeda
        while (currentDate <= endDate) {
            val objAtual : CotacaoMoeda? = cotacoes.firstOrNull{
                it.dataHoraCotacao.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(currentDate)
            }
            if(objAtual != null){
                anterior = objAtual
                resultado.add(objAtual.getCurrencyRate())
            }
            else{
                resultado.add(anterior.getCurrencyRate(currentDate))
            }
            currentDate = currentDate.plusDays(1)
        }
        if(currency != null)
            resultado.forEach{it.Currency = currency}

        return resultado
    }
}
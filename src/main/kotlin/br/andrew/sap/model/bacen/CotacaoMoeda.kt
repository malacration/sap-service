package br.andrew.sap.model.bacen

import br.andrew.sap.model.CurrencyRate
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
class CotacaoMoeda(
    val cotacaoCompra : Double,
    val cotacaoVenda : Double,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone="America/Sao_Paulo")
    dataHoraCotacao : Date,
    val tipoBoletim : String
) {
    val dataHoraCotacao : Date

    init {
        this.dataHoraCotacao = removeTime(dataHoraCotacao)
    }

    private fun removeTime(date: Date): Date {
        val cal: Calendar = Calendar.getInstance()
        cal.setTime(date)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.getTime()
    }



    fun getCurrencyRate() : CurrencyRate {
        return getCurrencyRate(dataHoraCotacao)
    }

    fun getCurrencyRate(dataHoraCotacao : Date) : CurrencyRate {
        return CurrencyRate(dataHoraCotacao,cotacaoVenda.toString())
    }

    fun getCurrencyRate(dataHoraCotacao : LocalDate) : CurrencyRate {
        return CurrencyRate(Date.from(dataHoraCotacao.atStartOfDay(ZoneId.systemDefault()).toInstant()),cotacaoVenda.toString())
    }
}
package br.andrew.sap.controllers.logistica

import br.andrew.sap.model.logistica.TicketFreteLocalidade
import br.andrew.sap.services.logistica.TicketFreteService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("relatorios/frete")
class RelatorioFreteController(val service: TicketFreteService) {

    @GetMapping("ticket-medio-localidade")
    fun ticketMedioPorLocalidade(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) de: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) ate: LocalDate,
        @RequestParam(required = false) filial: Int?
    ): List<TicketFreteLocalidade> {
        if (ate.isBefore(de))
            throw Exception("A data final nao pode ser menor que a inicial")
        return service.getTicketMedioPorLocalidade(de, ate, filial)
    }
}

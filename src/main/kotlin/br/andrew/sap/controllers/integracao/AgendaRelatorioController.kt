package br.andrew.sap.controllers.integracao

import br.andrew.sap.model.agenda.AgendaConfig
import br.andrew.sap.services.integracao.AgendaRelatorioService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.time.LocalTime

/** Tela de gestao do e-mail de inadimplencia. So admin (curinga do rules.yml). */
@RestController
@RequestMapping("agenda-relatorio/inadimplencia")
class AgendaRelatorioController(private val service: AgendaRelatorioService) {

    @GetMapping
    fun obter(): AgendaDto = AgendaDto.de(service.obter(), service.emAndamento())

    @PutMapping
    fun salvar(@RequestBody dto: AgendaDto): AgendaDto {
        val dias = dto.dias.orEmpty()
        val hora = dto.hora.orEmpty()
        val copia = dto.copia.orEmpty().map { it.trim() }.filter { it.isNotEmpty() }
        AgendaConfig.validar(dias, hora, copia)?.let { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it) }
        val atual = service.obter()
        val salva = service.salvar(
            atual.copy(ativo = dto.ativo != false, dias = dias.toSet(), hora = LocalTime.parse(hora), copia = copia.distinct())
        )
        return AgendaDto.de(salva, service.emAndamento())
    }

    /** Envio assincrono: 202 se iniciou, 409 se ja tem um em andamento. O resultado aparece em `ultimoStatus`. */
    @PostMapping("enviar")
    fun enviar(@RequestParam(required = false) slpCode: Int?, @RequestParam(required = false) destino: String?): ResponseEntity<Void> {
        // destino = envio de teste: o relatorio do vendedor vai so para esse e-mail, sem copia
        val teste = destino?.trim()?.takeIf { it.isNotEmpty() }
        if (teste != null) {
            if (slpCode == null) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o vendedor para o envio de teste.")
            if (!AgendaConfig.emailValido(teste)) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail de teste invalido: $teste")
        }
        return if (service.enviarAgora(slpCode, teste)) ResponseEntity.accepted().build()
        else ResponseEntity.status(HttpStatus.CONFLICT).build()
    }
}

data class AgendaDto(
    val ativo: Boolean? = true,
    val dias: List<Int>? = null,
    val hora: String? = null,
    val copia: List<String>? = null,
    val ultimoEnvio: String? = null,
    val ultimoStatus: String? = null,
    val emAndamento: Boolean = false,
) {
    companion object {
        fun de(c: AgendaConfig, emAndamento: Boolean) = AgendaDto(
            ativo = c.ativo, dias = c.dias.sorted(), hora = c.hora.toString(), copia = c.copia,
            ultimoEnvio = c.ultimoEnvio?.toString(), ultimoStatus = c.ultimoStatus, emAndamento = emAndamento,
        )
    }
}

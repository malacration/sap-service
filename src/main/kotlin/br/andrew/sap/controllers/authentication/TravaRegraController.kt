package br.andrew.sap.controllers.authentication

import br.andrew.sap.model.trava.TravaRegra
import br.andrew.sap.services.security.TravaRegraService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * CRUD do catálogo de regras de trava (UDO TRAVA_REGRA no SAP). Convive com o
 * TravaOtpController (mesmo base "trava", caminhos diferentes).
 *
 * GET livre para o papel `liberacao_trava` (popular o select). POST/PUT/DELETE
 * só admin (coberto pelo curinga do rules.yml) — gerir o catálogo é ação de admin.
 */
@RestController
@RequestMapping("trava")
class TravaRegraController(val service: TravaRegraService) {

    @GetMapping("/regras")
    fun listar(@RequestParam(required = false, defaultValue = "false") todas: Boolean): List<RegraDto> =
        service.listar(todas).map { RegraDto.from(it) }

    @PostMapping("/regras")
    fun criar(@RequestBody dto: RegraDto): RegraDto =
        RegraDto.from(service.salvar(dto.toEntity()))

    @PutMapping("/regras/{codigo}")
    fun editar(@PathVariable codigo: String, @RequestBody dto: RegraDto): RegraDto {
        val entidade = dto.toEntity()
        entidade.Code = codigo
        return RegraDto.from(service.salvar(entidade))
    }

    @DeleteMapping("/regras/{codigo}")
    fun apagar(@PathVariable codigo: String) {
        service.deletar(codigo)
    }
}

data class RegraDto(
    val codigo: String? = null,
    val descricao: String? = null,
    val ordem: Int? = null,
    val ativo: Boolean? = null,
) {
    fun toEntity() = TravaRegra(
        Code = codigo?.trim()?.uppercase(),
        U_Descricao = descricao,
        U_Ordem = ordem,
        U_Ativo = if (ativo == false) "N" else "Y",
    )

    companion object {
        fun from(r: TravaRegra) = RegraDto(
            codigo = r.Code,
            descricao = r.U_Descricao,
            ordem = r.U_Ordem,
            ativo = r.U_Ativo != "N",
        )
    }
}

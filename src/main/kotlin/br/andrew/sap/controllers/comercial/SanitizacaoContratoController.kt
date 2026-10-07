package br.andrew.sap.controllers.comercial

import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.comercial.sanitizacao.*
import br.andrew.sap.services.odbc.OdbcException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("sanitizacao-contratos")
class SanitizacaoContratoController(private val service: SanitizacaoContratoService) {
    @GetMapping("previa")
    fun previa(auth: Authentication): PreviaSanitizacao = service.previa(usuario(auth))

    @PostMapping("aplicar")
    fun aplicar(auth: Authentication, @RequestBody pedido: AplicarSanitizacao): ResultadoSanitizacao =
        service.aplicar(usuario(auth), pedido)

    private fun usuario(auth: Authentication): String {
        if (auth !is User || !auth.isAdmin())
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para acessar este recurso.")
        return "${auth.origin}:${auth.id}"
    }

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun invalido(e: RuntimeException) = ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("mensagem" to e.message))

    @ExceptionHandler(OdbcException::class)
    fun odbc(e: OdbcException) = ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(mapOf("mensagem" to
        if (e.codigo == "resultado_truncado") "A consulta excedeu o limite de linhas. Nenhuma prévia incompleta será aplicada; contate o suporte." else e.message))
}

package br.andrew.sap.controllers.sistema

import br.andrew.sap.model.authentication.User
import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoException
import br.andrew.sap.services.journal.importacao.HistoricoImportacao
import br.andrew.sap.services.journal.importacao.ImportacaoLancamentoService
import br.andrew.sap.services.journal.importacao.PreviaImportacaoLancamento
import br.andrew.sap.services.journal.importacao.ResultadoImportacaoLancamento
import br.andrew.sap.services.odbc.OdbcException
import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

/**
 * Tela de importacao de lancamentos. O corpo e o arquivo CSV cru (bytes, nao texto): quem decide
 * a codificacao e o LancamentoCsvParser, e o hash da previa e o do arquivo exato.
 *
 * Acesso pelo rules.yml (perfil contabil_importacao). So existe com fields.importacao-lancamento,
 * a mesma flag que cria o UDO do log: sem ele a previa e a importacao quebrariam.
 */
@RestController
@RequestMapping("journal/importacao")
@ConditionalOnProperty(value = ["fields.importacao-lancamento"], havingValue = "true", matchIfMissing = false)
class ImportacaoLancamentoController(private val service: ImportacaoLancamentoService) {

    //corpo opcional: arquivo de 0 bytes chega sem corpo, e o Spring responderia 500 (com alerta no
    //Telegram) em vez da previa dizendo "arquivo vazio"
    @PostMapping("validar")
    fun validar(@RequestBody(required = false) conteudo: ByteArray?, @RequestParam(required = false) arquivo: String?): PreviaImportacaoLancamento =
        service.validar(conteudo ?: ByteArray(0), arquivo)

    @PostMapping
    fun importar(
        auth: Authentication,
        @RequestBody(required = false) conteudo: ByteArray?,
        @RequestParam(required = false) arquivo: String?,
        @RequestParam hash: String,
        @RequestParam(defaultValue = "false") forcarDuplicado: Boolean,
    ): ResultadoImportacaoLancamento {
        val usuario = auth as? User ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
        return service.importar(conteudo ?: ByteArray(0), arquivo, hash, forcarDuplicado, usuario._name, "${usuario.origin}:${usuario.id}")
    }

    @GetMapping("historico")
    fun historico(): List<HistoricoImportacao> = service.historico()

    @JsonInclude(JsonInclude.Include.NON_NULL)
    class ErroImportacao(val mensagem: String, val previa: PreviaImportacaoLancamento? = null)

    @ExceptionHandler(ImportacaoLancamentoException::class)
    fun recusada(e: ImportacaoLancamentoException) = ResponseEntity.status(e.status).body(ErroImportacao(e.message, e.previa))

    @ExceptionHandler(OdbcException::class)
    fun odbc(e: OdbcException) = ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(ErroImportacao("Não foi possível consultar os cadastros do SAP para validar o arquivo: ${e.message}"))
}

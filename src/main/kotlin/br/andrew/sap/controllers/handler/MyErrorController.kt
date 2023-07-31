package br.andrew.sap.controllers.handler

import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.TelegramRequestService
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import java.util.*

class MyErrorController(val telegram : TelegramRequestService) : ErrorController {

    val log = LoggerFactory.getLogger(MyErrorController::class.java)

    @RequestMapping("/error",produces = ["application/json"])
    fun handleError(request: HttpServletRequest, t : Throwable): ResponseEntity<ErroDto> {
        val trace = request.getAttribute("trace") ?: t
        val traceId = if (request.getAttribute("TraceId") == null ) "SEM TRACE ID" else request.getAttribute("TraceId").toString()
        log.error("Error controller",t)
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        val statusCode  = if (status != null)
            Integer.valueOf(status.toString())
        else if(status == 404 || status == "404" )
            404
        else
            500

        val erro = if(statusCode == HttpStatus.FORBIDDEN.value())
            ErroDto("Acesso Negado",traceId)
        else if(statusCode == HttpStatus.NOT_FOUND.value())
            ErroDto(t.message ?: "Pagina Não encontrada",traceId,t)
        else
            ErroDto(t.message ?: "Erro inesperado",traceId,t)

        telegram.send(t.message ?: "Sem mensagem",TipoMensagem.erros)

        return ResponseEntity
            .status(statusCode)
            .header("error", "[$traceId]")
            .header("info", "[$traceId]")
            .header("traceId", traceId)
            .body(erro)
    }

    private fun getTraceParameter(request: HttpServletRequest): Boolean {
        val parameter = request.getParameter("trace") ?: return false
        return "false" != parameter.lowercase(Locale.getDefault())
    }
}


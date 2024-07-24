package br.andrew.sap.controllers.handler

import br.andrew.sap.model.sap.SapError
import br.andrew.sap.model.telegram.TipoMensagem
import br.andrew.sap.services.TelegramRequestService
import brave.Tracer
import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.servlet.NoHandlerFoundException
import java.util.*

class MyErrorController(val tracer: Tracer, val telegram: TelegramRequestService) : ErrorController {

    val log = LoggerFactory.getLogger(MyErrorController::class.java)

    fun getCurrentTrace(): String {
        try{
            return tracer.currentSpan().context().spanIdString()
        }catch (e : Throwable){
            log.error("Erro ao gerar trace ID",e)
            return "SEM TRACE ID"
        }
    }
    @RequestMapping("/error",produces = ["application/json"])
    fun handleError(request: HttpServletRequest, t : Throwable): ResponseEntity<ErroDto> {
        if(t is NoHandlerFoundException){
            if(t.requestURL == "/favicon.ico")
                return ResponseEntity.notFound().build()
        }
        val traceId = getCurrentTrace()
        log.error("Error controller",t)
        val status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE)
        val statusCode  = status?.toString()?.toIntOrNull() ?: 500
        val erro = if(statusCode == HttpStatus.FORBIDDEN.value())
            ErroDto("Acesso Negado",traceId)
        else if(statusCode == HttpStatus.NOT_FOUND.value())
            ErroDto(t.message ?: "Pagina Não encontrada",traceId,t)
        else if(t is HttpClientErrorException){
            val msg = t.getResponseBodyAs(SapError::class.java)?.error?.message?.value ?: "Erro inesperado"
            ErroDto(msg,traceId,t)
        }
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


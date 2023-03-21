package br.andrew.sap.controllers.handler

import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class ControllerAdviceAccessDeniedHandler {

    val logger = getLogger(ControllerAdviceAccessDeniedHandler::class.java)

    private val traceId: String
        get() = if(MDC.get("X-B3-TraceId") == null) "" else MDC.get("X-B3-TraceId")

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e : AccessDeniedException) : ResponseEntity<String>{
        logger.error("acesso negado",e)
        return ResponseEntity
            .status(403)
            .header("error", "Acesso Negado! [$traceId]")
            .header("info", "Acesso Negado! [$traceId]")
            .header("traceId", traceId)
            .body("Acesso Negado! [$traceId]")
    }

}

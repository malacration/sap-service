package br.andrew.sap.controllers.handler

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory.getLogger
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
class ControllerAdviceExceptionHandler {

    private val log = getLogger(ControllerAdviceExceptionHandler::class.java)

    @ExceptionHandler(Throwable::class)
    fun handleException(response: HttpServletRequest, e: Exception): ResponseEntity<ErroDto> {
        return MyErrorController().handleError(response,e)
    }
}

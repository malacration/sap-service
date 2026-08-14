package br.andrew.sap.controllers.documents

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.services.document.DownPaymentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

/**
 * O PIX dos boletos de contrato de venda futura segue a mesma regra do PIX de titulo
 * (PixController.gerarChave): role `pix` gera somente com juros, `pix_admin` gera com ou sem.
 */
class DownPaymentControllerTest {

    private val service = mock(DownPaymentService::class.java)
    private val controller = DownPaymentController(service, TAXA)

    @Test
    fun `role pix gera com juros aplicando a taxa configurada`() {
        `when`(service.createPixByContratoVendaFutura(10, TAXA)).thenReturn(listOf())

        controller.gerarPixByContrato(10, true, user("pix"))

        verify(service).createPixByContratoVendaFutura(10, TAXA)
    }

    @Test
    fun `role pix nao pode gerar sem juros`() {
        val erro = assertThrows(Exception::class.java) {
            controller.gerarPixByContrato(10, false, user("pix"))
        }

        assertEquals("Não é permitido criar pix!", erro.message)
    }

    @Test
    fun `role pix_admin gera sem juros zerando a taxa`() {
        `when`(service.createPixByContratoVendaFutura(10, 0.0)).thenReturn(listOf())

        controller.gerarPixByContrato(10, false, user("pix_admin"))

        verify(service).createPixByContratoVendaFutura(10, 0.0)
    }

    @Test
    fun `role pix_admin tambem gera com juros`() {
        `when`(service.createPixByContratoVendaFutura(10, TAXA)).thenReturn(listOf())

        controller.gerarPixByContrato(10, true, user("pix_admin"))

        verify(service).createPixByContratoVendaFutura(10, TAXA)
    }

    @Test
    fun `usuario sem role de pix nao gera nem com juros`() {
        val erro = assertThrows(Exception::class.java) {
            controller.gerarPixByContrato(10, true, user("vendedor"))
        }

        assertEquals("Não é permitido criar pix!", erro.message)
    }

    private fun user(vararg roles: String): User {
        return User(
            "windson", "windson", UserOriginEnum.EmployeesInfo, "", "", "",
            listOf(), roles.toList()
        )
    }

    companion object {
        private const val TAXA = 0.1
    }
}

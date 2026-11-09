package br.andrew.sap.services.integracao

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Properties

class MyMailMessageCopiaTest {

    private fun mime() = MimeMessage(Session.getInstance(Properties()))

    @Test
    fun `html leva os destinatarios em copia alem do vendedor`() {
        val msg = MyMailMessage(
            listOf(EmailAdrres("vendedor@x.com")), "assunto", "<p>oi</p>", From.DEFAULT,
            listOf(EmailAdrres("gerente@x.com"), EmailAdrres("cobranca@x.com")),
        ).get(mime())

        assertEquals(listOf("vendedor@x.com"), msg.getRecipients(Message.RecipientType.TO).map { it.toString() })
        assertEquals(listOf("gerente@x.com", "cobranca@x.com"), msg.getRecipients(Message.RecipientType.CC).map { it.toString() })
    }

    @Test
    fun `sem copia configurada o e-mail sai como antes`() {
        val msg = MyMailMessage("vendedor@x.com", "assunto", "<p>oi</p>").get(mime())

        assertNull(msg.getRecipients(Message.RecipientType.CC))
        assertNull(MyMailMessage("vendedor@x.com", "a", "b").simpleMailMessage().cc)
    }
}

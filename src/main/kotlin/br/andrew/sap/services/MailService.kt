package br.andrew.sap.services

import br.andrew.sap.services.MailService.Companion.copyDefault
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.springframework.mail.MailMessage
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context


@Service
class MailService(val mailSender: JavaMailSender, val templateEngine : TemplateEngine) {

    fun sendEmail(mailMessage : MyMailMessage, html : Boolean = false) {
        if(html)
            mailSender.send(mailMessage.get(mailSender.createMimeMessage()))
        else
            mailSender.send(mailMessage.simpleMailMessage())
    }

    companion object{
        var copyDefault : MutableList<String> = mutableListOf()
    }
}

class MyMailMessage(val to : EmailAdrres, val subject : String, val body : String, val from : From = From.DEFAULT){
    constructor(to : String, subject : String, body : String, from : From = From.DEFAULT)
            : this(EmailAdrres(to), subject, body, from)

    fun simpleMailMessage(): SimpleMailMessage {
        return SimpleMailMessage().also {
            it.replyTo = From.getFrom(from)?.toString()
            it.from = From.getFrom(from)?.toString()
            it.setTo(to.email)
            it.subject = subject
            it.text = body
        }
    }

    fun get(createMimeMessage: MimeMessage) : MimeMessage {
        return MimeMessageHelper(createMimeMessage, true).also {
            if(From.getFrom(from) != null)
                it.setFrom(From.getFrom(from)!!.getInternetAddress())
            it.setTo(mutableListOf<String>(to.email).also { it.addAll(copyDefault) }.toTypedArray())
            it.setSubject(subject)
            it.setText(body, true)
        }.mimeMessage
    }
}

enum class From{
    DEFAULT,
    COMERCIAL;
    companion object{
        val mailList : MutableList<EmailAdrres> = mutableListOf()
        fun getFrom(from: From) : EmailAdrres?{
            return mailList.firstOrNull{ it.type == from }
        }
    }
}

class EmailAdrres(val email : String,
                  val name : String? = null,
                  val type : From? = null){

    init {
        //Email validation
        if(!email.contains("@"))
            throw Exception("Invalid email account")
        //Email wiout space
        if(email.contains(" "))
            throw Exception("Invalid email, space not allowed")
    }

    override fun toString(): String {
        return if(name == null)
            email
        else
            "$name <$email>"
    }

    fun getInternetAddress(): InternetAddress {
        return InternetAddress(email, name)
    }
}

//-- ("Grupo Rovema <no-reply@gruporovema.com.br>")
//-- ("Comercial - Grupo Rovema <no-reply@gruporovema.com.br>")
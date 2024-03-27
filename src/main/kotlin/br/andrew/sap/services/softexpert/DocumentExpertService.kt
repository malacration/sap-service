package br.andrew.sap.services.softexpert

import br.andrew.sap.document.*
import br.andrew.sap.workflow.NewAssocDocumentRequestType
import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import org.apache.tika.Tika
import org.apache.tika.mime.MimeTypes
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date

@Service
class DocumentExpertService(val wsdl : DocumentoPortType) {

    fun getMockPdf(): ByteArray {
        val html = "<html><body><h1>Test</h1><p>This is a test.</p></body></html>"
        val pdfDest = ByteArrayOutputStream()
        val converterProperties = ConverterProperties()
        HtmlConverter.convertToPdf(html.byteInputStream(),
            pdfDest, converterProperties)
        return pdfDest.toByteArray()
    }

    fun save(documentName : String,
             title : String,
             category: String,
             data: ByteArray,
             date : Date = Date(),
             summary : String = ""): NewDocument2ResponseType {
        val ext =  MimeTypes.getDefaultMimeTypes().forName(Tika().detect(data)).extension
        return wsdl.newDocument2(NewDocument2RequestType().also {
            it.categoryID = category
            it.templateID = ""
            it.documentID = ""
            it.title = title
            it.summary = summary
            it.date = SimpleDateFormat("yyyy-MM-dd").format(date)
            it.responsibleUserID = ""
            it.revisionMembers = arrayOf()
            it.keywords = arrayOf()
            it.attributes = arrayOf()
            it.languageID = ""
            it.revisionID = ""
            it.files = arrayOf(File().also {
                it.name = "${documentName}${ext}"
                it.content = data
                it.container = ""
            })
        }).also {
            if(it.status != "SUCCESS") throw Exception("Erro SE ${it.detail}")
        }

    }

}
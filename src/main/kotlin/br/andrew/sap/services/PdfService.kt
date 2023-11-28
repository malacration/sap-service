package br.andrew.sap.services

import com.itextpdf.html2pdf.ConverterProperties
import com.itextpdf.html2pdf.HtmlConverter
import java.io.ByteArrayOutputStream

class PdfService {

    fun htmlToPdf(html: String): ByteArray {
        val pdfDest = ByteArrayOutputStream()
        val converterProperties = ConverterProperties()
        HtmlConverter.convertToPdf(html.byteInputStream(),
            pdfDest, converterProperties)
        return pdfDest.toByteArray()
    }
}
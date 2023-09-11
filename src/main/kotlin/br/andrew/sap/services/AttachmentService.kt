package br.andrew.sap.services

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.Attachment
import br.andrew.sap.model.envrioments.SapEnvrioment
import br.andrew.sap.model.SapError
import br.andrew.sap.model.partner.BusinessPartner
import br.andrew.sap.services.abstracts.EntitiesService
import org.apache.hc.client5.http.utils.Base64
import org.apache.tika.Tika
import org.apache.tika.mime.MimeTypes
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Service
class AttachmentService(env: SapEnvrioment, val bpService: BusinessPartnersService,
                        restTemplate: RestTemplate, authService: AuthService) :
    EntitiesService<Attachment>(env, restTemplate, authService) {

    override fun path(): String {
        return "/b1s/v1/Attachments2"
    }

    fun download(id: String) : Pair<String, ByteArray> {
        val request = RequestEntity
            .get(env.host+this.path()+"(${id})/\$value")
            .header("cookie","B1SESSION=${session().sessionId}")
            .build()
        val response = restTemplate.exchange(request, ByteArray::class.java)
        val body = response.body ?: ByteArray(0)
        val contentType = response.headers["Content-Type"]?.toString() ?: ""
        return (contentType to body)
    }

    fun appendFile(cardCode: String, attachment: Attachment): ResponseEntity<OData>{
        try{
            val partner = bpService.getById("'$cardCode'")
                .tryGetValue<BusinessPartner>()

            val request = if(partner.attachmentEntry == null)
                RequestEntity
                    .post(env.host+this.path())
            else
                RequestEntity
                    .patch(env.host+this.path()+"(${partner.attachmentEntry})")

            val boundary = "WebKitFormBoundaryUmZoXOtOBNCTLyxT"
            val mimeType: String = Tika().detect(Base64.decodeBase64(attachment.file64))
            val extension = MimeTypes.getDefaultMimeTypes().forName(mimeType).extension
            val fileName = attachment.fileName+" - "+cardCode+extension

            var body =
                "--$boundary\n"+
                        "Content-Disposition: form-data; name=\"$fileName\"; filename=\"$fileName\"\n" +
                        "Content-Type: $mimeType\n\n"+
                        "${attachment.file64}\n"+
                        "--$boundary--"
            request
                .header("Content-Type","multipart/form-data; boundary=$boundary")
                .header("cookie","B1SESSION=${session().sessionId}")
            val retorno = restTemplate.exchange(request.body(body), OData::class.java)

            if(partner.attachmentEntry == null){
                bpService.update(BusinessPartner().also {
                    it.attachmentEntry = retorno.body!!.tryGetValue<Attachment>().absoluteEntry
                },"'$cardCode'")
            }
            return retorno
        }catch (t : HttpClientErrorException){
            throw t.getResponseBodyAs(SapError::class.java)?.getError(t,"") ?: t
        }
    }
}
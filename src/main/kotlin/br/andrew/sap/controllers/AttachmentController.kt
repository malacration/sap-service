package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.sap.Attachment
import br.andrew.sap.services.AttachmentService
import br.andrew.sap.services.StorageService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("attachment")
class AttachmentController(
    val service: AttachmentService,
    val storageService: StorageService) {


    @GetMapping()
    fun get(): OData {
        return service.get(OrderBy("AbsoluteEntry", Order.DESC))
    }

    @GetMapping("{id}")
    fun download(@PathVariable id : String): ResponseEntity<ByteArray> {
        val retorno = service.download(id)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(retorno.first.replace("[","").replace("]","")))
            .body(retorno.second)
    }

    @PostMapping("business-partners/{cardCode}")
    fun upload(@RequestBody attachment: Attachment, @PathVariable cardCode : String){
        service.appendFile(cardCode,attachment)
    }

    @GetMapping("business-partners/{cardCode}/{file}")
    fun moveAnexoToSap(@PathVariable cardCode : String, @PathVariable file : String){
        upload(storageService.get(file),cardCode)
    }

    @PostMapping("business-partners")
    fun save(@RequestBody upload : UploadDto): String {
        moveAnexoToSap(upload.cardCode,upload.file)
        return "{ \"status\": 200 }"
    }


}
class UploadDto(val cardCode : String, val file : String)
package br.andrew.sap.controllers

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.infrastructure.odata.Order
import br.andrew.sap.infrastructure.odata.OrderBy
import br.andrew.sap.model.Attachment
import br.andrew.sap.services.AttachmentService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("attachment")
class AttachmentController(val service: AttachmentService) {


    @GetMapping()
    fun get(): OData {
        return service.get(OrderBy("AbsoluteEntry", Order.DESC))
    }

    @GetMapping("{id}")
    fun download(@PathVariable id : String): ResponseEntity<ByteArray> {
        val retorno = service.download(id)
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(retorno.first.replace("[","").replace("]","")))
            .body(retorno.second);
    }

    @PostMapping("business-partners/{cardCode}")
    fun upload(@RequestBody attachment: Attachment, @PathVariable cardCode : String){
        service.appendFile(cardCode,attachment)
    }
}
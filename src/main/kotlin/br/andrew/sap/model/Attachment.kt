package br.andrew.sap.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import org.apache.hc.client5.http.utils.Base64
import java.io.File
import java.util.*


@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
class Attachment() {
    constructor(file : File) : this(){
        body = file.readBytes()
        file64 = Base64.encodeBase64String(file.readBytes())
        fileName = file.nameWithoutExtension
    }

    var body: ByteArray? = null
    var fileName : String? = null
    var absoluteEntry : Int? = null
    var file64 : String? = null
}
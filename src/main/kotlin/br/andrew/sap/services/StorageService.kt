package br.andrew.sap.services

import br.andrew.sap.model.sap.Attachment
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isReadable

@Service
class StorageService(@Value("\${storage.path:anexo}") val rootPath: String) {

    fun get(filePath: String) : Attachment {
        val path = Path.of("$rootPath/$filePath")
        if(path.isDirectory())
            throw Exception("Path is a directory")
        if(!path.isReadable())
            throw Exception("Path is not readable")
        return Attachment(path.toFile())
    }


}
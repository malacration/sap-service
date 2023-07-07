package br.andrew.sap.model.documents

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException

class DocumentLinesDeserializer : JsonDeserializer<DocumentLines>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): DocumentLines {
        val node: JsonNode = jp.getCodec().readTree(jp)
        if(node.get("itemCode") == null) {
            return Service(
                node.get("UnitPrice")?.asText() ?: "0",
                node.get("Quantity")?.asText() ?: "0"
            ).also { it.setJson(node) }
        }
        else {
            return Product(
                node.get("itemCode").asText(),
                node.get("Quantity")?.asText() ?: "0",
                node.get("UnitPrice")?.asText() ?: "0"
            )
                .also { it.setJson(node) }
        }
    }
}
package br.andrew.sap.model.sap.documents.base

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException

class DocumentLinesDeserializer : JsonDeserializer<DocumentLines>() {

    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): DocumentLines {
        val node: JsonNode = jp.codec.readTree(jp)
        val line = if(node.get("ItemCode") == null) {
            Service(
                node.get("UnitPrice")?.asText() ?: "0",
                node.get("Quantity")?.asText() ?: "0"
            ).also { it.setJson(node) }
        }
        else {
            Product(
                node.get("ItemCode").asText(),
                node.get("Quantity")?.asText() ?: "0",
                node.get("UnitPrice")?.asText() ?: "0"
            ).also { it.setJson(node) }
        }
        line.LineTaxJurisdictions = parseTaxJurisdictions(node.get("LineTaxJurisdictions"))
        return line
    }

    private fun parseTaxJurisdictions(node: JsonNode?): List<LineTaxJurisdiction> {
        if (node == null || !node.isArray)
            return listOf()
        return node.map { j ->
            LineTaxJurisdiction().also {
                it.JurisdictionType = j.get("JurisdictionType")?.takeIf { n -> !n.isNull }?.asInt()
                it.JurisdictionCode = j.get("JurisdictionCode")?.takeIf { n -> !n.isNull }?.asText()
                it.TaxAmount = j.get("TaxAmount")?.takeIf { n -> !n.isNull }?.asDouble()
                it.TaxRate = j.get("TaxRate")?.takeIf { n -> !n.isNull }?.asDouble()
            }
        }
    }
}
package br.andrew.sap.infrastructure.odata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class OData() : LinkedHashMap<String,Any>() {

    inline fun <reified T: Any> tryGetValue() : T {
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val json = this.get("value") ?: mapper.writeValueAsString(this)
        if(json is LinkedHashMap<*,*>)
            return mapper.readValue(mapper.writeValueAsString(json), T::class.java)
        return mapper.readValue(json.toString(),T::class.java)
    }

    inline fun <reified T> tryGetValues() : List<T> {
        val json = this.get("value")
        val mapper = ObjectMapper().registerModule(KotlinModule())

        if(json is String)
            return mapper.readValue(json, jacksonTypeRef<List<T>>())
        else if(json is List<*>)
            return json.map { mapper.readValue(mapper.writeValueAsString(it), T::class.java) }
        throw Exception("Não foi possivel fazer o parse")
    }

    fun next() {

    }
}

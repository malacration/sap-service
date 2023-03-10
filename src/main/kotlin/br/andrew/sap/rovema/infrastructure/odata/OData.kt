package br.andrew.sap.rovema.infrastructure.odata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef

class OData() : LinkedHashMap<String,Any>() {

    inline fun <reified T: Any> tryGetValue() : T {
        val json = this.get("value").toString()!!
        val mapper = ObjectMapper().registerModule(KotlinModule())
        return mapper.readValue(json,T::class.java)
    }

    inline fun <reified T: Any> tryGetValues() : List<T> {
        val json = this.get("value")
        val mapper = ObjectMapper().registerModule(KotlinModule())

        if(json is String)
            return mapper.readValue(json, jacksonTypeRef<List<T>>())
        else if(json is List<*>)
            return json.map { mapper.readValue(mapper.writeValueAsString(it), T::class.java) }
        throw Exception("Não foi possivel fazer o parse")
    }
}

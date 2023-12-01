package br.andrew.sap.infrastructure.odata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.web.client.RestTemplate

class OData : LinkedHashMap<String,Any>(){

    inline fun <reified T: Any> tryGetValue() : T {
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, true)
            .configure(KotlinFeature.NullToEmptyMap, true)
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .configure(KotlinFeature.SingletonSupport, true)
            .configure(KotlinFeature.StrictNullChecks, true)
            .build())
        val json = this.get("value") ?: mapper.writeValueAsString(this)
        if(json is LinkedHashMap<*,*>)
            return mapper.readValue(mapper.writeValueAsString(json), T::class.java)
        return mapper.readValue(json.toString(),T::class.java)
    }

    inline fun <reified T> tryGetValues() : List<T> {
        val json = this.get("value")
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, true)
            .configure(KotlinFeature.NullToEmptyMap, true)
            .configure(KotlinFeature.NullIsSameAsDefault, true)
            .configure(KotlinFeature.SingletonSupport, true)
            .configure(KotlinFeature.StrictNullChecks, true)
            .build())
        if(json is String)
            return mapper.readValue(json, jacksonTypeRef<List<T>>())
        else if(json is List<*>)
            return json.map { mapper.readValue(mapper.writeValueAsString(it), T::class.java) }
        throw Exception("Não foi possivel fazer o parse")
    }

    inline fun <reified T> tryGetPageValues(): Page<T> {
        return PageImpl<T>(tryGetValues<T>(), Pageable.unpaged(),count() ?:100)
    }

    fun nextLink() : String{
        return this.get("odata.nextLink").toString()
    }

    fun hasNext() : Boolean{
        return this.get("odata.nextLink") != null
    }

    fun count() : Long?{
        return (this.get("odata.count") as Int?)?.toLong()
    }
}

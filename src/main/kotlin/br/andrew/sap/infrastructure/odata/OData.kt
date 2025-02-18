package br.andrew.sap.infrastructure.odata

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class OData : LinkedHashMap<String,Any>(){

    val mapper = ObjectMapper().registerModule(KotlinModule.Builder()
        .withReflectionCacheSize(512)
        .configure(KotlinFeature.NullToEmptyCollection, true)
        .configure(KotlinFeature.NullToEmptyMap, true)
        .configure(KotlinFeature.NullIsSameAsDefault, true)
        .configure(KotlinFeature.SingletonSupport, true)
        .configure(KotlinFeature.StrictNullChecks, true)
        .build())
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


    fun <T> tryGetValues(clazz: Class<T>): List<T> {
        val json = this.get("value")
        val collectionType = mapper.typeFactory.constructCollectionType(List::class.java, clazz)
        if(json is String)
            return mapper.readValue(json, collectionType)
        else if(json is List<*>)
            return mapper.readValue(mapper.writeValueAsString(json), collectionType)
        throw Exception("Não foi possivel fazer o parse")
    }

    inline fun <reified T> tryGetPageValues(page : Pageable): Page<T> {
        return PageImpl<T>(tryGetValues<T>(), page,count() ?:100)
    }

    fun nextLink() : String{
        return this["odata.nextLink"]?.toString() ?: ""
    }

    fun hasNext() : Boolean{
        return this["odata.nextLink"] != null
    }

    fun count() : Long?{
        return (this.get("odata.count") as Int?)?.toLong()
    }

    inline fun <reified T: Any> tryGetNextValues(): NextLink<T> {
        return NextLink(tryGetValues(),nextLink())
    }
}

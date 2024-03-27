package br.andrew.sap.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class BoletoIdsConfig(@Value("\${boleto.ids:{windson,jose}}") val ids : List<String>) {


    init {
        BoletoIdsConfig._ids = ids
    }

    companion object{
        private var _ids = listOf<String>()

        val ids : List<String>
            get() = _ids
    }

}
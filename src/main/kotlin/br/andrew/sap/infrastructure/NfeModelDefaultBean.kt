package br.andrew.sap.infrastructure

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class NfeModelDefaultBean(@Value("\${nfe-model-default:-1}") val model : Int) {
    init {
        NfeModelDefaultBean.model = model
    }
    companion object {
        var model: Int = -1
    }
}
package br.andrew.sap.services.batch

import com.fasterxml.jackson.annotation.JsonIgnore

interface BatchId{
    @JsonIgnore
    fun getId() : String
}
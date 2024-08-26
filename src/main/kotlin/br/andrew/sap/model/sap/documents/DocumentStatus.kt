package br.andrew.sap.model.sap.documents

import com.fasterxml.jackson.annotation.JsonCreator


enum class DocumentStatus(val typeName: String) {
    bost_Open("bost_Open"),
    bost_Close("bost_Close"),
    bost_Paid("bost_Paid"),
    bost_Delivered("bost_Delivered");


    companion object {
        @JsonCreator
        @JvmStatic
        fun forValue(value: String): DocumentStatus? {
            return DocumentStatus.values().find { it.typeName == value }
        }
    }
}
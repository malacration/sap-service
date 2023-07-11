package br.andrew.sap.model.uzzipay

import com.fasterxml.jackson.annotation.*


@JsonIgnoreProperties(ignoreUnknown = true)
data class DataRetonroPixQrCode(val data: RetonroPixQrCode)


@JsonIgnoreProperties(ignoreUnknown = true)
class RetonroPixQrCode(val textContent: String, val link: String, val reference: String, val image: String?){

}
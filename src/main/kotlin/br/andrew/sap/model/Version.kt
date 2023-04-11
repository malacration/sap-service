package br.andrew.sap.model

class Version(val version : String = "0", val timestamp : String, val commitSha : String) {

    var company : String? = null
}
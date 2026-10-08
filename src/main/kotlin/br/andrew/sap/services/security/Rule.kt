package br.andrew.sap.services.security

data class Rule(val url : String, val actions : List<String>){
    constructor(url : String, action : String) : this(url, listOf(action))
}

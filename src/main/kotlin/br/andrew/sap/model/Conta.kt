package br.andrew.sap.model

class Conta {

    var id: String = ""
    var nivel: Int = 0
    var descricao: String = ""
    var contaPai: String = ""
    var valoresIndividualizados: List<ValoresIndividualizados> = listOf()

    // generate getters and setters
}

class ValoresIndividualizados{

    var dtBase: String = ""
    var valor: Double = 0.0

    // generate getters and setters
}
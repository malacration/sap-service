package br.andrew.sap.model.sistema
import br.andrew.sap.model.sap.cadastro.BussinessPlace

class Conta {

    var id: String = ""
    var nivel: Int = 0
    var descricao: String = ""
    var contaPai: String = ""
    var valoresIndividualizados: List<ValoresIndividualizados> = listOf()
    var bussinessPlace: BussinessPlace? = null

    // generate getters and setters
}

class ValoresIndividualizados{

    var dtBase: String = ""
    var valor: Double = 0.0

    // generate getters and setters
}

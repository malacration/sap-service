package br.andrew.sap.model.serasa
class P002(tipoRegistro : String, chave : String, valor : String) {

    val valores = listOf(
        Parametro("tipoRegistro",tipoRegistro, 4),

        Parametro("id","RSPU", 4),
        Parametro("chave","", 21),

        Parametro("id",chave, 4),
        Parametro("chave",valor, 21),

        Parametro("id","", 4),
        Parametro("chave","", 21),

        Parametro("id","", 4),
        Parametro("chave","", 21),

        Parametro("filtger","", 11)
    )

    val resultado = valores.joinToString("") { it.toString() }
}
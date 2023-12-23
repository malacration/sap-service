package br.andrew.sap.model.serasa

class I001(tipoRegistro : String, subTipo : String){

    val valores = listOf(
        Parametro("tipoRegistro",tipoRegistro, 4),

        Parametro("id",subTipo, 2),
        Parametro("filtro1","R", 1),

        Parametro("","", 108),
    )

    val resultado = valores.joinToString("") { it.toString() }
}
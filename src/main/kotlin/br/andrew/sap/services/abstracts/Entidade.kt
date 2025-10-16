package br.andrew.sap.services.abstracts

class Entidade(val entidadeNome : String,
               val propriedades : List<String>,
    val aggregation : Boolean = false){

    fun getExpand() : String{
        return "$entidadeNome(\$select=${propriedades.joinToString(",")})"
    }

    fun getExpandGroupBy() : String{
        return propriedades.joinToString(",") { "$entidadeNome/$it" }
    }
}
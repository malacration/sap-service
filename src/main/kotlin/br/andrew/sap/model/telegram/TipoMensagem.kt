package br.andrew.sap.model.telegram

enum class TipoMensagem(val tipo: String){
    eventos("sap"),
    geral("geral"),
    autorizacao("autorizacao"),
    testes("testes"),
    erros("erros");

    fun topic() : Int{
        return topics[tipo]?: 44
    }

    companion object{
        val topics : MutableMap<String,Int> = mutableMapOf()
    }
}
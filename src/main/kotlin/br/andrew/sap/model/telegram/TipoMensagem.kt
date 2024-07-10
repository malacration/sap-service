package br.andrew.sap.model.telegram

enum class TipoMensagem(val tipo: String){
    eventos("eventos"),
    geral("geral"),
    autorizacao("autorizacao"),
    testes("testes"),
    erros("erros");

    fun topic() : Int?{
        return topics[tipo]
    }

    companion object{
        val topics : MutableMap<String,Int> = mutableMapOf()
    }
}
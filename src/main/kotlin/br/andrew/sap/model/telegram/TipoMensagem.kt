package br.andrew.sap.model.telegram

enum class TipoMensagem(val tipo: String){
    eventos("eventos"),
    geral("geral"),
    autorizacao("autorizacao"),
    testes("testes"),
    erros("erros");

    fun topic() : Int?{
        if(this == TipoMensagem.geral)
            return null
        return topics[tipo]?: 44
    }

    companion object{
        val topics : MutableMap<String,Int> = mutableMapOf()
    }
}
package br.andrew.sap.model.telegram

class Mensagem(val chat_id : String,
               val text : String,
               val message_thread_id : String,
               val parse_mode : String = "html"){
    constructor(chat_id: String, text: String, tipo: TipoMensagem) : this(chat_id,text,tipo.topic().toString())

}

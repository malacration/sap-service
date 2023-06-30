package br.andrew.sap.model.telegram

class Mensagem(val chat_id : String,
               val text : String,
               val message_thread_id : TipoMensagem = TipoMensagem.testes,
               val parse_mode : String = "html")

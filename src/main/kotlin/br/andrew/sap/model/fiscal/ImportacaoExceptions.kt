package br.andrew.sap.model.fiscal

// Entrada invalida da importacao de planilha (arquivo, cabecalho, linha): o controller responde 400.
open class ImportacaoInvalidaException(mensagem: String) : IllegalArgumentException(mensagem)

// Arquivo acima do teto de tamanho: o controller responde 413.
class ArquivoGrandeDemaisException(mensagem: String) : ImportacaoInvalidaException(mensagem)

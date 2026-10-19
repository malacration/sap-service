package br.andrew.sap.services.batch

/**
 * O SAP respondeu ao $batch e recusou o changeset: como o changeset e transacional, nada foi
 * gravado. Subclasse de Exception para quem ja trata Exception continuar igual; existe para quem
 * precisa separar essa recusa de uma falha de rede ou de login, em que nao se sabe se o lote chegou
 * (ver ImportacaoLancamentoService).
 */
class BatchRecusadoException(message: String) : Exception(message)

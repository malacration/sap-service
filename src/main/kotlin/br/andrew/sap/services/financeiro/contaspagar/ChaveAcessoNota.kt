package br.andrew.sap.services.financeiro.contaspagar

import java.math.BigInteger

/** Tipo do documento fiscal de entrada, para o filtro por modelo (OPCH.Model). */
enum class TipoNotaEntrada {
    NFE, CTE, NFSE;

    companion object {
        /** "NFE"/"Nfe", "CTE", "NFSE"/"NFS-e" (sem caixa). Qualquer outro texto (ex.: NaoDetectado) -> null. */
        fun de(texto: String?): TipoNotaEntrada? = when (texto?.trim()?.uppercase()?.replace("-", "")) {
            "NFE" -> NFE
            "CTE" -> CTE
            "NFSE" -> NFSE
            else -> null
        }
    }
}

/**
 * Chave de acesso de nota de entrada, como gravada pelo TaxPlus em OPCH.U_ChaveAcesso.
 *
 * - 44 caracteres (NFe/CTe e demais modelos do leiaute SEFAZ): cUF(2) AAMM(4) CNPJ emitente(14)
 *   modelo(2) serie(3) numero(9) tpEmis(1) codigo(8) DV(1). O CNPJ pode ser alfanumerico
 *   (CNPJ alfanumerico a partir de 2026); o resto e so digito.
 * - 50 digitos (NFS-e nacional; o Qive manda `InfNfse/@Id` = "NFS" + 50): cMun(7) ambGer(1)
 *   tpInsc(1) inscricao federal(14) numero(13) AAMM(4) codigo(9) DV(1).
 * - `DPS` + 42 (id da DPS, raro): cMun(7) tpInsc(1) inscricao(14) serie(5) numero DPS(15). Sem numero
 *   da NFS-e: so o tipo e o emitente sao conferidos.
 */
data class ChaveAcessoNota(
    /** Como comparar com OPCH.U_ChaveAcesso: sem espacos, maiuscula, sem prefixo NFS/NFE/CTE. */
    val valor: String,
    /** Tipo pelo modelo da chave; null para modelo de 44 caracteres que nao e 55/57 (NF3e, NFCom...). */
    val tipo: TipoNotaEntrada?,
    /** Modelo fiscal da chave de 44 (55, 57...); null nas demais. */
    val modeloFiscal: String?,
    /** CNPJ (ou CPF completado com zeros) do emitente, 14 caracteres. */
    val emitente: String,
    /** Numero do documento embutido na chave; null no id de DPS. */
    val numero: BigInteger?,
    /** Serie embutida na chave de 44 (3 digitos com zeros); null nas demais. */
    val serie: String?,
) {
    sealed interface Leitura {
        data object Ausente : Leitura
        data object Invalida : Leitura
        data class Valida(val chave: ChaveAcessoNota) : Leitura
    }

    companion object {
        private val CHAVE_44 = Regex("[0-9]{6}[0-9A-Z]{12}[0-9]{26}")
        private val CHAVE_50 = Regex("[0-9]{50}")
        private val DPS = Regex("DPS[0-9]{8}[0-9A-Z]{12}[0-9]{22}")

        fun ler(texto: String?): Leitura {
            val limpo = texto?.filterNot(Char::isWhitespace)?.uppercase().orEmpty()
            if (limpo.isEmpty()) return Leitura.Ausente
            val valor = when {
                limpo.startsWith("NFS") && CHAVE_50.matches(limpo.substring(3)) -> limpo.substring(3)
                (limpo.startsWith("NFE") || limpo.startsWith("CTE")) && CHAVE_44.matches(limpo.substring(3)) -> limpo.substring(3)
                else -> limpo
            }
            val chave = when {
                CHAVE_44.matches(valor) -> {
                    val modelo = valor.substring(20, 22)
                    ChaveAcessoNota(
                        valor = valor,
                        tipo = when (modelo) { "55" -> TipoNotaEntrada.NFE; "57" -> TipoNotaEntrada.CTE; else -> null },
                        modeloFiscal = modelo,
                        emitente = valor.substring(6, 20),
                        numero = BigInteger(valor.substring(25, 34)),
                        serie = valor.substring(22, 25),
                    )
                }
                CHAVE_50.matches(valor) -> ChaveAcessoNota(
                    valor = valor, tipo = TipoNotaEntrada.NFSE, modeloFiscal = null,
                    emitente = valor.substring(9, 23), numero = BigInteger(valor.substring(23, 36)), serie = null,
                )
                DPS.matches(valor) -> ChaveAcessoNota(
                    valor = valor, tipo = TipoNotaEntrada.NFSE, modeloFiscal = null,
                    emitente = valor.substring(11, 25), numero = null, serie = null,
                )
                else -> return Leitura.Invalida
            }
            return Leitura.Valida(chave)
        }
    }
}

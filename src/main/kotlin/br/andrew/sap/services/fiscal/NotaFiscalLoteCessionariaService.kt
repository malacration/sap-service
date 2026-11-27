package br.andrew.sap.services.fiscal

import br.andrew.sap.infrastructure.odata.Condicao
import br.andrew.sap.infrastructure.odata.Filter
import br.andrew.sap.infrastructure.odata.Predicate
import br.andrew.sap.model.exceptions.SapGenericException
import br.andrew.sap.model.fiscal.CessionariaEditRequest
import br.andrew.sap.model.fiscal.CessionariaInvalidaException
import br.andrew.sap.model.fiscal.CessionariaLote
import br.andrew.sap.model.fiscal.CessionariaRequest
import br.andrew.sap.model.fiscal.CessionariaUdo
import br.andrew.sap.model.sistema.SapEnvrioment
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.security.AuthService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/** Cadastro de cessionaria (UDO NFL_CESSIONARIA) pela tela do lote. Cria e edita nome/nome do PDF; Code = CNPJ so com digitos. */
@Service
class NotaFiscalLoteCessionariaService(env: SapEnvrioment, restTemplate: RestTemplate, authService: AuthService)
    : EntitiesService<CessionariaUdo>(env, restTemplate, authService) {

    override fun path(): String = "/b1s/v1/NFL_CESSIONARIA"

    fun criar(request: CessionariaRequest): CessionariaLote {
        val nova = validar(request)
        // Procura por Code (inclui inativas): a chave e o CNPJ, entao repetir seria erro do SAP de qualquer jeito.
        if (get(Filter(Predicate("Code", nova.Code!!, Condicao.EQUAL))).tryGetValues<CessionariaUdo>().isNotEmpty())
            throw CessionariaInvalidaException(MSG_DUPLICADA, HttpStatus.CONFLICT)
        try {
            save(nova)
        } catch (e: Exception) {
            // Cadastro simultaneo do mesmo CNPJ passa da checagem acima; o SAP recusa a chave repetida.
            if (chaveRepetida(e))
                throw CessionariaInvalidaException(MSG_DUPLICADA, HttpStatus.CONFLICT)
            throw e
        }
        return CessionariaLote(nova.Code!!, nova.Name!!, nova.U_Telefone, nova.U_NomePdf)
    }

    // Edita so Name e U_NomePdf (Code/CNPJ, telefone e ativo ficam). nomePdf ausente mantem; vazio grava vazio: o termo cai no nome.
    fun editar(codigo: String, request: CessionariaEditRequest): CessionariaLote {
        val nome = nomeValido(request.nome)
        val nomePdf = request.nomePdf?.trim().orEmpty()
        if (nomePdf.length > MAX_NOME_PDF)
            throw CessionariaInvalidaException("O nome para o PDF pode ter no maximo $MAX_NOME_PDF caracteres.")
        // Code so tem digitos; qualquer outra coisa nao existe (e nao vai montar filtro OData).
        val atual = codigo.takeIf { it.isNotEmpty() && it.all { c -> c in '0'..'9' } }?.let {
            get(Filter(Predicate("Code", it, Condicao.EQUAL))).tryGetValues<CessionariaUdo>().firstOrNull()
        } ?: throw CessionariaInvalidaException("Cessionaria nao encontrada.", HttpStatus.NOT_FOUND)
        // Mapa e nao CessionariaUdo: o JSON do UDO omite campo vazio e o SAP manteria o NomePdf antigo.
        // Sem nomePdf no pedido, U_NomePdf nem vai no PATCH (o gravado fica).
        val campos = mutableMapOf<String, Any>("Name" to nome)
        if (request.nomePdfInformado) campos["U_NomePdf"] = nomePdf
        // Chave string entre aspas: sem elas o CNPJ com zeros a esquerda vira numero (toIntOrNull) e o SAP nao acha.
        update(campos, "'$codigo'")
        return CessionariaLote(codigo, nome, atual.U_Telefone, if (request.nomePdfInformado) nomePdf else atual.U_NomePdf)
    }

    companion object {
        private const val MSG_DUPLICADA = "Ja existe uma cessionaria cadastrada com este CNPJ."
        private const val MAX_NOME = 100
        val FORMATO_CNPJ = Regex("[0-9./\\- ]*")

        // Chave repetida: codigo estruturado do SAP (-2035, vale em qualquer idioma); o texto ("already exists" /
        // "duplicate") fica so como reserva para erro que nao trouxe o codigo.
        const val CODIGO_SAP_JA_EXISTE = "-2035"

        fun chaveRepetida(e: Throwable): Boolean =
            generateSequence(e) { it.cause }.take(5).any {
                if (it is SapGenericException && it.erro.error.code.trim() == CODIGO_SAP_JA_EXISTE) return@any true
                val m = it.message?.lowercase().orEmpty()
                m.contains("already exists") || m.contains("duplicate")
            }
        private const val MAX_TELEFONE = 20
        private const val MAX_NOME_PDF = 254

        private fun nomeValido(bruto: String?): String {
            val nome = bruto?.trim().orEmpty()
            if (nome.isEmpty())
                throw CessionariaInvalidaException("Informe o nome da cessionaria.")
            if (nome.length > MAX_NOME)
                throw CessionariaInvalidaException("O nome da cessionaria pode ter no maximo $MAX_NOME caracteres.")
            return nome
        }

        fun validar(request: CessionariaRequest): CessionariaUdo {
            val nome = nomeValido(request.nome)
            val nomePdf = request.nomePdf?.trim().orEmpty()
            if (nomePdf.isEmpty())
                throw CessionariaInvalidaException("Informe o nome da cessionaria para o PDF.")
            if (nomePdf.length > MAX_NOME_PDF)
                throw CessionariaInvalidaException("O nome para o PDF pode ter no maximo $MAX_NOME_PDF caracteres.")
            val cnpj = request.cnpj.orEmpty()
            // So digitos ASCII e a pontuacao do CNPJ; letra ou digito de outro alfabeto nao e descartado em silencio.
            if (!FORMATO_CNPJ.matches(cnpj))
                throw CessionariaInvalidaException("CNPJ invalido.")
            val digitos = cnpj.filter { it in '0'..'9' }
            if (digitos.isEmpty())
                throw CessionariaInvalidaException("Informe o CNPJ da cessionaria.")
            if (!cnpjValido(digitos))
                throw CessionariaInvalidaException("CNPJ invalido.")
            val telefone = request.telefone?.trim()?.ifEmpty { null }
            if (telefone != null && telefone.length > MAX_TELEFONE)
                throw CessionariaInvalidaException("O telefone pode ter no maximo $MAX_TELEFONE caracteres.")
            return CessionariaUdo(digitos, nome, formatar(digitos), telefone, nomePdf, "Y")
        }

        private fun formatar(c: String) =
            "${c.substring(0, 2)}.${c.substring(2, 5)}.${c.substring(5, 8)}/${c.substring(8, 12)}-${c.substring(12)}"

        // Os dois digitos verificadores (modulo 11) dos 12 primeiros digitos de um CNPJ.
        fun digitosVerificadores(base12: String): String {
            fun dv(base: String): Int {
                val pesos = if (base.length == 12) intArrayOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
                else intArrayOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
                val resto = base.indices.sumOf { (base[it] - '0') * pesos[it] } % 11
                return if (resto < 2) 0 else 11 - resto
            }
            val d1 = dv(base12)
            return "$d1${dv(base12 + d1)}"
        }

        // 14 digitos, nao todos iguais, e os dois digitos verificadores conferem.
        fun cnpjValido(c: String): Boolean {
            if (c.length != 14 || c.all { it == c[0] })
                return false
            return digitosVerificadores(c.substring(0, 12)) == c.substring(12)
        }
    }
}

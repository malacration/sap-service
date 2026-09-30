package br.andrew.sap.security.acesso

import br.andrew.sap.model.acesso.AcessoRegrasVersao
import br.andrew.sap.services.security.acesso.RegrasAcessoRepositorio
import br.andrew.sap.services.security.acesso.RegrasConflitoException
import java.util.TreeMap

/** O ACESSO_REGRAS do SAP, em memoria: Code unico, listagem sem o documento, mais nova primeiro. */
class RepositorioEmMemoria : RegrasAcessoRepositorio {

    val linhas = TreeMap<Int, AcessoRegrasVersao>()

    /** Se preenchido, qualquer leitura falha com esta excecao (SAP fora do ar). */
    var falhaNaLeitura: (() -> Exception?)? = null
    var leituras = 0
    var buscas = 0

    private fun verificar() {
        leituras++
        falhaNaLeitura?.invoke()?.let { throw it }
    }

    override fun ultimaVersao(): Int? {
        verificar()
        return if (linhas.isEmpty()) null else linhas.lastKey()
    }

    override fun buscar(versao: Int): AcessoRegrasVersao? {
        verificar()
        buscas++
        return linhas[versao]
    }

    override fun listar(limite: Int): List<AcessoRegrasVersao> {
        verificar()
        return linhas.descendingMap().values.take(limite).map {
            AcessoRegrasVersao(
                it.Code, it.Name, null, it.U_IdEscrita, it.U_Usuario, it.U_UsuarioId,
                it.U_Data, it.U_Hora, it.U_Origem, it.U_Comentario, it.U_Resumo,
            )
        }
    }

    override fun gravar(versao: AcessoRegrasVersao) {
        val numero = versao.numeroDaVersao()
        val existente = linhas[numero]
        if (existente != null) {
            if (existente.U_IdEscrita == versao.U_IdEscrita) return
            throw RegrasConflitoException("versao $numero ja gravada")
        }
        linhas[numero] = versao
    }

    /** Insere uma versao "do SAP" com um documento arbitrario (inclusive lixo). */
    fun inserir(numero: Int, documentoJson: String?) {
        linhas[numero] = AcessoRegrasVersao(
            Code = AcessoRegrasVersao.codigo(numero), Name = AcessoRegrasVersao.codigo(numero),
            U_Documento = documentoJson, U_IdEscrita = "id-$numero",
        )
    }
}

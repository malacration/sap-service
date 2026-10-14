package br.andrew.sap.services.security.acesso

import br.andrew.sap.model.acesso.AcessoRegrasVersao

/**
 * Onde as versoes das regras ficam guardadas. Existe como interface para a logica de negocio e o
 * cache poderem ser testados com um repositorio em memoria, sem o SAP.
 */
interface RegrasAcessoRepositorio {

    /** Numero da versao mais nova, ou null se ainda nao ha nenhuma. Nao le o documento. */
    fun ultimaVersao(): Int?

    /** A versao completa, com o documento, ou null se nao existe. */
    fun buscar(versao: Int): AcessoRegrasVersao?

    /** Metadados das versoes (sem o documento), da mais nova para a mais antiga. */
    fun listar(limite: Int = 200): List<AcessoRegrasVersao>

    /**
     * Grava uma versao nova. Lanca [RegrasConflitoException] se o Code ja pertence a outra gravacao.
     * Regravar a mesma tentativa (mesmo `U_IdEscrita`) nao e conflito.
     */
    fun gravar(versao: AcessoRegrasVersao)
}

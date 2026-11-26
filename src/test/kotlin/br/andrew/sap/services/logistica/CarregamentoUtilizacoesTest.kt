package br.andrew.sap.services.logistica

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.core.convert.TypeDescriptor

/** carregamento.utilizacoes vazia ("") ou com item vazio ("9,,10") nao pode esconder todos os pedidos. */
class CarregamentoUtilizacoesTest {

    @Test
    fun `sem utilizacao valida vale 9`() {
        assertEquals(listOf(9), CarregamentoUtilizacoes.utilizacoesOuPadrao(emptyList()))
        assertEquals(listOf(9), CarregamentoUtilizacoes.utilizacoesOuPadrao(listOf(null)))
    }

    @Test
    fun `item vazio e ignorado`() {
        assertEquals(listOf(9, 10), CarregamentoUtilizacoes.utilizacoesOuPadrao(listOf(9, null, 10)))
    }

    @Test
    fun `valor da propriedade vira a lista que o construtor recebe`() {
        val conversao = ApplicationConversionService.getSharedInstance()
        val tipo = TypeDescriptor.collection(List::class.java, TypeDescriptor.valueOf(Int::class.javaObjectType))
        fun converter(valor: String) = CarregamentoUtilizacoes.utilizacoesOuPadrao(
            @Suppress("UNCHECKED_CAST") (conversao.convert(valor, TypeDescriptor.valueOf(String::class.java), tipo) as List<Int?>))

        assertEquals(listOf(9), converter(""))
        assertEquals(listOf(9, 10), converter("9,,10"))
        assertEquals(listOf(9, 10), converter(" 9 , 10 "))
    }
}

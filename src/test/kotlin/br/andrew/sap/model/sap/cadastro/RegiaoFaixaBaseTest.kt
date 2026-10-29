package br.andrew.sap.model.sap.cadastro

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

/**
 * A faixa de menor quantidade e a base da regiao e o minimo para entregar (tarefa "Remover bloqueio da
 * quantidade base do frete": cliente que so entrega a partir de 50 pacotes). Antes a base era fixa em 1.
 */
class RegiaoFaixaBaseTest {

    //100km, R$ 2,00 a cada 100km por unidade na base; R$ 1,50 a partir de 100
    private fun regiao(base: Int = 50) = Regiao().also {
        it.Code = "R1"
        it.addLocalidade("10", 100.0)
        it.addFaixa(qtdeMinima = base, valorKm = 2.0)
        it.addFaixa(qtdeMinima = 100, valorKm = 1.5)
    }

    @Test
    fun `base pode comecar em qualquer quantidade e define o minimo de entrega`() {
        val r = regiao()

        assertEquals(50, r.quantidadeMinimaEntrega())
        assertEquals(50, r.faixaBase()?.U_QtdeAte)
    }

    @Test
    fun `quantidade da base pode ser alterada e a base removida`() {
        val r = regiao()
        val base = r.faixaBase()!!

        r.atualizaFaixa(base.LineId!!, 40, 2.0)
        assertEquals(40, r.quantidadeMinimaEntrega())

        r.removeFaixa(base.LineId!!)
        assertEquals(100, r.quantidadeMinimaEntrega())
    }

    @Test
    fun `abaixo da base nao ha frete nem entrega`() {
        val r = regiao()

        assertNull(r.calcularFrete("10", 30.0))
        val erro = assertThrows<Exception> { r.validaQuantidadeMinimaEntrega(30.0) }
        assertTrue(erro.message!!.contains("mínima para entrega nesta região é 50"), erro.message)
        assertTrue(erro.message!!.contains("tem 30"), erro.message)
        assertDoesNotThrow { r.validaQuantidadeMinimaEntrega(50.0) }
    }

    @Test
    fun `documento abaixo da base passa pela faixa base quando o pedido inteiro alcanca o minimo`() {
        val r = regiao()

        //pedido de 60 dividido em 30 + 30: cada documento paga a faixa base pela propria quantidade
        assertEquals(60.0, r.calcularFrete("10", 30.0, quantidadePedido = 60.0)!!, 0.001)
        assertNull(r.calcularFrete("10", 30.0, quantidadePedido = 40.0))
        //a faixa continua sendo a do proprio documento quando ele alcanca uma
        assertEquals(150.0, r.calcularFrete("10", 100.0, quantidadePedido = 160.0)!!, 0.001)
    }

    @Test
    fun `regiao sem faixa nao tem minimo`() {
        assertNull(Regiao().quantidadeMinimaEntrega())
        assertDoesNotThrow { Regiao().validaQuantidadeMinimaEntrega(1.0) }
    }
}

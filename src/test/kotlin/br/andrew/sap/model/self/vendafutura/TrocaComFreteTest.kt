package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sistema.Comissao
import br.andrew.sap.services.comercial.FreteContratoService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.pricing.ComissaoService
import br.andrew.sap.services.stock.ItemsService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A troca passou a recalcular o frete. Antes nao recalculava: total() soma U_valorFrete, entao
 * ele aparecia nos dois lados da subtracao do delta e se cancelava - trocar 20 itens de A por 5
 * de B mudava a quantidade (e o frete devido) sem o contrato registrar nada.
 *
 * O recalculo usa a regiao VIGENTE da filial e a tabela de preco de agora, nao a da assinatura.
 */
class TrocaComFreteTest {

    private val itemService = mock<ItemsService>()
    private val comissaoService = mock<ComissaoService>().also {
        whenever(it.getByIdTabela(any())).doReturn(Comissao("1", 5.0))
    }

    /** Regiao de 100km com faixa de R$ 10 a cada 100km: frete = 10 x quantidade. */
    private fun regiao(code: String, valorKm: Double = 10.0) = Regiao().also {
        it.Code = code
        it.U_Ativa = "1"
        it.U_Filial = 2
        it.addLocalidade("20", 100.0)
        it.addFaixa(qtdeMinima = 1, valorKm = valorKm)
    }

    private fun freteService(vararg regioes: Regiao): FreteContratoService {
        val regiaoService = mock<RegiaoService>().also {
            whenever(it.getTodas(anyOrNull())).doReturn(regioes.toList())
        }
        return FreteContratoService(regiaoService, mock(), mock())
    }

    private fun anyOrNull(): String? = org.mockito.ArgumentMatchers.any()

    private fun contrato(quantidade: Double, frete: Double, localidade: Int? = 20): Contrato {
        val item = Item("A", "", 10.0, quantidade, 10.0, 0.0, 10.0, "KG").also { it.LineId = 0 }
        return Contrato(1, "C0001", mutableListOf(item), 1, "", 2, frete).also {
            it.DocEntry = 1
            it.U_Localidade = localidade
            it.U_RegiaoCode = "REGIAO-ANTIGA"
        }
    }

    private fun trocaDeItem(qtdSaida: Double, qtdEntrada: Double) = PedidoTroca(
        "1",
        listOf(ItemTroca("A", qtdSaida, 10.0)),
        listOf(Product("B", qtdEntrada.toString(), "10.0", 0).also {
            it.ItemDescription = "Descricao"
            it.MeasureUnit = "un"
            it.PriceList = 1
            it.U_preco_negociado = 10.0
            it.DiscountPercent = 0.0
        }))

    @Test
    fun `troca recalcula o frete pela nova quantidade`() {
        val contrato = contrato(quantidade = 20.0, frete = 200.0)

        contrato.troca(trocaDeItem(20.0, 5.0), itemService, comissaoService, freteService(regiao("VIGENTE")))

        //5 itens x R$ 10 por item = 50,00 (antes ficava travado nos 200,00 dos 20 itens)
        assertEquals(50.0, contrato.U_valorFrete)
    }

    /** A diferenca de frete entra no delta que ajusta os adiantamentos. */
    @Test
    fun `delta da troca inclui a diferenca de frete`() {
        val contrato = contrato(quantidade = 20.0, frete = 200.0)

        val delta = contrato.troca(trocaDeItem(20.0, 5.0), itemService, comissaoService,
            freteService(regiao("VIGENTE")))

        //produtos: 50 - 200 = -150 | frete: 50 - 200 = -150 | total -300
        assertEquals("-300.00", delta.toString())
    }

    /**
     * O U_RegiaoCode gravado e historico: o recalculo usa a regiao vigente da filial, com a
     * tabela de preco de agora. Preco do frete pode ter mudado entre a assinatura e a troca.
     */
    @Test
    fun `usa a regiao vigente, nao a gravada no contrato`() {
        val contrato = contrato(quantidade = 20.0, frete = 200.0)

        //a vigente cobra o dobro do que valia quando o contrato foi fechado
        contrato.troca(trocaDeItem(20.0, 5.0), itemService, comissaoService,
            freteService(regiao("VIGENTE", valorKm = 20.0)))

        assertEquals(100.0, contrato.U_valorFrete)
    }

    @Test
    fun `troca falha quando a filial nao tem regiao ativa`() {
        val contrato = contrato(quantidade = 20.0, frete = 200.0)

        val erro = assertThrows<Exception> {
            contrato.troca(trocaDeItem(20.0, 5.0), itemService, comissaoService, freteService())
        }

        assertEquals(true, erro.message!!.contains("Nao existe regiao de frete ativa"), erro.message)
    }

    @Test
    fun `troca falha quando a regiao vigente nao cobre a localidade do contrato`() {
        val contrato = contrato(quantidade = 20.0, frete = 200.0, localidade = 99)

        val erro = assertThrows<Exception> {
            contrato.troca(trocaDeItem(20.0, 5.0), itemService, comissaoService,
                freteService(regiao("VIGENTE")))
        }

        assertEquals(true, erro.message!!.contains("nao esta vinculada a regiao"), erro.message)
    }

    /** Contrato legado nao tem destino: mantem o frete e a troca segue, sem recalcular. */
    @Test
    fun `contrato sem localidade mantem o frete intacto`() {
        val contrato = contrato(quantidade = 20.0, frete = 200.0, localidade = null)

        contrato.troca(trocaDeItem(20.0, 5.0), itemService, comissaoService, freteService(regiao("VIGENTE")))

        assertEquals(200.0, contrato.U_valorFrete)
    }
}

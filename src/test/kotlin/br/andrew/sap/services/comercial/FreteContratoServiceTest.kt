package br.andrew.sap.services.comercial

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.logistica.RegiaoService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * A regiao que vale e sempre a VIGENTE da filial - nunca uma gravada em documento ou contrato.
 * Duas localidades estao "na mesma regiao" quando a vigente cobre as duas.
 */
class FreteContratoServiceTest {

    private val regiaoService = mock<RegiaoService>()
    private val localidadeService = mock<LocalidadeService>()
    private val businessPartnersService = mock<BusinessPartnersService>()

    private val service = FreteContratoService(regiaoService, localidadeService, businessPartnersService)

    private fun regiao(code: String, filial: Int = 2, ativa: Boolean = true,
                       localidades: List<Pair<String, Double>> = listOf("20" to 100.0)) = Regiao().also {
        it.Code = code
        it.U_Ativa = if(ativa) "1" else "0"
        it.U_Filial = filial
        localidades.forEach { (local, dist) -> it.addLocalidade(local, dist) }
        it.addFaixa(qtdeMinima = 1, valorKm = 10.0)
    }

    private fun comRegioes(vararg regioes: Regiao) {
        whenever(regiaoService.getTodas(ArgumentMatchers.any())).doReturn(regioes.toList())
    }

    private fun cliente(vararg enderecos: Address) {
        val bp = BusinessPartner().also { it.setAddresses(enderecos.toList()) }
        whenever(businessPartnersService.getById(any<String>()))
            .doReturn(OData(linkedMapOf("value" to OData().mapper.writeValueAsString(bp))))
    }

    private fun endereco(nome: String, localidade: Int?, tipo: AddresType = AddresType.bo_ShipTo) = Address().also {
        it.addressName = nome
        it.addressType = tipo
        it.U_Localidade = localidade
    }

    @Test
    fun `escolhe a regiao ativa da filial, ignorando as inativas e as de outra filial`() {
        comRegioes(
            regiao("INATIVA", ativa = false),
            regiao("OUTRA-FILIAL", filial = 6),
            regiao("VIGENTE"))

        assertEquals("VIGENTE", service.regiaoVigente(2).Code)
    }

    @Test
    fun `falha quando a filial nao tem regiao ativa`() {
        comRegioes(regiao("INATIVA", ativa = false))

        val erro = assertThrows<Exception> { service.regiaoVigente(2) }

        assertTrue(erro.message!!.contains("filial 2"), erro.message)
    }

    @Test
    fun `falha quando a filial nao foi informada`() {
        val erro = assertThrows<Exception> { service.regiaoVigente(null) }

        assertTrue(erro.message!!.contains("filial"), erro.message)
    }

    /** 100km / 100 x R$10 x 5 itens = R$ 50 */
    @Test
    fun `calcula o frete pela regiao vigente`() {
        comRegioes(regiao("VIGENTE"))

        assertEquals(50.0, service.calcula(2, 20, 5.0))
    }

    @Test
    fun `falha quando a regiao vigente nao cobre a localidade`() {
        comRegioes(regiao("VIGENTE"))
        whenever(localidadeService.getById(any<String>())).doThrow(RuntimeException("sem nome"))

        val erro = assertThrows<Exception> { service.calcula(2, 99, 5.0) }

        assertTrue(erro.message!!.contains("nao esta vinculada a regiao de frete VIGENTE"), erro.message)
    }

    /** Regiao cobre a localidade mas sem distancia cadastrada: calcularFrete devolve null. */
    @Test
    fun `falha quando falta distancia na localidade`() {
        comRegioes(regiao("VIGENTE", localidades = listOf("20" to 0.0)).also {
            it.linhas.first().U_Distancia = null
        })
        whenever(localidadeService.getById(any<String>())).doThrow(RuntimeException("sem nome"))

        val erro = assertThrows<Exception> { service.calcula(2, 20, 5.0) }

        assertTrue(erro.message!!.contains("falta a distancia"), erro.message)
    }

    @Test
    fun `dois enderecos na mesma regiao`() {
        val regiao = regiao("VIGENTE", localidades = listOf("20" to 100.0, "30" to 250.0))

        assertTrue(regiao.temLocalidade("20"))
        assertTrue(regiao.temLocalidade("30"))
    }

    @Test
    fun `endereco de outra regiao nao e coberto pela vigente`() {
        val regiao = regiao("VIGENTE", localidades = listOf("20" to 100.0))

        assertTrue(!regiao.temLocalidade("30"))
    }

    @Test
    fun `localidade do endereco escolhido pelo nome`() {
        cliente(endereco("ENTREGA", 20), endereco("FAZENDA", 30))

        assertEquals(30, service.localidadeDoEndereco("C0001", "FAZENDA", AddresType.bo_ShipTo))
    }

    /** Chave do endereco no SAP e (nome, tipo): o de cobranca de mesmo nome nao pode ser lido. */
    @Test
    fun `nao confunde endereco de cobranca de mesmo nome`() {
        cliente(
            endereco("COBRANCA", 10, AddresType.bo_BillTo),
            endereco("COBRANCA", 30, AddresType.bo_ShipTo))

        assertEquals(30, service.localidadeDoEndereco("C0001", "COBRANCA", AddresType.bo_ShipTo))
    }

    @Test
    fun `sem shipToCode cai no primeiro endereco de entrega`() {
        cliente(endereco("ENTREGA", 20), endereco("FAZENDA", 30))

        assertEquals(20, service.localidadeDoEndereco("C0001", null, AddresType.bo_ShipTo))
    }

    /**
     * Cliente antigo nao manda shipToCode. A validacao caia no primeiro endereco da colecao,
     * mas o SAP aplicava o endereco padrao DELE - podia recusar pela regiao errada, ou aceitar
     * com a entrega caindo fora da regiao negociada. O nome resolvido tem que voltar para ser
     * gravado no documento.
     */
    @Test
    fun `devolve o nome do endereco usado, para o documento fixar o mesmo`() {
        cliente(endereco("ENTREGA", 20), endereco("FAZENDA", 30))

        val resolvido = service.enderecoEntrega("C0001", null, AddresType.bo_ShipTo)

        assertEquals("ENTREGA", resolvido.addressName)
        assertEquals(20, resolvido.localidade)
    }

    @Test
    fun `devolve o nome do endereco escolhido explicitamente`() {
        cliente(endereco("ENTREGA", 20), endereco("FAZENDA", 30))

        val resolvido = service.enderecoEntrega("C0001", "FAZENDA", AddresType.bo_ShipTo)

        assertEquals("FAZENDA", resolvido.addressName)
        assertEquals(30, resolvido.localidade)
    }

    @Test
    fun `falha quando o endereco nao tem localidade`() {
        cliente(endereco("ENTREGA", null))

        val erro = assertThrows<Exception> {
            service.localidadeDoEndereco("C0001", "ENTREGA", AddresType.bo_ShipTo)
        }

        assertTrue(erro.message!!.contains("nao possui localidade"), erro.message)
    }

    @Test
    fun `falha quando o endereco nao existe no cliente`() {
        cliente(endereco("ENTREGA", 20))

        val erro = assertThrows<Exception> {
            service.localidadeDoEndereco("C0001", "FAZENDA", AddresType.bo_ShipTo)
        }

        assertTrue(erro.message!!.contains("FAZENDA"), erro.message)
    }
}

package br.andrew.sap.controllers.cadastro

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import br.andrew.sap.model.forca.Cliente
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.comercial.FormaPagamentoService
import br.andrew.sap.model.payment.PaymentMethodDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import br.andrew.sap.model.enums.Cancelled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Cadastro de cliente pela tela (POST /business-partners/novo): CPF/CNPJ duplicado e barrado, as
 * formas de pagamento escolhidas viram BPPaymentMethods (CRD2) e, sem vendedor escolhido, fica o
 * de quem cadastrou.
 */
class NovoParceiroControllerTest {

    private val service = mock<BusinessPartnersService>()
    private val formas = mock<FormaPagamentoService>().also {
        whenever(it.liberadas()).doReturn(listOf("AVISTA", "BB-RC-BOL-1199").map { codigo ->
            PaymentMethodDto(codigo, codigo, null, null, null) })
    }
    private val controller = BusinessPartnersController(service, mock(), mock(), mock(), mock(), mock(), mock(), mock(), formas)
    private val mapper = ObjectMapper().registerKotlinModule()

    private fun vendedor(id: String) = User(id, "Vendedor", UserOriginEnum.SalePerson, "vend", bussinesPlace = listOf())
    private fun funcionario(id: String) = User(id, "Funcionario", UserOriginEnum.EmployeesInfo, "func", bussinesPlace = listOf())

    private val enderecoPadrao = """{"rua":"RUA A","numero":"10","bairro":"CENTRO","cidade":"PORTO VELHO","estado":"RO",
        "cep":"76800-000","pais":"BR","tipoEndereco":"RUA","codigoMunicipio":"4852"}"""

    /** Sem "endereco" no JSON, entra um de entrega valido: a maioria dos testes nao e sobre endereco. */
    private fun cliente(json: String): Cliente {
        val arvore = mapper.readTree(json) as com.fasterxml.jackson.databind.node.ObjectNode
        if(!arvore.has("endereco"))
            arvore.set<com.fasterxml.jackson.databind.JsonNode>("endereco", mapper.readTree(enderecoPadrao))
        return mapper.treeToValue(arvore, Cliente::class.java)
    }

    private fun clienteSemEndereco(json: String): Cliente = mapper.readValue(json, Cliente::class.java)

    private fun salvo(): BusinessPartner {
        val captor = argumentCaptor<BusinessPartner>()
        verify(service).save(captor.capture())
        return captor.firstValue
    }

    private fun aceitaSalvar() {
        whenever(service.save(any())).doReturn(OData(linkedMapOf("CardCode" to "CLI0001")))
    }

    @Test
    fun `CPF ja cadastrado e recusado sem salvar`() {
        whenever(service.cardCodeDoCpfCnpj(any())).doReturn("CLI0999")

        val erro = assertThrows<Exception> {
            controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"123.456.789-09","formasPagamento":["AVISTA"]}"""), vendedor("5"))
        }

        assertEquals("Ja existe o parceiro CLI0999 com o CPF/CNPJ 123.456.789-09", erro.message)
        verify(service, never()).save(any())
    }

    @Test
    fun `documento que nao e CPF nem CNPJ e recusado`() {
        val erro = assertThrows<Exception> {
            controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345","formasPagamento":["AVISTA"]}"""), vendedor("5"))
        }
        assertEquals("CPF/CNPJ invalido: informe 11 digitos (CPF) ou 14 (CNPJ)", erro.message)
    }

    @Test
    fun `sem forma de pagamento e recusado`() {
        val erro = assertThrows<Exception> {
            controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":[" "]}"""), vendedor("5"))
        }
        assertEquals("Selecione ao menos uma forma de pagamento", erro.message)
        verify(service, never()).save(any())
    }

    @Test
    fun `formas escolhidas viram BPPaymentMethods sem repetir`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909",
            "formasPagamento":["AVISTA","BB-RC-BOL-1199","AVISTA"]}"""), vendedor("5"))

        assertEquals(listOf("AVISTA", "BB-RC-BOL-1199"), salvo().getBPPaymentMethods().map { it.paymentMethodCode })
    }

    @Test
    fun `sem endereco de entrega e recusado`() {
        val erro = assertThrows<Exception> {
            controller.novo(clienteSemEndereco("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), vendedor("5"))
        }
        assertEquals("Informe o endereco de entrega", erro.message)
        verify(service, never()).save(any())
    }

    /** O SAP exige CRD1.AddrType: sem ele a TransactionNotification recusa o cadastro. */
    @Test
    fun `sem tipo de logradouro na entrega ou na cobranca e recusado`() {
        val semTipo = enderecoPadrao.replace("\"tipoEndereco\":\"RUA\"", "\"tipoEndereco\":\" \"")
        val erroEntrega = assertThrows<Exception> {
            controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"],"endereco":$semTipo}"""), vendedor("5"))
        }
        assertEquals("Informe o tipo de logradouro do endereco de entrega", erroEntrega.message)

        val erroCobranca = assertThrows<Exception> {
            controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"],"enderecoCobranca":$semTipo}"""), vendedor("5"))
        }
        assertEquals("Informe o tipo de logradouro do endereco de cobranca", erroCobranca.message)
        verify(service, never()).save(any())
    }

    @Test
    fun `tipo de logradouro vai em TypeOfAddress dos dois enderecos`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), vendedor("5"))

        val json = mapper.readTree(mapper.writeValueAsString(salvo()))
        assertEquals(listOf("RUA", "RUA"), json.get("BPAddresses").map { it.get("TypeOfAddress").asText() })
    }

    /** Forma fora do portal seria vinculada mas nunca apareceria na tela de venda. */
    @Test
    fun `forma nao liberada para o portal e recusada`() {
        val erro = assertThrows<Exception> {
            controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA","SC-RC-BOL-17609"]}"""), vendedor("5"))
        }

        assertEquals("Forma de pagamento nao liberada para o portal: SC-RC-BOL-17609", erro.message)
        verify(service, never()).save(any())
    }

    @Test
    fun `sem vendedor escolhido fica o de quem cadastrou`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), vendedor("5"))

        assertEquals(5, salvo().salesPersonCode)
    }

    @Test
    fun `vendedor escolhido na tela prevalece`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","idVendedor":"7","formasPagamento":["AVISTA"]}"""), vendedor("5"))

        assertEquals(7, salvo().salesPersonCode)
    }

    /** Sem a propriedade, nada de BPBranchAssignment: o SAP decide (comportamento de antes). */
    @Test
    fun `sem filiais configuradas nao manda vinculo de filial`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), vendedor("5"))

        assertEquals(null, salvo().bpBranchAssignment)
    }

    @Test
    fun `cliente novo nasce so nas filiais configuradas`() {
        val comFiliais = BusinessPartnersController(service, mock(), mock(), mock(), mock(), mock(), mock(), mock(), formas, listOf(2, 4, 11, 17, 18, 2))
        aceitaSalvar()

        comFiliais.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), vendedor("5"))

        val vinculos = salvo().bpBranchAssignment!!
        assertEquals(listOf("2", "4", "11", "17", "18"), vinculos.map { it.bplid })
        assertTrue(vinculos.all { it.DisabledForBP == Cancelled.tNO })
    }

    /** O Service Layer so aceita os nomes exatos dos campos do BPBranchAssignmentItem. */
    @Test
    fun `vinculo de filial sai com os nomes que o Service Layer aceita`() {
        val comFiliais = BusinessPartnersController(service, mock(), mock(), mock(), mock(), mock(), mock(), mock(), formas, listOf(11))
        aceitaSalvar()

        comFiliais.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), vendedor("5"))

        val json = mapper.readTree(mapper.writeValueAsString(salvo()))
        val vinculo = json.get("BPBranchAssignment").single()
        assertEquals(setOf("BPLID", "DisabledForBP"), vinculo.fieldNames().asSequence().toSet())
        assertEquals("11", vinculo.get("BPLID").asText())
        assertEquals("tNO", vinculo.get("DisabledForBP").asText())
    }

    /** County do SAP e o codigo do municipio; City fica com o nome. */
    @Test
    fun `endereco grava o codigo do municipio em County`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"],
            "endereco":{"rua":"RUA A","numero":"10","bairro":"CENTRO","cidade":"PORTO VELHO","estado":"RO",
                "cep":"76800-000","pais":"BR","tipoEndereco":"RUA","codigoMunicipio":"4852"}}"""), vendedor("5"))

        val entrega = salvo().getAddresses().first()
        assertEquals("4852", entrega.County)
        assertEquals("PORTO VELHO", entrega.City)
    }

    private val enderecoJson = """{"rua":"RUA A","numero":"10","bairro":"CENTRO","cidade":"PORTO VELHO","estado":"RO",
        "cep":"76800-000","pais":"BR","tipoEndereco":"RUA","codigoMunicipio":"4852"}"""

    @Test
    fun `sem endereco de cobranca a cobranca copia a entrega`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"],
            "localidade":7,"endereco":$enderecoJson}"""), vendedor("5"))

        val (entrega, cobranca) = salvo().getAddresses()
        assertEquals(AddresType.bo_ShipTo, entrega.addressType)
        assertEquals("ENTREGA", entrega.addressName)
        assertEquals(7, entrega.U_Localidade)
        assertEquals(AddresType.bo_BillTo, cobranca.addressType)
        assertEquals("COBRANCA", cobranca.addressName)
        assertEquals("RUA A", cobranca.Street)
    }

    @Test
    fun `endereco de cobranca proprio vai para o bo_BillTo`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"],
            "endereco":$enderecoJson,
            "enderecoCobranca":{"rua":"AV B","numero":"200","bairro":"NOVA","cidade":"CACOAL","estado":"RO",
                "cep":"76960-000","pais":"BR","tipoEndereco":"AVENIDA","codigoMunicipio":"4840"}}"""), vendedor("5"))

        val (entrega, cobranca) = salvo().getAddresses()
        assertEquals("RUA A", entrega.Street)
        assertEquals(AddresType.bo_BillTo, cobranca.addressType)
        assertEquals("COBRANCA", cobranca.addressName)
        assertEquals("AV B", cobranca.Street)
        assertEquals("4840", cobranca.County)
        assertEquals(null, cobranca.U_Localidade)
    }

    /**
     * O id de quem nao e vendedor e o codigo de funcionario: virar SlpCode gravaria o cliente com o
     * vendedor errado ("12" aqui). Sem vendedor escolhido, fica sem vendedor (-1).
     */
    @Test
    fun `funcionario sem vendedor escolhido deixa sem vendedor`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","formasPagamento":["AVISTA"]}"""), funcionario("12"))

        assertEquals(-1, salvo().salesPersonCode)
    }

    @Test
    fun `funcionario que escolhe o vendedor cadastra`() {
        aceitaSalvar()

        controller.novo(cliente("""{"nome":"FULANO","cpfCnpj":"12345678909","idVendedor":"7","formasPagamento":["AVISTA"]}"""), funcionario("12"))

        assertEquals(7, salvo().salesPersonCode)
    }
}

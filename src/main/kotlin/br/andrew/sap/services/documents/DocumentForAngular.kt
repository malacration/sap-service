package br.andrew.sap.services.documents

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.stock.ItemsService
import org.springframework.security.core.Authentication

class DocumentForAngular {

    fun prepareToSave(pedido : Document, itemService: ItemsService, businessPartnersService: BusinessPartnersService,
                      regiaoService: RegiaoService, auth : Authentication): Document {
        validaFreteParaEntrega(pedido, businessPartnersService, regiaoService)
        pedido.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
        pedido.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        pedido.atualizaPrecoBase(itemService)
        pedido.u_pedido_update = "1"
        pedido.salesPersonCode = auth.principal.toString().toInt()
        return pedido
    }

    //diferenca tolerada entre o frete calculado pelo back e o que o front mandou -
    //cobre arredondamentos de exibicao (currency-input trabalha em centavos)
    private val TOLERANCIA_FRETE = 5.0

    /**
     * Incoterms 1 = entrega (ver DocumentStatementComponent.tipoEnvioChange no front).
     * Pedido de entrega sem localidade cadastrada no endereco de entrega do
     * cliente, sem regiao ativa cobrindo essa localidade/filial, ou com o
     * frete enviado divergente do recalculado aqui (fora da tolerancia),
     * bloqueia o registro - tudo recalculado direto no SAP, nao confia no
     * que o front mandou, pra nao dar pra contornar a validacao so alterando
     * a requisicao.
     */
    private fun validaFreteParaEntrega(pedido : Document, businessPartnersService: BusinessPartnersService, regiaoService: RegiaoService) {
        if(pedido.Incoterms != 1)
            return

        val bp = businessPartnersService.getById("'${pedido.CardCode}'").tryGetValue<BusinessPartner>()
        val enderecoEntrega = enderecoEntregaSelecionado(pedido, bp)
        val codLocalidade = enderecoEntrega?.U_Localidade
            ?: throw Exception("O cliente ${pedido.CardCode} nao possui localidade cadastrada no endereco de entrega - cadastre a localidade antes de finalizar a venda")

        val filial = pedido.getBPL_IDAssignedToInvoice().toIntOrNull()
        val regiao = regiaoService.getRegioesByLocalidade(codLocalidade.toString())
            .firstOrNull { it.ativa && it.U_Filial == filial }
            ?: throw Exception("Nao existe regiao de frete ativa cobrindo a localidade do cliente ${pedido.CardCode} para essa filial")

        val quantidade = pedido.DocumentLines.sumOf { it.Quantity.toDoubleOrNull() ?: 0.0 }
        val freteEsperado = regiao.calcularFrete(codLocalidade.toString(), quantidade)
            ?: throw Exception("Nao foi possivel calcular o frete na regiao ${regiao.Code} (falta distancia da localidade ou faixa de preco cadastrada)")

        val freteEnviado = pedido.documentAdditionalExpenses
            .filter { it.expenseCode == 1 }
            .sumOf { it.LineTotal }

        if(Math.abs(freteEsperado - freteEnviado) > TOLERANCIA_FRETE)
            throw Exception(
                "Valor do frete divergente do calculado pelo sistema " +
                "(enviado: R$ ${"%.2f".format(freteEnviado)}, esperado: R$ ${"%.2f".format(freteEsperado)})"
            )
    }

    private fun enderecoEntregaSelecionado(pedido: Document, bp: BusinessPartner): Address? {
        val enderecosEntrega = bp.getAddresses()
            .filter { it.addressType == AddresType.bo_ShipTo }
        val shipToCode = pedido.shipToCode?.trim()

        if(shipToCode.isNullOrBlank())
            return enderecosEntrega.firstOrNull()

        return enderecosEntrega.firstOrNull { it.addressName?.trim().equals(shipToCode, ignoreCase = true) }
            ?: throw Exception("Endereco de entrega $shipToCode nao encontrado no cliente ${pedido.CardCode}")
    }
}

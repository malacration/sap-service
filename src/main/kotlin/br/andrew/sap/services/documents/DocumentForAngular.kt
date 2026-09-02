package br.andrew.sap.services.documents

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.infrastructure.configurations.DistribuicaoCustoByBranchConfig
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.Address
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.model.sap.cadastro.Localidade
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.logistica.RegiaoService
import br.andrew.sap.services.stock.ItemsService
import org.springframework.security.core.Authentication

class DocumentForAngular {

    fun prepareToSave(pedido : Document, itemService: ItemsService, businessPartnersService: BusinessPartnersService,
                      regiaoService: RegiaoService, localidadeService: LocalidadeService,
                      auth : Authentication): Document {
        validaFreteParaEntrega(pedido, businessPartnersService, regiaoService, localidadeService)
        pedido.usaBrenchDefaultWarehouse(WarehouseDefaultConfig.warehouses)
        pedido.setDistribuicaoCusto(DistribuicaoCustoByBranchConfig.distibucoesCustos)
        pedido.atualizaPrecoBase(itemService)
        pedido.u_pedido_update = "1"
        //Documento de contrato ja traz o vendedor do contrato (PedidoRetirada.parse).
        //Sobrescrever aqui creditaria a entrega a quem operou a retirada - se for alguem do
        //faturamento, a comissao vai para a pessoa errada. Na venda normal o comportamento
        //continua o de sempre: quem monta o pedido e o vendedor.
        if(pedido.U_venda_futura == null)
            pedido.salesPersonCode = auth.principal.toString().toInt()
        return pedido
    }

    //Incoterms 9 = "Sem Frete" (ver incoterms.select.component.ts no front: 0 = por conta do
    //emitente, 1 = por conta do destinatario, 9 = sem frete)
    private val SEM_FRETE = 9

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
    private fun validaFreteParaEntrega(pedido : Document, businessPartnersService: BusinessPartnersService,
                                       regiaoService: RegiaoService, localidadeService: LocalidadeService) {
        //9 = "Sem Frete": nao ha o que conferir. null = chamador que nao e o portal (integracao,
        //sync offline), mantem o comportamento antigo de nao validar.
        val incoterms = pedido.incotermsEfetivo()
        if(incoterms == null || incoterms == SEM_FRETE)
            return

        //Entrega de venda futura tem regra de frete propria: o valor e o residual rateado do
        //contrato (PedidoRetirada.freteResidual), nao a tabela por km. Comparar os dois barraria
        //toda retirada - a tolerancia aqui e de R$ 5,00 e os numeros nao tem por que bater.
        //Quem valida o frete dela e a SBO_SP_VALIDACAO_VENDA_FUTURA, do lado do banco.
        if(pedido.U_venda_futura != null && pedido.U_entrega_vf == 1)
            return

        val bp = businessPartnersService.getById("'${pedido.CardCode}'").tryGetValue<BusinessPartner>()
        val enderecoEntrega = enderecoEntregaSelecionado(pedido, bp)
            ?: throw Exception("O cliente ${pedido.CardCode} nao possui endereco de entrega cadastrado - cadastre o endereco antes de finalizar a venda")
        val endereco = enderecoEntrega.addressName?.trim() ?: pedido.shipToCode
        val codLocalidade = enderecoEntrega.U_Localidade
            ?: throw Exception("O endereco de entrega '$endereco' do cliente ${pedido.CardCode} nao possui localidade cadastrada - cadastre a localidade antes de finalizar a venda")

        val localidade = descreveLocalidade(codLocalidade, localidadeService)
        val filial = pedido.getBPL_IDAssignedToInvoice().toIntOrNull()
        val regiao = regiaoService.getRegioesByLocalidade(codLocalidade.toString())
            .firstOrNull { it.ativa && it.U_Filial == filial }
            ?: throw Exception("A localidade $localidade, do endereco de entrega '$endereco' do cliente ${pedido.CardCode}, " +
                "nao esta vinculada a nenhuma regiao de frete ativa da filial $filial - vincule a localidade a uma regiao para calcular o frete")

        val quantidade = pedido.DocumentLines.sumOf { it.Quantity.toDoubleOrNull() ?: 0.0 }
        val freteEsperado = regiao.calcularFrete(codLocalidade.toString(), quantidade)
            ?: throw Exception("Nao foi possivel calcular o frete da localidade $localidade na regiao ${regiao.Code} - " +
                "falta a distancia da localidade nessa regiao ou a faixa de preco que cubra $quantidade unidades")

        val freteEnviado = pedido.documentAdditionalExpenses
            .filter { it.expenseCode == 1 }
            .sumOf { it.LineTotal }

        if(Math.abs(freteEsperado - freteEnviado) > TOLERANCIA_FRETE)
            throw Exception(
                "Valor do frete divergente do calculado pelo sistema " +
                "(enviado: R$ ${"%.2f".format(freteEnviado)}, esperado: R$ ${"%.2f".format(freteEsperado)})"
            )
    }

    /**
     * "12 - MANICORE" quando o nome esta cadastrado, so "12" quando nao da pra buscar.
     *
     * O codigo sozinho nao ajuda quem vende - a pessoa precisa saber QUAL localidade vincular a
     * regiao. A busca do nome so acontece no caminho de erro, e falha dela nunca pode mascarar o
     * erro de verdade: sem nome, a mensagem sai com o codigo e segue.
     */
    private fun descreveLocalidade(codLocalidade: Int, localidadeService: LocalidadeService): String {
        val nome = try {
            localidadeService.getById("'$codLocalidade'").tryGetValue<Localidade>().Name
        } catch (e: Exception) {
            null
        }
        return if(nome.isNullOrBlank()) "$codLocalidade" else "$codLocalidade - $nome"
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

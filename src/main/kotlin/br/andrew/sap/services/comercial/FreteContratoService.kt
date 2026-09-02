package br.andrew.sap.services.comercial

import br.andrew.sap.model.sap.cadastro.Localidade
import br.andrew.sap.model.sap.cadastro.Regiao
import br.andrew.sap.model.sap.partner.AddresType
import br.andrew.sap.model.sap.partner.BusinessPartner
import br.andrew.sap.services.cadastro.BusinessPartnersService
import br.andrew.sap.services.logistica.LocalidadeService
import br.andrew.sap.services.logistica.RegiaoService
import org.springframework.stereotype.Service

/**
 * Dono unico da regra "regiao vigente da filial" e do calculo de frete a partir dela.
 *
 * O criterio `ativa && U_Filial == filial` estava repetido inline no DocumentForAngular e no
 * componente Angular do pedido; aqui ele tem um lugar so.
 *
 * Regra central: a regiao NUNCA vem gravada de lugar nenhum. Mesmo o contrato de venda futura,
 * que guarda um U_RegiaoCode, guarda-o apenas como historico - todo calculo resolve a regiao
 * vigente da filial no momento em que acontece. Se a regiao vigente mudou, e ela que vale.
 */
@Service
class FreteContratoService(
    val regiaoService: RegiaoService,
    val localidadeService: LocalidadeService,
    val businessPartnersService: BusinessPartnersService) {

    /**
     * A regiao que vale para a filial agora. Varias regioes podem cobrir a mesma localidade, mas
     * so uma fica ativa por filial - por isso a filial e obrigatoria para desambiguar.
     */
    fun regiaoVigente(filial: Int?): Regiao {
        if(filial == null)
            throw Exception("A filial do documento nao foi informada, nao da para descobrir a regiao de frete")
        return regiaoService.getTodas(null)
            .firstOrNull { it.ativa && it.U_Filial == filial }
            ?: throw Exception("Nao existe regiao de frete ativa para a filial $filial - ative uma regiao antes de calcular o frete")
    }

    /**
     * Frete de uma localidade na regiao vigente da filial.
     *
     * Falha em vez de devolver zero: cada uma das tres situacoes abaixo e cadastro faltando, e
     * seguir com frete zerado esconderia o problema ate virar divergencia de valor no documento.
     */
    fun calcula(filial: Int?, codLocalidade: Int, quantidade: Double): Double {
        val regiao = regiaoVigente(filial)
        val local = codLocalidade.toString()

        if(!regiao.temLocalidade(local))
            throw Exception("A localidade ${descreve(codLocalidade)} nao esta vinculada a regiao de frete " +
                "${regiao.Code} (a vigente da filial $filial) - vincule a localidade a regiao para calcular o frete")

        return regiao.calcularFrete(local, quantidade)
            ?: throw Exception("Nao foi possivel calcular o frete da localidade ${descreve(codLocalidade)} na " +
                "regiao ${regiao.Code} - falta a distancia da localidade nessa regiao ou a faixa de preco " +
                "que cubra $quantidade unidades")
    }

    /**
     * Endereco de entrega efetivamente usado e a localidade dele. Chave do endereco no SAP e
     * (nome, tipo).
     *
     * Quem chama tem que gravar o [EnderecoEntregaResolvido.addressName] no documento. Sem
     * shipToCode o SAP aplica o endereco padrao DELE, que nao e necessariamente o primeiro da
     * colecao - validar um e entregar no outro deixava o pedido ser recusado pela regiao errada,
     * ou aceito com entrega fora da regiao negociada.
     */
    fun enderecoEntrega(cardCode: String, shipToCode: String?, addressType: AddresType): EnderecoEntregaResolvido {
        val bp = businessPartnersService.getById("'$cardCode'").tryGetValue<BusinessPartner>()
        val enderecos = bp.getAddresses().filter { it.addressType == addressType }

        val endereco = if(shipToCode.isNullOrBlank())
            enderecos.firstOrNull()
                ?: throw Exception("O cliente $cardCode nao possui endereco de entrega cadastrado")
        else
            enderecos.firstOrNull { it.addressName?.trim().equals(shipToCode.trim(), ignoreCase = true) }
                ?: throw Exception("Endereco de entrega $shipToCode nao encontrado no cliente $cardCode")

        val localidade = endereco.U_Localidade
            ?: throw Exception("O endereco de entrega '${endereco.addressName}' do cliente $cardCode nao possui " +
                "localidade cadastrada - cadastre a localidade antes de continuar")

        return EnderecoEntregaResolvido(endereco.addressName, localidade)
    }

    /** Localidade do endereco de entrega escolhido. */
    fun localidadeDoEndereco(cardCode: String, shipToCode: String?, addressType: AddresType): Int {
        return enderecoEntrega(cardCode, shipToCode, addressType).localidade
    }

    /**
     * O endereco que a validacao usou. O nome tem que ir no shipToCode do documento, senao o SAP
     * escolhe o padrao dele e entrega em outro lugar.
     */
    class EnderecoEntregaResolvido(val addressName: String?, val localidade: Int)

    /**
     * "12 - MANICORE" quando da para buscar o nome, so o codigo quando nao da.
     *
     * A busca do nome so acontece montando mensagem de erro, e falha dela nunca pode mascarar o
     * erro de verdade - por isso o catch amplo.
     */
    fun descreve(codLocalidade: Int): String {
        val nome = try {
            localidadeService.getById("'$codLocalidade'").tryGetValue<Localidade>().Name
        } catch (e: Exception) {
            null
        }
        return if(nome.isNullOrBlank()) "$codLocalidade" else "$codLocalidade - $nome"
    }
}

package br.andrew.sap.services.autorizacao

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.services.cadastro.BusinessPartnersService
import org.springframework.stereotype.Component

//motor de regras pluggavel: cada regra nova e so uma nova classe @Component
//implementando essa interface, sem precisar mexer em mais nada
interface RegraAutorizacao {
    val motivo : String
    fun avalia(documento : Document) : Boolean
}

//pagamento a prazo pra cliente com titulo vencido ha mais de 3 dias e nao
//reconciliado (ver BusinessPartnersService.temTituloVencido / cliente-em-atraso.sql)
@Component
class ClienteEmAtrasoRegra(val businessPartnersService : BusinessPartnersService) : RegraAutorizacao {
    override val motivo = "CLIENTE_EM_ATRASO"

    override fun avalia(documento : Document) : Boolean {
        return !documento.isAvista() && businessPartnersService.temTituloVencido(documento.CardCode)
    }
}

@org.springframework.stereotype.Service
class RegraAutorizacaoService(val regras : List<RegraAutorizacao>,
                              val regraFilialService : RegraFilialService) {

    //devolve o motivo da primeira regra que bater, ou null se nenhuma regra
    //exigir autorizacao pra esse documento
    //
    //O filtro por filial vem ANTES do avalia(): alem de nenhuma regra precisar saber de
    //filial, regra desligada naquela filial nem chega a consultar o banco (ClienteEmAtraso
    //e ClienteEstouraLimiteCredito fazem uma consulta cada uma por documento).
    //
    //O cadastro de filial e lido UMA vez por avaliacao e todas as regras sao filtradas contra
    //esse retrato - ler por regra repetia a mesma leitura paginada do UDO a cada regra.
    fun avaliar(documento : Document) : String? {
        val filial = documento.getBPL_IDAssignedToInvoice()
        val cadastro = regraFilialService.cadastro()
        return regras
            .filter { cadastro.ativaPara(it.motivo, filial) }
            .firstOrNull { it.avalia(documento) }
            ?.motivo
    }

    /**
     * Motivos que o motor de regras realmente produz - fonte unica pro cadastro de autorizador.
     *
     * O motivo e string solta em tres lugares (a regra, o U_motivo da @AUTORIZACAO e o cadastro
     * de autorizador), e os tres precisam bater exatamente: podeAutorizar compara com ==. Com o
     * cadastro em campo livre, um erro de digitacao criava autorizador para um motivo que
     * nenhuma regra gera - o documento ficava pendente sem ninguem que pudesse aprovar, sem erro
     * nenhum. Publicando a lista daqui, a tela so oferece motivo que existe de verdade.
     */
    fun motivos() : List<String> {
        return regras.map { it.motivo }.distinct().sorted()
    }
}

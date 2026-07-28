package br.andrew.sap.infrastructure.create.udo

import br.andrew.sap.services.cobranca.CobrancaDominioService
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("!test")
@ConditionalOnProperty(value = ["fields.cobranca"], havingValue = "true", matchIfMissing = false)
class CobrancaDominioSeeder(
    // dependência não usada diretamente: força o Spring a criar as UDTs/UDFs
    // do CobrancaConfiguration antes de tentar semear dados nelas
    cobrancaConfiguration: CobrancaConfiguration,
    val service: CobrancaDominioService
) {

    init {
        val status = listOf(
            "1" to "NÃO INICIADO",
            "2" to "PRORROGADO",
            "3" to "SEM CONTATO",
            "4" to "PROBLEMA INTERNO (logística)",
            "5" to "SEM SUCESSO COM O CLIENTE – Email Unidade",
            "6" to "SLA UNIDADE (5 dias)",
            "7" to "COMITE",
            "8" to "EM NEGOCIAÇÃO",
            "9" to "ACORDO ENVIADO",
            "10" to "PAGO",
            "11" to "PAGAMENTO ENTRE EMPRESAS",
            "12" to "A VISTA",
            "13" to "JURIDICO",
        )

        val acao = listOf(
            "1" to "WATTS",
            "2" to "PROCESSO INTERNO",
            "3" to "EMAIL",
            "4" to "LIGAÇÃO",
            "5" to "PROTESTADO",
            "6" to "SERASA",
            "7" to "UNIDADE",
        )

        val situacao = listOf(
            "1" to "PAGO",
            "2" to "A RECEBER",
            "3" to "DEVOLUÇÃO",
            "4" to "RENEGOCIADO",
            "5" to "JURIDICO",
        )

        val ocorrencia = listOf(
            "1" to "VENDA A VISTA",
            "2" to "COBRADO CLIENTE VIA WHATTSAPP, AGUARDANDO RETORNO",
            "3" to "ENCONTRO DE CONTAS",
            "4" to "COBRANÇA ENVIADA PARA O REPRESENTANTE",
            "5" to "ACORDO DA DIRETORIA, JUNTO AO CLIENTE",
            "6" to "CLIENTE NÃO RECONHECE A DIVIDA",
            "7" to "SOLICITO CONTATO ATUALIZADO DO CLIENTE AO REPRESENTANTE",
            "8" to "CLIENTE DISSE QUE ESTA ESPERANDO UM DINHEIRO PARA EFETUAR O PAGAMENTO",
            "9" to "PAGAMENTO VIA DEPÓSITO EM CONTA",
            "10" to "PAGAMENTO EM BEZERROS",
            "11" to "COBRADO NOVAMENTE O CLIENTE, ATE O MOMENTO SEM RETORNO",
            "12" to "FEITA RENEGOCIAÇÃO DA DIVIDA",
            "13" to "PASSADO O VALOR ATUALIZADO, AGUARDANDO OK DO CLIENTE PARA RENEGOCIAR",
            "14" to "PAGAMENTO AGENDANDO NO BANCO",
            "15" to "LANÇAMENTO CONTABIL",
            "16" to "PAGAMENTO ATE O FINAL DA SEMANA",
            "17" to "ENVIADO BOLETO ATUALIZADO",
            "18" to "ABATIMENTO EM COMISSÃO",
            "19" to "NEGOCIADO PAGAMENTO NO CARTÃO",
            "20" to "CLIENTE FICOU DE PAGAR NA UNIDADE",
            "21" to "FEITO RENEGOCIAÇÃO(PRORROGAÇÃO DE VENCIMENTO)",
            "22" to "PREVISÃO DE PAGAMENTO, PARA PROXIMA SEMANA.",
            "23" to "TROCA DE PRODUTOS",
            "24" to "DEBITO DO CLIENTE ENVIADO AO DEPARTAMENTO JURIDICO",
            "25" to "DEBITO PROTESTADO, JUNTANDO DOCUMENTOS PARA ENVIO AO JURIDICO",
            "26" to "ENVIADO VALOR PARA PAGAMENTO VIA PIX",
            "27" to "TROCA DE PRODUTOS",
            "28" to "DEBITO DO SECADOR",
            "29" to "A VISTA PARA 30 DIAS",
        )

        seed("STATUS", status)
        seed("ACAO", acao)
        seed("SITUACAO", situacao)
        seed("OCORRENCIA", ocorrencia)
    }

    private fun seed(tipo: String, valores: List<Pair<String, String>>) {
        valores.forEachIndexed { index, (codigo, descricao) ->
            service.findOrCreate(tipo, codigo, descricao, index + 1)
        }
    }
}

package br.andrew.sap.model.documents

import br.andrew.sap.model.enums.CancelStatus
import br.andrew.sap.model.sap.documents.Invoice
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * CancelStatus fica no Document, ou seja, na base de TODO documento: um valor que o enum
 * nao conhecesse quebrava a leitura da nota inteira e derrubava o fluxo que estivesse
 * rodando. Foi o que aconteceu quando o SAP devolveu "csYes" e a
 * ConciliacaoVendaFuturaSchedule parou no meio ("Cannot deserialize value of type
 * CancelStatus from String csYes").
 */
class CancelStatusTest {

    private val mapper = ObjectMapper().registerKotlinModule()

    private fun lerInvoice(cancelStatus: String): Invoice {
        val json = """{"CardCode":"C1","DocDueDate":null,"DocumentLines":[],
            "BPL_IDAssignedToInvoice":"2","DocEntry":1,"CancelStatus":"$cancelStatus"}"""
        return mapper.readValue(json, Invoice::class.java)
    }

    @Test
    fun leOsStatusQueOSapDevolve(){
        Assertions.assertEquals(CancelStatus.csNo, lerInvoice("csNo").CancelStatus)
        Assertions.assertEquals(CancelStatus.csYes, lerInvoice("csYes").CancelStatus)
        Assertions.assertEquals(CancelStatus.csCancelled, lerInvoice("csCancelled").CancelStatus)
        Assertions.assertEquals(
            CancelStatus.csCancellationDocument,
            lerInvoice("csCancellationDocument").CancelStatus
        )
    }

    //valor novo/desconhecido do SAP: o documento tem que continuar sendo lido, so sem
    //saber dizer se esta cancelado
    @Test
    fun valorDesconhecidoNaoQuebraALeituraDoDocumento(){
        val invoice = lerInvoice("csAlgoQueOSapInventou")
        Assertions.assertNull(invoice.CancelStatus)
        Assertions.assertEquals(1, invoice.docEntry)
    }

    //tudo que nao e csNo esta fora do jogo - ver MapaRelacoesService.cancelado
    @Test
    fun soCsNoNaoEhCancelado(){
        Assertions.assertFalse(CancelStatus.csNo.cancelado())
        Assertions.assertTrue(CancelStatus.csYes.cancelado())
        Assertions.assertTrue(CancelStatus.csCancelled.cancelado())
        Assertions.assertTrue(CancelStatus.csCancellationDocument.cancelado())
    }

    //Access.WRITE_ONLY: o campo e somente leitura no Service Layer - manda-lo de volta
    //num POST/PATCH derruba a chamada com "Internal error (-5002)"
    @Test
    fun naoVoltaNoJsonEnviadoAoSap(){
        val invoice = lerInvoice("csYes")
        Assertions.assertFalse(mapper.writeValueAsString(invoice).contains("CancelStatus"))
    }
}

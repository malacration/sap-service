package br.andrew.sap.services

import br.andrew.sap.mock.Mocks
import br.andrew.sap.model.logistica.PedidoUpdate
import br.andrew.sap.model.logistica.PedidoUpdateLine
import br.andrew.sap.services.abstracts.EntitiesService
import br.andrew.sap.services.batch.BatchId
import br.andrew.sap.services.batch.BatchList
import br.andrew.sap.services.batch.BatchService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate
import org.mockito.kotlin.eq
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import java.util.UUID


class BatchServiceTest {

    @Test
    fun pedidoUpdateExibeDocNumEDocEntryQuandoForemDiferentes() {
        val pedido = PedidoUpdate(
            "67521",
            listOf(PedidoUpdateLine(1, 0, 10)),
            "70830"
        )

        Assertions.assertEquals("70830 (DocEntry 67521)", pedido.getDisplayId())
    }

    @Test
    fun bachError(){
        val saida = "--batchresponse_S8CsLgI6-Vy8a-5Zez-rbLi-H0M1AXEiT4yu\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: 12\n" +
                "\n" +
                "HTTP/1.1 404 Not Found\n" +
                "Content-Type: application/json;charset=utf-8\n" +
                "Content-Length: 210\n" +
                "OData-Version: 4.0\n" +
                "\n" +
                "{\n" +
                "   \"error\" : {\n" +
                "      \"code\" : \"-2028\",\n" +
                "      \"details\" : [\n" +
                "         {\n" +
                "            \"code\" : \"\",\n" +
                "            \"message\" : \"\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"message\" : \"No matching records found (ODBC -2028)\"\n" +
                "   }\n" +
                "}\n" +
                "--batchresponse_S8CsLgI6-Vy8a-5Zez-rbLi-H0M1AXEiT4yu--\n"


        val rest = Mockito.mock(RestTemplate::class.java)

        whenever(
            rest.exchange(
                any<RequestEntity<*>>(),
                eq(String::class.java)
            )
        ).thenReturn(ResponseEntity.ok(saida))

        val erro = assertThrows<Exception> {
            BatchService(rest, Mocks.getSapEnvrioment(),Mocks.getBusinessPartnersService(),Mocks.getAuthService())
                .run(BatchList())
        }
    }


    @Test
    fun bachSucesso(){
        val saida = "--batchresponse_d878cedc-a0ad-4025-823e-5ee1aaffa288\n" +
                "Content-Type:application/http\n" +
                "Content-Transfer-Encoding:binary\n" +
                "\n" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type:application/json;odata=minimalmetadata;charset=utf-8\n" +
                "Content-Length:14729\n" +
                "\n" +
                "<Json format Item(i001) Body>\n" +
                "--batchresponse_d878cedc-a0ad-4025-823e-5ee1aaffa288\n" +
                "Content-Type:multipart/mixed;boundary=changesetresponse_8bfb3c36-dbf7-46a0-bdfe670bbac86eb2\n" +
                "--changesetresponse_8bfb3c36-dbf7-46a0-bdfe-670bbac86eb2\n" +
                "\n" +
                "Content-Type:application/http\n" +
                "Content-Transfer-Encoding:binary\n" +
                "Content-ID:1\n" +
                "\n" +
                "HTTP/1.1 201 Created\n" +
                "Content-Type:application/json;odata=minimalmetadata;charset=utf-8\n" +
                "Content-Length:14641\n" +
                "Location:https://databaseserver:50000/b1s/v1/Items('i002')\n" +
                "\n" +
                "<Json format Item(i002) Body>\n" +
                "\n" +
                "--changesetresponse_8bfb3c36-dbf7-46a0-bdfe-670bbac86eb2\n" +
                "Content-Type:application/http\n" +
                "Content-Transfer-Encoding:binary\n" +
                "Content-ID:2\n" +
                "\n" +
                "HTTP/1.1 204 No Content\n" +
                "\n" +
                "\n" +
                "--changesetresponse_8bfb3c36-dbf7-46a0-bdfe-670bbac86eb2--\n" +
                "--batchresponse_d878cedc-a0ad-4025-823e-5ee1aaffa288--\n"

        val rest = Mockito.mock(RestTemplate::class.java)

        whenever(
            rest.exchange(
                any<RequestEntity<*>>(),
                eq(String::class.java)
            )
        ).thenReturn(ResponseEntity.ok(saida))

        val retorno = BatchService(rest, Mocks.getSapEnvrioment(),Mocks.getBusinessPartnersService(),Mocks.getAuthService())
            .run(BatchList())

        Assertions.assertEquals(3,retorno.size)
    }

    @Test
    fun bachErrorNaoInventaDocNumQuandoContentIdNaoMapeiaParaPedidoReal() {
        val batchId = UUID.randomUUID()
        val saida = "--batchresponse_$batchId\n" +
                "Content-Type: multipart/mixed; boundary=changesetresponse_test\n" +
                "\n" +
                "--changesetresponse_test\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: 1\n" +
                "\n" +
                "HTTP/1.1 204 No Content\n" +
                "\n" +
                "--changesetresponse_test\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: 99\n" +
                "\n" +
                "HTTP/1.1 400 Bad Request\n" +
                "Content-Type: application/json;charset=utf-8\n" +
                "\n" +
                "{\n" +
                "   \"error\" : {\n" +
                "      \"code\" : \"-1\",\n" +
                "      \"message\" : \"erro real do pedido\"\n" +
                "   }\n" +
                "}\n" +
                "--changesetresponse_test--\n" +
                "--batchresponse_$batchId--\n"

        val rest = Mockito.mock(RestTemplate::class.java)

        whenever(
            rest.exchange(
                any<RequestEntity<*>>(),
                eq(String::class.java)
            )
        ).thenReturn(ResponseEntity.ok(saida))

        val batchList = BatchList()
            .add(br.andrew.sap.services.batch.BatchMethod.PATCH, TestBatchId("67521"), FakeEntityService())
            .add(br.andrew.sap.services.batch.BatchMethod.PATCH, TestBatchId("70830"), FakeEntityService())

        val erro = assertThrows<Exception> {
            BatchService(rest, Mocks.getSapEnvrioment(), Mocks.getBusinessPartnersService(), Mocks.getAuthService())
                .run(batchList)
        }

        Assertions.assertTrue(erro.message!!.contains("erro real do pedido"))
    }

    @Test
    fun bachErrorNaoAtribuiPedidoEspecificoQuandoBatchTemMaisDeUmPedido() {
        val saida = "--batchresponse_S8CsLgI6-Vy8a-5Zez-rbLi-H0M1AXEiT4yu\n" +
                "Content-Type: application/http\n" +
                "Content-Transfer-Encoding: binary\n" +
                "Content-ID: 2\n" +
                "\n" +
                "HTTP/1.1 404 Not Found\n" +
                "Content-Type: application/json;charset=utf-8\n" +
                "Content-Length: 210\n" +
                "OData-Version: 4.0\n" +
                "\n" +
                "{\n" +
                "   \"error\" : {\n" +
                "      \"code\" : \"-2028\",\n" +
                "      \"details\" : [\n" +
                "         {\n" +
                "            \"code\" : \"\",\n" +
                "            \"message\" : \"\"\n" +
                "         }\n" +
                "      ],\n" +
                "      \"message\" : \"No matching records found (ODBC -2028)\"\n" +
                "   }\n" +
                "}\n" +
                "--batchresponse_S8CsLgI6-Vy8a-5Zez-rbLi-H0M1AXEiT4yu--\n"

        val rest = Mockito.mock(RestTemplate::class.java)

        whenever(
            rest.exchange(
                any<RequestEntity<*>>(),
                eq(String::class.java)
            )
        ).thenReturn(ResponseEntity.ok(saida))

        val batchList = BatchList()
            .add(br.andrew.sap.services.batch.BatchMethod.PATCH, TestBatchId("67521"), FakeEntityService())
            .add(br.andrew.sap.services.batch.BatchMethod.PATCH, TestBatchId("70830"), FakeEntityService())

        val erro = assertThrows<Exception> {
            BatchService(rest, Mocks.getSapEnvrioment(), Mocks.getBusinessPartnersService(), Mocks.getAuthService())
                .run(batchList)
        }

        Assertions.assertTrue(erro.message!!.contains("No matching records found (ODBC -2028)"))
        Assertions.assertFalse(erro.message!!.contains("Pedido 70830"))
    }

    private class TestBatchId(private val displayId: String) : BatchId {
        override fun getId(): String = displayId
        override fun getDisplayId(): String = displayId
    }

    private class FakeEntityService : EntitiesService<Any>(
        Mocks.getSapEnvrioment(),
        Mockito.mock(RestTemplate::class.java),
        Mocks.getAuthService()
    ) {
        override fun path(): String = "/b1s/v1/Orders"
    }
}

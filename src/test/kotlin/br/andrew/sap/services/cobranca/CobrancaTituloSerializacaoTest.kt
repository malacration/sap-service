package br.andrew.sap.services.cobranca

import br.andrew.sap.model.cobranca.CobrancaTitulo
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class CobrancaTituloSerializacaoTest {

    @Test
    fun `BPLId e BPLName sobrevivem a serializacao pro front com o nome exato esperado`() {
        // Sem @JsonProperty explicito, o UpperCamelCaseStrategy transforma esses dois
        // campos (3+ maiusculas seguidas no inicio) em "Bplid"/"Bplname" na saida, e o
        // front (que espera "BPLId"/"BPLName" literal) nunca acha o valor - foi assim
        // que a coluna Filial ficou em branco em producao. Trava esse comportamento aqui.
        val titulo = CobrancaTitulo(
            Tipo = "NF", DocEntry = 1, DocNum = 534, Serial = "1", Series = 1,
            BPLId = 6, BPLName = "FAZENDA SERRA VERDE",
            CardCode = "CLI0007196", CardName = "GILBERTO", DocDate = "20260401",
            DocTotal = BigDecimal("100.00"), SlpCode = 60, SlpName = "Agro Pasto",
            InstlmntID = 1, InsTotal = BigDecimal("100.00"), PaidToDate = BigDecimal.ZERO,
            DueDate = "20260701", Saldo = BigDecimal("100.00"), DiasAtraso = 30, SituacaoSap = "ABERTO",
            U_Status = null, U_Cobrador = null, U_Acao = null, U_Situacao = null,
            U_Ocorrencia = null, U_Observacao = null, U_DataAcao = null, U_DataPromessa = null,
        )

        val mapper = JsonMapper.builder().addModule(kotlinModule()).build()
        val json = mapper.writeValueAsString(titulo)

        assertTrue(json.contains("\"BPLId\":6"), "BPLId nao saiu com o nome exato esperado! JSON: $json")
        assertTrue(json.contains("\"BPLName\":\"FAZENDA SERRA VERDE\""), "BPLName nao saiu com o nome exato esperado! JSON: $json")
        assertFalse(json.contains("\"Bplid\""), "voltou a sair com o nome mangled pelo UpperCamelCaseStrategy")
        assertFalse(json.contains("\"Bplname\""), "voltou a sair com o nome mangled pelo UpperCamelCaseStrategy")
    }
}

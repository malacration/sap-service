package br.andrew.sap.services.cobranca

import br.andrew.sap.infrastructure.create.udo.CobrancaConfiguration
import br.andrew.sap.model.entity.DbType
import br.andrew.sap.model.entity.FieldMd
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CobrancaUdfTamanhoTest {

    private val mapper = JsonMapper.builder().addModule(kotlinModule()).build()

    @Test
    fun `db_Numeric sem size nao manda Size pro SAP - foi essa omissao que criou o SMALLINT`() {
        // DbType.db_Numeric tem size = null e o FieldMd e @JsonInclude(NON_EMPTY): o campo
        // simplesmente desaparece do JSON, o SAP aplica o default dele (SizeID 6 -> SMALLINT,
        // teto 32767) e nunca avisa. Travando o comportamento pra ninguem se surpreender de novo.
        val semTamanho = FieldMd("DocEntry", "Nº Doc.", "@COB_TITULO", DbType.db_Numeric)

        val json = mapper.writeValueAsString(semTamanho)

        assertFalse(json.contains("\"Size\""), "esperado que o Size ficasse de fora. JSON: $json")
    }

    @Test
    fun `com o tamanho explicito o Size chega no SAP e o campo nasce INTEGER`() {
        val comTamanho = FieldMd("DocEntry", "Nº Doc.", "@COB_TITULO", DbType.db_Numeric).also {
            it.size = CobrancaConfiguration.TAMANHO_DOC_ENTRY
            it.editSize = CobrancaConfiguration.TAMANHO_DOC_ENTRY
        }

        val json = mapper.writeValueAsString(comTamanho)

        assertTrue(json.contains("\"Size\":11"), "Size nao chegou no JSON. JSON: $json")
        assertTrue(json.contains("\"EditSize\":11"), "EditSize nao chegou no JSON. JSON: $json")
    }

    @Test
    fun `o tamanho escolhido comporta o DocEntry real da base, nao so o teto do SMALLINT`() {
        // OINV nesta base ja esta em ~152 mil; SMALLINT para em 32767. O numero aqui e o que
        // separa "grava" de "grava nulo em silencio".
        val maiorValorSuportado = "9".repeat(CobrancaConfiguration.TAMANHO_DOC_ENTRY).toLong()

        assertTrue(maiorValorSuportado > Short.MAX_VALUE, "tamanho nao passa nem do SMALLINT")
        assertTrue(maiorValorSuportado > 200_000, "tamanho nao comporta o DocEntry atual de OINV")
    }
}

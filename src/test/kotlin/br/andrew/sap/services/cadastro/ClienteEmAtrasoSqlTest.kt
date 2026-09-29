package br.andrew.sap.services.cadastro

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class ClienteEmAtrasoSqlTest {

    private val sql = Files.readString(Path.of("src/main/resources/views/cliente-em-atraso.sql"))

    @Test
    fun `titulo em atraso e o saldo residual de debito da propria linha`() {
        // Em HMG (set/2026) o anti-join antigo (ITR1/OITR ligado so por TransId) deixava de fora
        // 289 clientes: pagar a parcela 1 de uma nota escondia as demais parcelas vencidas da
        // mesma transacao, e pagamento parcial contava como quitado. E marcava 8 clientes so
        // por pagamento a conta nao reconciliado (credito).
        assertTrue(sql.contains("\"BalDueDeb\" > 0"), "cliente-em-atraso precisa filtrar saldo residual de debito")
        assertFalse(sql.contains("ITR1") || sql.contains("OITR"), "nao reintroduzir o anti-join de reconciliacao")
    }

    @Test
    fun `view nao tem comentario`() {
        // Query achata o SQL numa linha so: um -- comentaria o resto da view (ver CLAUDE.md)
        assertFalse(sql.contains("--") || sql.contains("/*"))
    }
}

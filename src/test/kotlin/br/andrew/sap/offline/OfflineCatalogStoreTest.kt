package br.andrew.sap.offline

import br.andrew.sap.model.authentication.User
import br.andrew.sap.model.authentication.UserOriginEnum
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StringRedisTemplate

class OfflineCatalogStoreTest {

    private val store = OfflineCatalogStore(
        mock<StringRedisTemplate>(),
        mock<RedisTemplate<String, ByteArray>>(),
        jacksonObjectMapper(),
        ttlHours = 72,
        maxConcurrency = 1,
        leaseMinutes = 60
    )

    @Test
    fun `chave de autorizacao independe da ordem das permissoes`() {
        assertEquals(
            store.userKey(user(listOf("vendedor", "pix"), listOf(2, 1))),
            store.userKey(user(listOf("pix", "vendedor"), listOf(1, 2)))
        )
    }

    @Test
    fun `mudanca de permissao invalida o snapshot anterior`() {
        assertNotEquals(
            store.userKey(user(listOf("vendedor"), listOf(2))),
            store.userKey(user(listOf("vendedor_admin"), listOf(2)))
        )
    }

    private fun user(roles: List<String>, branches: List<Int>) = User(
        id = "55",
        _name = "Vendedor",
        origin = UserOriginEnum.SalePerson,
        userName = "vendedor55",
        bussinesPlace = branches,
        roles = roles
    )
}

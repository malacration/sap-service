package br.andrew.sap.model

import org.junit.jupiter.api.Test
import org.springframework.web.util.UriComponentsBuilder


class UrlTest {

    @Test
    fun test(){
        val initialUri = "https://windson/jose.php"
        val builder = UriComponentsBuilder.fromHttpUrl(initialUri)
        val modifiedUri = builder.host("myserver").toUriString()
        println(modifiedUri)
        assert(modifiedUri == "https://myserver/jose.php")

    }
}
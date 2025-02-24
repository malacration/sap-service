package br.andrew.sap

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

class PlanodeContasTests {

	@Test
	fun contextLoads() {
	}

	@Test
	fun leftTrim(){
		val entrada = "0006660"
		Assertions.assertEquals("6660",entrada.trim().trimStart('0'))
	}

	@Test
	fun arrayNull(){
		val lista = listOf("windson","jose")
		Assertions.assertEquals(null,lista.getOrNull(3))
	}

}

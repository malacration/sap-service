package br.andrew.sap

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

class RovemaApplicationTests {

	@Test
	fun contextLoads() {
	}

	@Test
	fun leftTrim(){
		val entrada = "0006660"
		Assertions.assertEquals("6660",entrada.trim().trimStart('0'))

	}

}

package br.andrew.sap.model


import br.andrew.sap.model.partner.CpfCnpj
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CpfCnpjTest {

    @Test
    fun testValidCPFWithMask() {
        val cpfvalidator = CpfCnpj("123.456.789-09")
        assertEquals("12345678909",cpfvalidator.value)
        assertTrue(cpfvalidator.isCpf())
    }

    @Test
    fun testValidCPFWithoutMask() {
        assertTrue(CpfCnpj("12345678909").isCpf())
        assertEquals("123.456.789-09",CpfCnpj("12345678909").getWithMask())
    }

    @Test
    fun testValidCNPJWithMask() {
        assertTrue(CpfCnpj("12.345.678/0001-99").isCnpj())
        assertEquals("12.345.678/0001-99",CpfCnpj("12.345.678/0001-99").getWithMask())
    }

    @Test
    fun testValidCNPJWithOutMask() {
        assertTrue(CpfCnpj("12345678000199").isCnpj())
        assertEquals("12.345.678/0001-99",CpfCnpj("12345678000199").getWithMask())

    }
}
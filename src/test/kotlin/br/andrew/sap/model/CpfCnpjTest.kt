package br.andrew.sap.model


import br.andrew.sap.model.sap.partner.CpfCnpj
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
        assertEquals("123.456.789-09", CpfCnpj("12345678909").getWithMask())
    }

    @Test
    fun testValidCNPJWithMask() {
        assertTrue(CpfCnpj("12.345.678/0001-99").isCnpj())
        assertEquals("12.345.678/0001-99", CpfCnpj("12.345.678/0001-99").getWithMask())
    }

    @Test
    fun testValidCNPJWithOutMask() {
        assertTrue(CpfCnpj("12345678000199").isCnpj())
        assertEquals("12.345.678/0001-99", CpfCnpj("12345678000199").getWithMask())
    }

    @Test
    fun tryCpf() {
        assertEquals("1", CpfCnpj("1").getWithMask())
        assertEquals("22", CpfCnpj("22").getWithMask())
        assertEquals("333", CpfCnpj("333").getWithMask())
        assertEquals("444.4", CpfCnpj("4444").getWithMask())
        assertEquals("555.55", CpfCnpj("55555").getWithMask())
        assertEquals("666.666", CpfCnpj("666666").getWithMask())
        assertEquals("777.777.7", CpfCnpj("7777777").getWithMask())
        assertEquals("888.888.88", CpfCnpj("88888888").getWithMask())
        assertEquals("999.999.999", CpfCnpj("999999999").getWithMask())
        assertEquals("000.000.000-0", CpfCnpj("0000000000").getWithMask())
        assertEquals("123.456.789-0", CpfCnpj("123.456.789-0").getWithMask())
    }

    @Test
    fun tryCnpj() {
        assertEquals("12.000.000/0000", CpfCnpj("120000000000").getWithMask())
        assertEquals("13.000.000/0000-0", CpfCnpj("1300000000000").getWithMask())
    }
}
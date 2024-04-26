package br.andrew.sap.model.romaneio

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class RomaneioFazendaInsumoTests {


    @Test
    fun dataEntradaExiste(){
        val romaneio = RomaneioFazendaInsumo(null,null,null,null,null,null,null)
        romaneio.preparaEntrada()
        Assertions.assertNotNull(romaneio.getU_DataEntrada())
        Assertions.assertNull(romaneio.getU_DataSaida())
    }

    @Test
    fun dataSaidaExiste(){
        val romaneio = RomaneioFazendaInsumo(null,null,null,null,null,null,null)
        romaneio.preparaSaida()
        Assertions.assertNull(romaneio.getU_DataEntrada())
        Assertions.assertNotNull(romaneio.getU_DataSaida())
    }
}
package br.andrew.sap.services

import br.andrew.sap.infrastructure.WarehouseDefaultConfig
import br.andrew.sap.model.WarehouseDefault
import org.springframework.stereotype.Service

@Service
class SequenceCodeService(private val warehouseDefaultConfig: WarehouseDefaultConfig) {

    // Mapeamento estático de SequenceCode por BPLID (pode ser substituído por uma consulta ao SAP ou outra fonte)
    private val sequenceCodeByBPLID = mapOf(
        "2" to 27,
        "3" to 28,
        // TODO: Substituir por consulta dinâmica ao SAP ou banco de dados, se aplicável
    )

    fun getSequenceCodeForBPLID(bplId: String): Int {
        val warehouseDefault = WarehouseDefaultConfig.warehouses.find { it.BPLID == bplId }
            ?: throw Exception("Nenhum depósito padrão encontrado para a filial: $bplId")

        return sequenceCodeByBPLID[bplId]
            ?: throw Exception("Nenhum SequenceCode configurado para a filial: $bplId")
    }
}
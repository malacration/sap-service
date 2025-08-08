package br.andrew.sap.services.document

import br.andrew.sap.model.sap.documents.Invoice
import br.andrew.sap.model.sap.documents.base.BatchNumbers

class InvoiceOrdemCarregamento {
    fun prepareToSave(invoice: Invoice): Invoice {
        invoice.DocumentLines.forEach { line ->
            if (line.ItemCode != null && line.BatchNumbers.isNullOrEmpty()) {
                // Adiciona um batch default se não houver
                line.BatchNumbers = listOf(
                    BatchNumbers(
                        BatchNumber = "RLE005100",
                        ItemCode = line.ItemCode
                    )
                )
            }
            // Garante que cada batch tenha a quantidade correta
            line.BatchNumbers?.forEach { batch ->
                if (batch.Quantity == null || batch.Quantity == 0) {
                    batch.Quantity = line.Quantity.toDoubleOrNull()?.toInt() ?: 1
                }
            }
        }
        return invoice
    }
}
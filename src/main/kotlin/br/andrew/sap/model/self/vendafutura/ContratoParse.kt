package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.infrastructure.odata.OData
import br.andrew.sap.model.entity.ChildTables
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines
import br.andrew.sap.model.sap.documents.base.Product
import kotlin.math.exp


class ContratoParse {

    companion object{

        fun parse(doc : Document) : Contrato{
            return Contrato(
                doc.docEntry ?: throw Exception("A propriedade docEntry nao pode ser null"),
                doc.CardCode,
                parseDocumentLines(doc.DocumentLines),
                doc.salesPersonCode,
                doc.cardName ?: throw Exception("Nome do cliente nao pode ser nulo"),
                doc.getBPL_IDAssignedToInvoice().toIntOrNull() ?: throw Exception("Nao foi possivel obter o id da filial"),
                doc.totalDespesaAdicional().toDouble() ?: 0.0,
            )
        }

        fun parse(line : DocumentLines) : Item{
            return Item(
                line.ItemCode ?: throw Exception("A propriedade ItemCode nao pode ser null"),
                line.ItemDescription ?: throw Exception("A propriedade ItemDescription nao pode ser null"),
                line.U_preco_negociado ?: throw Exception("A propriedade U_preco_negociado nao pode ser null"),
                line.Quantity.toDoubleOrNull() ?: throw Exception("A propriedade Quantity nao pode ser null"),
                line.U_preco_base ?: throw Exception("A propriedade U_preco_base nao pode ser null"),
                line.DiscountPercent  ?: throw Exception("A propriedade DiscountPercent nao pode ser null"),
                line.CommisionPercent  ?: throw Exception("A propriedade CommisionPercent nao pode ser null"),
                line.MeasureUnit  ?: throw Exception("A propriedade MeasureUnit nao pode ser null")
            )
        }


        fun parseDocumentLines(itens : List<DocumentLines>): MutableList<Item> {
            return itens
                .mapIndexed{ index: Int, it: DocumentLines -> parse(it).also { it.LineId = index } }
                .toMutableList()
        }
    }
}
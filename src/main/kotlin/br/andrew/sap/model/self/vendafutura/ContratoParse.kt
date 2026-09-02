package br.andrew.sap.model.self.vendafutura

import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.DocumentLines


class ContratoParse {

    companion object{

        /**
         * @param localidade localidade de entrega negociada, resolvida pelo chamador a partir do
         *   endereco do pedido. Nulo quando nao da pra resolver (retirada, endereco sem
         *   localidade cadastrada): o contrato nasce sem destino e a troca exige atribuir depois.
         * @param regiaoCode regiao que valia na assinatura - historico, nunca entra em calculo.
         */
        fun parse(doc : Document, localidade : Int? = null, regiaoCode : String? = null) : Contrato{
            return Contrato(
                doc.docEntry ?: throw Exception("A propriedade docEntry nao pode ser null"),
                doc.CardCode,
                parseDocumentLines(doc.DocumentLines),
                doc.salesPersonCode,
                doc.cardName ?: throw Exception("Nome do cliente nao pode ser nulo"),
                doc.getBPL_IDAssignedToInvoice().toIntOrNull() ?: throw Exception("Nao foi possivel obter o id da filial"),
                //freteDespesaAdicional, nao totalDespesaAdicional: o segundo soma TODAS as
                //despesas do pedido. Qualquer despesa que nao fosse frete nascia dentro do
                //U_valorFrete e desalinhava o rateio das retiradas, dos dois lados (aqui e na
                //SBO_SP_VALIDACAO_VENDA_FUTURA, que le so ExpnsCode = 1).
                doc.freteDespesaAdicional().toDouble(),
            ).also {
                it.U_Localidade = localidade
                it.U_RegiaoCode = regiaoCode
            }
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
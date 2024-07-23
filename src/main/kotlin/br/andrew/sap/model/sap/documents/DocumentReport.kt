package br.andrew.sap.model.sap.documents

import br.andrew.sap.model.sap.BussinessPlace
import br.andrew.sap.model.bank.PaymentMethod
import br.andrew.sap.model.sap.documents.base.Document
import br.andrew.sap.model.sap.documents.base.Product
import br.andrew.sap.model.sap.partner.BusinessPartner

class DocumentReport(
    private val document: Document,
    private val businessPartner: BusinessPartner,
    private val place: BussinessPlace,
    private val paymentMethodSap: PaymentMethod
) {
        val nome : String
        val cpf : String
        val email : String
        val idpedido : String
        val titule : String
        val companyName : String
        val total : String = document.DocTotal ?: "0.00"
        val numInstallment = document.documentInstallments?.size ?: 1
        val paymentMethod = paymentMethodSap.Description
        val address = document.Address ?: ""

        val itens : List<Product> = this.document.DocumentLines as List<Product>

        init{
            nome = businessPartner.cardName ?: throw Exception("Nome não informado")
            cpf = businessPartner.getCpfCnpj().value
            email = businessPartner.emailAddress ?: throw Exception("Email não informado")
            idpedido = document.docNum.toString()
            titule = "Pedido de venda  N[${idpedido}], para $nome"
            companyName = place.BPLName ?: "Nome da empresa não informado"
        }

        fun getDate() : Map<String,Map<String,String>> {
            return mapOf<String,Map<String,String>>(
                "assinapessoadoc" to mapOf(
                    "nome" to nome,
                    "cpf" to cpf,
                    "email" to email,
                    "idPedido" to idpedido,
                    "telefone" to "69996106666",
                )
            )
        }
    }
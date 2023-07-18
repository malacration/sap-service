package br.andrew.sap.model.bank

enum class PaymentType(val valor : String){

    it_AllTransactions("it_AllTransactions"),
    it_OpeningBalance("it_OpeningBalance"),
    it_ClosingBalance("it_ClosingBalance"),
    it_Invoice("it_Invoice"),
    it_CredItnote("it_CredItnote"),
    it_TaxInvoice("it_TaxInvoice"),
    it_Return("it_Return"),
    it_PurchaseInvoice("it_PurchaseInvoice"),
    it_PurchaseCreditNote("it_PurchaseCreditNote"),
    it_PurchaseDeliveryNote("it_PurchaseDeliveryNote"),
}
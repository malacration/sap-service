package br.andrew.sap.model.sap.documents

enum class PixDocType(val documentType: DocumentTypes) {

    oInvoices(DocumentTypes.oInvoices),
    oDownPayments(DocumentTypes.oDownPayments);

    val value: Int
        get() = documentType.value

    val label: String
        get() = documentType.label

    companion object {
        fun from(documentType: DocumentTypes): PixDocType {
            return entries.firstOrNull { it.documentType == documentType }
                ?: throw IllegalArgumentException("Tipo de documento não permitido para PIX: ${documentType.name}")
        }
    }
}

fun PixDocType.matches(documentType: DocumentTypes): Boolean = this.documentType == documentType

fun DocumentTypes.toPixDocTypeOrNull(): PixDocType? =
    PixDocType.entries.firstOrNull { it.documentType == this }

fun DocumentTypes.isPixDocType(): Boolean = this.toPixDocTypeOrNull() != null

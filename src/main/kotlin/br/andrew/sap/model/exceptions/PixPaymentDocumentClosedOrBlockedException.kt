package br.andrew.sap.model.exceptions

class PixPaymentDocumentClosedOrBlockedException(
    cause: Throwable? = null
) : Exception(
    "O Documento esta fechado ou cancelado, tente novamente.",
    cause
)

package br.andrew.sap.model.exceptions

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

class PixPaymentAmountExceededException(
    invoiceAmount: Double,
    bankAmount: Double,
    cause: Throwable? = null
) : Exception(
    buildMessage(invoiceAmount, bankAmount),
    cause
) {
    companion object {
        private val locale = Locale.of("pt", "BR")

        private fun buildMessage(invoiceAmount: Double, bankAmount: Double): String {
            val invoiceFormatted = format(invoiceAmount)
            val bankFormatted = format(bankAmount)
            val retryMessage = if (sameDisplayedValue(invoiceAmount, bankAmount)) {
                " Tente novamente."
            } else {
                ""
            }
            return "O valor informado pelo banco é maior que o valor da invoice. " +
                "Valor da invoice: $invoiceFormatted. " +
                "Valor informado pelo banco: $bankFormatted." +
                retryMessage
        }

        private fun format(value: Double): String = "%.2f".format(locale, value)

        private fun sameDisplayedValue(invoiceAmount: Double, bankAmount: Double): Boolean {
            return BigDecimal.valueOf(invoiceAmount).setScale(2, RoundingMode.HALF_UP) ==
                BigDecimal.valueOf(bankAmount).setScale(2, RoundingMode.HALF_UP)
        }
    }
}

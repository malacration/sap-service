package br.andrew.sap.services.batch

class BatchRespondeHandler {

    fun parseBatchResponse(batchResponse: String): List<BatchResponse> {
        // Delimitadores para separar os blocos de batch e changesets
        val batchDelimiter = "--batchresponse_[\\w-]+".toRegex()
        val changesetDelimiter = "--changesetresponse_[\\w-]+".toRegex()

        // Separamos primeiro pelos batch delimiters
        val batchParts = batchResponse.split(batchDelimiter)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val results = mutableListOf<BatchResponse>()

        for (batchPart in batchParts) {
            // Alguns batchParts podem conter vários changesets
            val changesets = batchPart.split(changesetDelimiter)
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            for (changeset in changesets) {
                // Regex pra achar status code
                val statusCodeRegex = """HTTP\/1\.1\s+(\d{3})""".toRegex()
                // Regex para pegar código de erro e mensagem
                val errorCodeRegex = """"code"\s*:\s*"([^"]+)"""".toRegex()
                val errorMessageRegex = """"message"\s*:\s*"([^"]+)"""".toRegex()

                // Captura o status code
                val statusCode = statusCodeRegex.find(changeset)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull() ?: 0

                val isSuccess = statusCode in 200..299

                if (isSuccess) {
                    results.add(
                        BatchResponse(
                            statusCode = statusCode,
                            body = changeset,
                            success = true
                        )
                    )
                } else {
                    var errCode = errorCodeRegex.find(changeset)?.groupValues?.get(1)
                    var errMsg = errorMessageRegex.find(changeset)?.groupValues?.get(1)

                    results.add(
                        BatchResponse(
                            statusCode = statusCode,
                            body = changeset,
                            success = false,
                            errorCode = errCode,
                            errorMessage = errMsg
                        )
                    )
                }
            }
        }
        return results
    }
}
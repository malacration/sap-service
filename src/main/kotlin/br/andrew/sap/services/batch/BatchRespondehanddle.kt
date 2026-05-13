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
                val errorCodeRegex = """"code"\s*:\s*"([^"]+)"""".toRegex()
                val errorMessageRegex = """"message"\s*:\s*"([^"]+)"""".toRegex()
                val errorValueRegex = """"value"\s*:\s*"([^"]+)"""".toRegex()
                val contentIdRegex = """Content-ID:\s*(\d+)""".toRegex(RegexOption.IGNORE_CASE)

                val statusCode = statusCodeRegex.find(changeset)
                    ?.groupValues?.get(1)
                    ?.toIntOrNull() ?: 0

                if (statusCode == 0) continue

                val contentId = contentIdRegex.find(changeset)?.groupValues?.get(1)?.toIntOrNull()

                val isSuccess = statusCode in 200..299

                if (isSuccess) {
                    results.add(
                        BatchResponse(
                            statusCode = statusCode,
                            body = changeset,
                            success = true,
                            contentId = contentId
                        )
                    )
                } else {
                    val errCode = errorCodeRegex.find(changeset)?.groupValues?.get(1)
                    val errMsg = errorMessageRegex.find(changeset)?.groupValues?.get(1)
                        ?: errorValueRegex.find(changeset)?.groupValues?.get(1)

                    results.add(
                        BatchResponse(
                            statusCode = statusCode,
                            body = changeset,
                            success = false,
                            errorCode = errCode,
                            errorMessage = errMsg,
                            contentId = contentId
                        )
                    )
                }
            }
        }
        return results
    }
}
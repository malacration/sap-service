package br.andrew.sap.model

data class StartProcessRequest<T>(
    val jobId: String,
    val params: T
)

data class ItemProgressUpdate(
    val jobId: String,
    val status: String,      // "PROCESSING" | "DONE" | "ERROR" | "FINISHED"
    val message: String? = null,
    val result: Any? = null
)
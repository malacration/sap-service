import kotlinx.serialization.Serializable

@Serializable
data class LogisticaPayload(
    val U_placa: String?,
    val U_motorista: String?,
    val U_capacidadeCaminhao: Double?
)
package simple.payment.tracker

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val theme: String,
    val deviceName: String = "",
    val trip: String = "",
)

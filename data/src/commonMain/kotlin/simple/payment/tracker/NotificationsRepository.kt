package simple.payment.tracker

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val time: Long,
    val text: String,
    val device: String? = null,
)

// You paid 43,05 EUR to MGP Vinted - Kleiderkreisel
// You saved 1,36 EUR on a 34,10 EUR purchase at Georg Endres
// You sent 14,00 EUR to Iaroslav Karpenko
@Deprecated("Use sumCents", ReplaceWith("sumCents() / 100"))
fun Notification.sum(): Int {
  return sumCents() / 100
}

fun Notification.sumCents(): Int {
  return runCatching {
        val sumText =
            when {
              text.startsWith("You paid") ->
                  text.substringAfter("You paid ").substringBefore(" to ")
              text.startsWith("You saved") ->
                  text.substringAfter(" on a ").substringBefore(" purchase at ")
              text.startsWith("You sent") ->
                  text.substringAfter("You sent ").substringBefore(" to ")
              else -> return 0
            }
        val currency = sumText.substringAfter(" ").substringAfter(" ") ?: "EUR"
        val number =
            sumText
                .substringBefore(" ")
                .substringBefore(" ")
                .removePrefix("$")
                .replace(",", "")
                .replace(".", "")
                .toIntOrNull()
                ?: 0
        val sum =
            when (currency) {
              "€ EUR" -> number
              "EUR" -> number
              "USD" -> number
              "GBP" -> number
              "RUB" -> number / 80
              else -> error("Cannot parse: $text, sumText: $sumText")
            }
        sum
      }
      .getOrElse {
        throw IllegalArgumentException("Failed to parse sum of $this, caused by $it", it)
      }
}

fun Notification.merchant(): String {
  return text.substringAfter(" to ").substringAfter("purchase at ")
}

/** Notifications can only be added, never removed. */
interface NotificationsRepository {
  fun notifications(): Flow<List<Notification>>

  suspend fun addNotifications(newNotifications: List<Notification>)
}

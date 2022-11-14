package simple.payment.tracker

import java.time.Instant
import java.util.*
import kotlinx.serialization.Serializable

/**
 * A payment created by a user.
 *
 * Can be created from [Notification] or from scratch
 *
 * @param time creation time. Used as [id]
 */
@Serializable
data class PaymentRecord(
    val category: String,
    val time: Long,
    val comment: String,
    val merchant: String,
    val notificationId: Long? = null,
    val manualSum: Int? = null,
    val sum: Int,
    val trip: String? = null,
    val user: String? = null,
    val refunds: List<Refund>? = null,
) {
  val id = time

  override fun toString(): String {
    return "Payment(id=$id, notificationId=$notificationId, date=${Date.from(Instant.ofEpochMilli(time))}, sum=$sum, category='$category', comment='$comment', merchant='$merchant', trip=$trip)"
  }
}

@Serializable
data class Refund(
    val sum: Int,
    val comment: String,
)

package simple.payment.tracker

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties
import java.time.Instant
import java.util.Date

// TODO id und sum non null, better domain model

@Keep
@IgnoreExtraProperties
data class Payment(
    val category: String,
    val time: Long,
    val comment: String,
    val merchant: String,
    val sum: Int,
    val notificationId: Long?,
    val cancelled: Boolean,
    val trip: String?,
    val auto: Boolean = false,
) {
  val id = time

  override fun toString(): String {
    return "Payment(id=$id, date=${Date.from(Instant.ofEpochMilli(time))}, sum=$sum, category='$category', comment='$comment', merchant='$merchant', trip=$trip)"
  }

  companion object // functions below
}

/** Used to create payments from Firebase maps */
fun Payment.Companion.fromMap(map: Map<String, Any>): Payment {
  return Payment(
      category = map["category"] as String,
      notificationId = map["notificationId"] as Long?,
      time = map["time"] as Long? ?: map["notificationId"] as Long,
      comment = map["comment"] as String? ?: "",
      merchant = map["merchant"] as String? ?: "",
      sum = (map["sum"] as Long? ?: 0L).toInt(),
      cancelled = map["cancelled"] as Boolean? ?: false,
      trip = map["trip"] as String?)
}

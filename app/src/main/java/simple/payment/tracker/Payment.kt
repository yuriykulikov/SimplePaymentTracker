package simple.payment.tracker

import com.google.firebase.database.IgnoreExtraProperties
import java.time.Instant
import java.util.Date
import kotlinx.serialization.Serializable

// TODO id und sum non null, better domain model

@IgnoreExtraProperties
@Serializable
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
  val isRecurrent: Boolean = merchant == "Recurrent"

  override fun toString(): String {
    return "Payment(id=$id, date=${Date.from(Instant.ofEpochMilli(time))}, sum=$sum, category='$category', comment='$comment', merchant='$merchant', trip=$trip)"
  }
}

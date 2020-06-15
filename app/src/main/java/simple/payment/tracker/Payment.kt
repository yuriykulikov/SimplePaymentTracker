package simple.payment.tracker

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class Payment(
    val category: String,
    val notificationId: Long?,
    val time: Long?,
    val comment: String?,
    val merchant: String?,
    val sum: Int?,
    val id: Long = notificationId ?: time!!
) {
    companion object // functions below
}

fun Payment.Companion.fromNotification(
    category: String,
    notificationId: Long,
    sum: Int? = null,
    comment: String? = null
): Payment {
    return Payment(
        category = category,
        notificationId = notificationId,
        time = null,
        comment = comment,
        merchant = null,
        sum = sum
    )
}

fun Payment.Companion.manual(
    category: String,
    sum: Int,
    comment: String? = null,
    merchant: String?,
    time: Long?
): Payment {
    return Payment(
        category = category,
        notificationId = null,
        time = time,
        comment = comment,
        merchant = merchant,
        sum = sum
    )
}

fun Payment.Companion.fromMap(map: Map<String, Any>): Payment {
    return Payment(
        id = map["id"] as Long,
        category = map["category"] as String,
        notificationId = map["notificationId"] as Long?,
        time = map["time"] as Long?,
        comment = map["comment"] as String?,
        merchant = map["merchant"] as String?,
        sum = (map["sum"] as Long?)?.toInt()
    )
}

package simple.payment.tracker

import androidx.annotation.Keep
import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

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
    val trip: String? = null
) {
    val id = time

    companion object // functions below
}


fun Payment.Companion.fromMap(map: Map<String, Any>): Payment {
    return Payment(
        category = map["category"] as String,
        notificationId = map["notificationId"] as Long?,
        time = map["time"] as Long? ?: map["notificationId"] as Long,
        comment = map["comment"] as String? ?: "",
        merchant = map["merchant"] as String? ?: "",
        sum = (map["sum"] as Long? ?: 0L).toInt(),
        cancelled = map["cancelled"] as Boolean? ?: false,
        trip = map["trip"] as String?
    )
}

@Deprecated("Fix R8 and get rid of it")
internal class PaymentAdapter {
    private val dataFormat: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

    @ToJson
    fun toJson(writer: JsonWriter, payment: Payment) {
        writer.beginObject()
        payment.run {
            writer.name("category").value(category)
            notificationId?.let { writer.name("notificationId").value(it) }
            time?.let { writer.name("time").value(it) }
            time?.let {
                writer.name("date").value(dataFormat.format(Date.from(Instant.ofEpochMilli(it))))
            }
            comment?.let { writer.name("comment").value(it) }
            merchant?.let { writer.name("merchant").value(it) }
            sum?.let { writer.name("sum").value(it) }
            cancelled?.let { writer.name("cancelled").value(it) }
            trip?.let { writer.name("trip").value(it) }

        }
        writer.endObject()
    }

    @FromJson
    fun fromJson(reader: JsonReader): Payment {
        lateinit var category: String
        var notificationId: Long? = null
        var time: Long = 0
        var comment: String = ""
        var merchant: String = ""
        var sum: Int = 0
        var cancelled: Boolean = false
        var trip: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "category" -> category = reader.nextString()
                "notificationId" -> notificationId = reader.nextLong()
                "time" -> time = reader.nextLong()
                "comment" -> comment = reader.nextString()
                "merchant" -> merchant = reader.nextString()
                "sum" -> sum = reader.nextInt()
                // "id" -> id = reader.nextLong()
                "cancelled" -> cancelled = reader.nextBoolean()
                "trip" -> trip = reader.nextString()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return Payment(
            category = category,
            notificationId = notificationId,
            time = time,
            comment = comment,
            merchant = merchant,
            sum = sum,
            cancelled = cancelled,
            trip = trip
        )
    }
}
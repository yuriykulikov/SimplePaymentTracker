package simple.payment.tracker

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

@Deprecated("Fix R8 and get rid of it")
internal class PaymentAdapter {
    @ToJson
    fun toJson(writer: JsonWriter, payment: Payment) {
        writer.beginObject()
        payment.run {
            writer.name("category").value(category)
            notificationId?.let { writer.name("notificationId").value(it) }
            time?.let { writer.name("time").value(it) }
            comment?.let { writer.name("comment").value(it) }
            merchant?.let { writer.name("merchant").value(it) }
            sum?.let { writer.name("sum").value(it) }
            id?.let { writer.name("id").value(it) }
        }
        writer.endObject()
    }

    @FromJson
    fun fromJson(reader: JsonReader): Payment {
        lateinit var category: String
        var notificationId: Long? = null
        var time: Long? = null
        var comment: String? = null
        var merchant: String? = null
        var sum: Int? = null
        var id: Long? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "category" -> category = reader.nextString()
                "notificationId" -> notificationId = reader.nextLong()
                "time" -> time = reader.nextLong()
                "comment" -> comment = reader.nextString()
                "merchant" -> merchant = reader.nextString()
                "sum" -> sum = reader.nextInt()
                "id" -> id = reader.nextLong()
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return Payment(category, notificationId, time, comment, merchant, sum)
    }
}
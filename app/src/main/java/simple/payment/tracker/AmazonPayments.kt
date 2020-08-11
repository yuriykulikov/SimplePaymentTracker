package simple.payment.tracker

import io.reactivex.Observable

data class AmazonPayment(
  val orderId: String,
  val category: String,
  val time: Long,
  val comment: String,
  val sum: Int
) {
  companion object {
    fun fromMap(map: Map<String, Any>): AmazonPayment {
      return AmazonPayment(
        comment = map["comment"] as String? ?: "",
        category = map["category"] as String,
        sum = (map["sum"] as Long? ?: 0L).toInt(),
        orderId = map["orderId"] as String,
        time = map["time"] as Long
      )
    }
  }
}

class AmazonPaymentsRepository(
  private val firebaseDatabase: Firebase
) {
  private val amazonPayments: Observable<List<AmazonPayment>> = firebaseDatabase
    .child("amazonpayments") { map -> AmazonPayment.fromMap(map) }
    .observe()
    .map { it.values.toList() }
    .replay(1)
    .refCount()

  val payments: Observable<List<Payment>> = amazonPayments.map {
    it.map(AmazonPayment::toPayment)
  }
}

fun AmazonPayment.toPayment(): Payment {
  return Payment(
    category = category,
    trip = null,
    time = time,
    cancelled = false,
    sum = sum,
    merchant = "Amazon",
    notificationId = null,
    comment = comment
  )
}
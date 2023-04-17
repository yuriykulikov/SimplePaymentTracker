package simple.payment.tracker

import kotlinx.serialization.Serializable

sealed class Payment {
  abstract val merchant: String
  abstract val sum: Int
  abstract val comment: String
  abstract val time: Long
  abstract val category: String
  abstract val trip: String?
  open val refunds: List<Refund> = emptyList()
  val refunded
    get() = refunds.sumOf { it.sum }
  val initialSum
    get() = sum + refunded
  open val user: String? = null
}

data class ManualPayment(
    val payment: PaymentRecord,
) : Payment() {
  override val merchant: String
    get() = payment.merchant
  override val sum: Int
    get() = payment.sum - refunded
  override val comment: String
    get() = payment.comment
  override val time: Long
    get() = payment.time
  override val category: String
    get() = payment.category
  override val trip: String?
    get() = payment.trip
  override val refunds: List<Refund>
    get() = payment.refunds.orEmpty()
  override val user: String?
    get() = payment.user
}

data class PaypalPayment(
    val payment: PaymentRecord,
    val notification: Notification,
) : Payment() {
  override val merchant: String = payment.merchant
  override val sum: Int = notification.sum() - refunded
  override val comment: String = payment.comment
  override val time: Long = notification.time
  override val category: String = payment.category
  override val trip: String? = payment.trip
  override val refunds: List<Refund>
    get() = payment.refunds.orEmpty()
  override val user: String?
    get() = payment.user
}

data class InboxPayment(
    val notification: Notification,
) : Payment() {
  override val merchant: String = notification.merchant()
  override val sum: Int = notification.sum()
  override val comment: String = ""
  override val time: Long = notification.time
  override val category: String = ""
  override val trip: String? = null
}

data class AutomaticPayment(
    val notification: Notification,
    override val merchant: String,
    override val category: String,
    override val comment: String,
) : Payment() {
  override val sum: Int = notification.sum()
  override val time: Long = notification.time
  override val trip: String? = null
}

data class RecurringPayment(
    override val sum: Int,
    override val comment: String,
    override val time: Long,
    override val category: String,
) : Payment() {
  override val trip: String? = null
  override val merchant = "Recurrent"
}

@Serializable
data class AmazonPayment(
    val orderId: String,
    override val category: String,
    override val time: Long,
    override val comment: String,
    override val sum: Int,
    override val user: String?,
) : Payment() {
  override val merchant: String = "Amazon"
  override val trip: String? = null
}

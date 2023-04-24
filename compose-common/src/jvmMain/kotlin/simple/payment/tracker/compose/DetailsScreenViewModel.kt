package simple.payment.tracker.compose

import androidx.datastore.core.DataStore
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import simple.payment.tracker.*
import simple.payment.tracker.logging.Logger

interface DetailsScreenStateCallback {
  fun change(
      sum: String? = null,
      trip: String? = null,
      merchant: String? = null,
      comment: String? = null,
      category: String? = null,
  ) {}

  fun addRefund() {}
  fun changeRefund(index: Int, refund: Refund) {}
}

data class Refund(
    val comment: String,
    val sum: String,
)

data class Sum(
    val initialSum: String,
    val refunds: List<Refund> = emptyList(),
) {
  val refunded = refunds.sumOf { it.sum.toIntOrNull() ?: 0 }
  val actualSum = (initialSum.toIntOrNull() ?: 0) - refunded
}

class DetailsScreenViewModel(
    private val payment: Payment?,
    private val paymentsRepository: PaymentsRepository,
    private val swipedPaymentsRepository: SwipedPaymentsRepository,
    private val onSave: () -> Unit,
    private val settings: DataStore<Settings>,
    private val logger: Logger,
    private val viewModelScope: CoroutineScope,
) : DetailsScreenStateCallback {
  private val dateFormat = SimpleDateFormat("dd-MM-yy HH:mm", Locale.GERMANY)

  private val initialTime = payment?.time?.let { Instant.ofEpochMilli(it) } ?: Instant.now()

  val category = MutableStateFlow(payment?.category)

  val sum =
      MutableStateFlow(
          Sum(
              payment?.initialSum?.toString() ?: "",
              payment?.refunds.orEmpty().map {
                Refund(sum = it.sum.toString(), comment = it.comment)
              }))

  val merchant = MutableStateFlow(payment?.merchant ?: "")

  val time = MutableStateFlow(dateFormat.format(Date.from(initialTime)))

  val comment = MutableStateFlow(payment?.comment ?: "")

  val trip = MutableStateFlow("")

  val canChangeSum = payment == null

  fun canSave(): StateFlow<Boolean> {
    return combine(time, sum, merchant, category) { time, sum, merchant, category ->
          canSave(time, sum, merchant, category)
        }
        .stateIn(viewModelScope, started = SharingStarted.Eagerly, false)
  }

  @OptIn(ExperimentalContracts::class)
  private fun canSave(time: String, sum: Sum, merchant: String, category: String?): Boolean {
    contract { returns(true) implies (category != null) }
    return runCatching { dateFormat.parse(time) }.isSuccess &&
        sum.initialSum.toIntOrNull() != null &&
        sum.refunds.all { it.sum.toIntOrNull() != null } &&
        merchant.isNotBlank() &&
        !category.isNullOrEmpty()
  }

  init {
    when (payment) {
      is PaypalPayment -> check(trip.tryEmit(payment.trip ?: ""))
      is ManualPayment -> check(trip.tryEmit(payment.trip ?: ""))
      is InboxPayment -> {
        //     // for new transactions get the value from settings
        viewModelScope.launch { trip.emit(settings.data.first().trip) }
      }
      is AmazonPayment,
      is AutomaticPayment,
      is RecurringPayment,
      null -> Unit
    }
  }

  override fun change(
      sum: String?,
      trip: String?,
      merchant: String?,
      comment: String?,
      category: String?
  ) {
    sum?.let { check(this.sum.tryEmit(this.sum.value.copy(initialSum = sum))) }
    merchant?.let { check(this.merchant.tryEmit(it)) }
    comment?.let { check(this.comment.tryEmit(it)) }
    category?.let { check(this.category.tryEmit(it)) }
    trip?.let { check(this.trip.tryEmit(it)) }
  }

  override fun addRefund() {
    val updatedRefunds = this.sum.value.refunds.plus(Refund("", ""))
    val updatedSum = this.sum.value.copy(refunds = updatedRefunds)
    check(this.sum.tryEmit(updatedSum))
  }

  override fun changeRefund(index: Int, refund: Refund) {
    val updatedRefunds = this.sum.value.refunds.toMutableList().apply { set(index, refund) }
    val updatedSum = this.sum.value.copy(refunds = updatedRefunds)
    check(this.sum.tryEmit(updatedSum))
  }

  fun save() {
    val time = time.value
    val sum = sum.value
    val merchant = merchant.value
    val category = category.value
    val comment = comment.value
    val trip = trip.value

    if (!canSave(time, sum, merchant, category)) return

    val paymentRecord =
        when (payment) {
          is PaypalPayment -> payment.payment
          is ManualPayment -> payment.payment
          else -> null
        }

    val notification =
        when (payment) {
          is InboxPayment -> payment.notification
          is AutomaticPayment -> payment.notification
          else -> null
        }

    val toSave =
        when {
          paymentRecord != null ->
              paymentRecord.copy(
                  category = category,
                  comment = comment,
                  merchant = merchant,
                  refunds =
                      sum.refunds.map {
                        Refund(
                            sum = it.sum.toIntOrNull() ?: 0,
                            comment = it.comment,
                        )
                      },
                  trip = trip.takeIf { it.isNotEmpty() },
              )
          notification != null ->
              PaymentRecord(
                  notificationId = notification.time,
                  time = notification.time,
                  category = category,
                  comment = comment,
                  merchant = merchant,
                  refunds =
                      sum.refunds.map {
                        Refund(
                            sum = it.sum.toIntOrNull() ?: 0,
                            comment = it.comment,
                        )
                      },
                  sum = 0,
                  trip = trip.takeIf { it.isNotEmpty() },
              )
          else ->
              PaymentRecord(
                  notificationId = null,
                  time = Instant.now().toEpochMilli(),
                  category = category,
                  comment = comment,
                  merchant = merchant,
                  refunds =
                      sum.refunds.map {
                        Refund(
                            sum = it.sum.toIntOrNull() ?: 0,
                            comment = it.comment,
                        )
                      },
                  sum = sum.initialSum.toIntOrNull() ?: 0,
                  trip = trip.takeIf { it.isNotEmpty() },
              )
        }

    viewModelScope.launch {
      paymentsRepository.changeOrCreatePayment(paymentRecord?.id, toSave)
      swipedPaymentsRepository.remove(notification?.time)
      onSave()
    }
  }
}

package simple.payment.tracker

import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.logging.Logger

sealed class Payment {
  abstract val merchant: String
  abstract val sum: Int
  abstract val comment: String
  abstract val time: Long
  abstract val category: String
  abstract val trip: String?
}

data class ManualPayment(
    val payment: PaymentRecord,
) : Payment() {
  override val merchant: String
    get() = payment.merchant
  override val sum: Int
    get() = payment.sum ?: 0
  override val comment: String
    get() = payment.comment
  override val time: Long
    get() = payment.time
  override val category: String
    get() = payment.category
  override val trip: String?
    get() = payment.trip
}

data class PaypalPayment(
    val payment: PaymentRecord,
    val notification: Notification,
) : Payment() {
  override val merchant: String = payment.merchant
  override val sum: Int = notification.sum()
  override val comment: String = payment.comment
  override val time: Long = notification.time
  override val category: String = payment.category
  override val trip: String? = payment.trip
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

/** Aggregates notifications and payments into one flat list ready for presentation. */
class TransactionsRepository(
    private val logger: Logger,
    private val notificationsFlow: Flow<List<Notification>>,
    private val paymentsFlow: Flow<List<PaymentRecord>>,
    private val matchersFlow: Flow<List<PaymentMatcher>>,
) {
  private val scope = CoroutineScope(Dispatchers.Unconfined)

  private val processedNotifications =
      combine(notificationsFlow, matchersFlow) { notifications, matchers ->
            ProcessedNotifications(notifications, matchers)
          }
          .flowOn(Dispatchers.Default)
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  private val transactions: Flow<List<Payment>> by lazy {
    combine(
            paymentsFlow,
            processedNotifications,
        ) { payments: List<PaymentRecord>, notifications: ProcessedNotifications,
          ->
          buildTransactionsList(notifications, payments)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(45.seconds.inWholeMilliseconds), 1)
  }

  val inbox: Flow<List<Payment>> by lazy {
    combine(
            paymentsFlow,
            processedNotifications,
        ) { payments: List<PaymentRecord>, notifications: ProcessedNotifications,
          ->
          buildTransactionsList(notifications, payments, onlyInbox = true)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(45.seconds.inWholeMilliseconds), 1)
  }

  private fun buildTransactionsList(
      notifications: ProcessedNotifications,
      payments: List<PaymentRecord>,
      onlyInbox: Boolean = false,
  ): List<Payment> {
    return logger.logTimedValue("combine") {
      logger.logTimedValue("  warmup idToGroup") { notifications.idToGroup }
      logger.logTimedValue("  warmup automaticPayments") { notifications.automaticPayments.values }

      val manuallyAssignedNotificationIds = payments.mapNotNull { it.notificationId }.toSet()

      val fullAutoPayments by lazy {
        logger.logTimedValue("  fullAutoPayments") {
          notifications.automaticPayments.minus(manuallyAssignedNotificationIds).values
        }
      }

      val inbox: List<InboxPayment> =
          logger.logTimedValue("  inbox") {
            val enteredIdsWithDuplicates =
                logger.logTimedValue("    enteredIdsWithDuplicates") {
                  manuallyAssignedNotificationIds
                      .flatMap { id ->
                        // for the given ID find id's of all duplicate notifications
                        notifications.idToGroup.getOrDefault(id, emptyList()) + id
                      }
                      .toSet()
                }

            val automaticallyAssignedNotifications =
                notifications.automaticPayments.values.mapNotNull { it.notification }.toSet()

            notifications.idToGroup
                .minus(enteredIdsWithDuplicates)
                .values
                .distinct()
                .map { notifications.notificationsById.getValue(it.first()) }
                .minus(automaticallyAssignedNotifications)
                .map { InboxPayment(notification = it) }
          }

      logger.logTimedValue("  flatten and sort") {
        when {
          onlyInbox -> inbox
          else -> {
            val (paymentsWithNotifications, paymentsWithoutNotifications) =
                payments.partition { it.notificationId != null }

            val manualPayments = paymentsWithoutNotifications.map { ManualPayment(payment = it) }

            val paypalPayments =
                paymentsWithNotifications.mapNotNull { payment ->
                  notifications.notificationsById[checkNotNull(payment.notificationId)] //
                      ?.let { notification -> PaypalPayment(payment, notification) }
                      .also {
                        if (it == null) {
                          logger.warning { "Something is off: $payment" }
                        }
                      }
                }

            listOf(
                    manualPayments,
                    paypalPayments,
                    inbox,
                    fullAutoPayments,
                )
                .flatten()
                .sortedByDescending { it.time }
          }
        }
      }
    }
  }

  fun transactions(): Flow<List<Payment>> {
    return transactions
  }

  companion object {
    fun create(
        logger: Logger,
        paymentsRepository: PaymentsRepository,
        notificationsRepository: NotificationsRepository,
        automaticPaymentsRepository: AutomaticPaymentsRepository,
    ): TransactionsRepository {
      return TransactionsRepository(
          logger,
          notificationsRepository.notifications(),
          paymentsRepository.payments(),
          automaticPaymentsRepository.matchers(),
      )
    }

    fun createForTest(
        logger: Logger,
        notificationsFlow: Flow<List<Notification>>,
        paymentsFlow: Flow<List<PaymentRecord>>,
        matchersFlow: Flow<List<PaymentMatcher>>
    ): TransactionsRepository {
      return TransactionsRepository(logger, notificationsFlow, paymentsFlow, matchersFlow)
    }
  }
}

class ProcessedNotifications(
    private val rawNotifications: List<Notification>,
    private val matchers: List<PaymentMatcher>,
) {
  private val notifications by lazy {
    rawNotifications
        .asSequence()
        .filter { notification -> "You received" !in notification.text }
        .filter { notification -> "Partial refund" !in notification.text }
        .filter { notification -> !notification.text.startsWith("Are you trying") }
        .filter { notification -> notification.sum() != 0 }
        .toList()
  }

  /** Finds notifications without explicit payments and removes duplicates */
  val idToGroup: Map<Long, Set<Long>> by lazy {
    notifications
        .groupBy { it.text }
        .flatMap { (text, notificationsWithEqualText) ->
          findDuplicates(notificationsWithEqualText)
        }
        .toMap()
  }

  val notificationsById: Map<Long, Notification> by lazy {
    notifications.associateBy(Notification::time)
  }

  val automaticPayments: Map<Long, AutomaticPayment> by lazy {
    findAutomatic(
            idToGroup.values.distinct().map { notificationsById.getValue(it.first()) }, matchers)
        .associateBy { requireNotNull(it.notification).time }
  }
}

@OptIn(ExperimentalTime::class)
private inline fun <T> Logger.logTimedValue(name: String, block: () -> T): T {
  val (value, duration) = measureTimedValue(block)
  debug { "$name took $duration" }
  return value
}

/**
 */
fun findDuplicates(notifications: List<Notification>): List<Pair<Long, Set<Long>>> {
  return notifications.map { notification ->
    val duplicates =
        notifications.filter { other ->
          notification.text == other.text &&
              notification.device != other.device &&
              abs(notification.time - other.time) < 3 * 60 * 60 * 1000
        }
    notification.time to duplicates.map { it.time }.plus(notification.time).toSet()
  }
}

/** Run all [PaymentMatcher]s against notifications to find all automatically detectable */
fun findAutomatic(
    validNotifications: List<Notification>,
    matchers: List<PaymentMatcher>
): List<AutomaticPayment> {
  return validNotifications.mapNotNull { notification ->
    matchers.firstOrNull { it.matches(notification) }?.convert(notification)
  }
}

// You saved 1,36 EUR on a 34,10 EUR
fun Notification.sum(): Int {
  return runCatching {
        // You payed 1,34 EUR to Some Guy"
        val sum =
            text
                .substringAfter("You paid ")
                .substringAfter("You sent ")
                .substringAfter(" on a ")
                .substringBefore(",")
                .substringBefore(".")
        val youSaved: Int =
            when {
              "You saved " in text -> text.substringAfter("You saved ").substringBefore(",").toInt()
              else -> 0
            }
        when {
          sum.startsWith("$") -> sum.removePrefix("$").toInt() * 10 / 9
          text.contains("RUB to") -> sum.replace(".", "").toInt() / 80
          else -> sum.toIntOrNull() ?: 0
        } - youSaved
      }
      .getOrElse {
        throw IllegalArgumentException("Failed to parse sum of $this, caused by $it", it)
      }
}

fun Notification.merchant(): String {
  return text
      .substringAfterLast(" EUR to ")
      .substringAfterLast(" EUR to ")
      .substringAfterLast(" USD to ")
      .substringAfterLast(" USD to ")
      .substringAfterLast(" RUB to ")
      .substringAfterLast(" RUB to ")
      .substringAfterLast("purchase at ")
}

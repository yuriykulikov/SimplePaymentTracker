package simple.payment.tracker

import kotlin.math.abs
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.logging.Logger

data class Transaction(val payment: Payment? = null, val notification: Notification? = null) {
  val id: Long = payment?.id ?: requireNotNull(notification).time
  val merchant: String = payment?.merchant ?: requireNotNull(notification).merchant()
  val sum: Int = payment?.sum ?: requireNotNull(notification).sum()
  val comment: String = payment?.comment ?: ""
  val cancelled: Boolean = payment?.cancelled ?: false
  val time: Long = payment?.time ?: requireNotNull(notification).time
  val category: String = payment?.category ?: ""
  val trip: String? = payment?.trip
}

/** Aggregates notifications and payments into one flat list ready for presentation. */
class TransactionsRepository(
    private val logger: Logger,
    private val notificationsFlow: Flow<List<Notification>>,
    private val paymentsFlow: Flow<List<Payment>>,
    private val matchersFlow: Flow<List<PaymentMatcher>>,
) {
  private val scope = CoroutineScope(Dispatchers.Unconfined)

  private val processedNotifications =
      combine(notificationsFlow, matchersFlow) { notifications, matchers ->
            ProcessedNotifications(notifications, matchers)
          }
          .flowOn(Dispatchers.Default)
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  private val transactions: Flow<List<Transaction>> by lazy {
    combine(
            paymentsFlow,
            processedNotifications,
        ) { payments: List<Payment>, notifications: ProcessedNotifications,
          ->
          buildTransactionsList(notifications, payments)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(45.seconds.inWholeMilliseconds), 1)
  }

  val inbox: Flow<List<Transaction>> by lazy {
    combine(
            paymentsFlow,
            processedNotifications,
        ) { payments: List<Payment>, notifications: ProcessedNotifications,
          ->
          buildTransactionsList(notifications, payments, onlyInbox = true)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(45.seconds.inWholeMilliseconds), 1)
  }

  private fun buildTransactionsList(
      notifications: ProcessedNotifications,
      payments: List<Payment>,
      onlyInbox: Boolean = false,
  ) =
      logger.logTimedValue("combine") {
        logger.logTimedValue("  warmup idToGroup") { notifications.idToGroup }
        logger.logTimedValue("  warmup automaticPayments") {
          notifications.automaticPayments.values
        }

        val manuallyAssignedNotificationIds = payments.mapNotNull { it.notificationId }.toSet()

        val fullAutoPayments by lazy {
          logger.logTimedValue("  fullAutoPayments") {
            notifications.automaticPayments.minus(manuallyAssignedNotificationIds).values
          }
        }

        val inbox =
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
                  .map { Transaction(notification = it) }
            }

        logger.logTimedValue("  flatten and sort") {
          when {
            onlyInbox -> inbox
            else -> {
              listOf(
                      payments.map { Transaction(payment = it) },
                      inbox,
                      fullAutoPayments,
                  )
                  .flatten()
                  .sortedByDescending { it.time }
            }
          }
        }
      }

  fun transactions(): Flow<List<Transaction>> {
    return transactions.retry {
      logger.warning { "Flow transactions() failed: $it, retry in 1 second!" }
      delay(1.seconds)
      true
    }
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
        paymentsFlow: Flow<List<Payment>>,
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

  val automaticPayments: Map<Long, Transaction> by lazy {
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
): List<Transaction> {
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

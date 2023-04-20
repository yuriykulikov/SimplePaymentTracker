package simple.payment.tracker

import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.logging.Logger

/** Aggregates notifications and payments into one flat list ready for presentation. */
class Transactions(
    private val logger: Logger,
    private val notificationsFlow: Flow<List<Notification>>,
    private val paymentsFlow: Flow<List<PaymentRecord>>,
    private val matchersFlow: Flow<List<PaymentMatcher>>,
) {
  private val scope = CoroutineScope(Dispatchers.Unconfined)

  private val processedNotifications =
      notificationsFlow
          .map { logger.logTimedValue("ProcessedNotifications") { ProcessedNotifications(it) } }
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  private val transactions: Flow<List<Payment>> by lazy {
    combine(
            paymentsFlow,
            processedNotifications,
            matchersFlow,
        ) { payments: List<PaymentRecord>, notifications: ProcessedNotifications, matchers,
          ->
          buildTransactionsList(notifications, payments, matchers)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(45.seconds.inWholeMilliseconds), 1)
  }

  val inbox: Flow<List<Payment>> by lazy {
    combine(
            paymentsFlow,
            processedNotifications,
            matchersFlow,
        ) { payments: List<PaymentRecord>, notifications: ProcessedNotifications, matchers,
          ->
          buildTransactionsList(notifications, payments, matchers, onlyInbox = true)
        }
        .shareIn(scope, SharingStarted.WhileSubscribed(45.seconds.inWholeMilliseconds), 1)
  }

  private fun buildTransactionsList(
      notifications: ProcessedNotifications,
      payments: List<PaymentRecord>,
      matchers: List<PaymentMatcher>,
      onlyInbox: Boolean = false,
  ): List<Payment> {
    return logger.logTimedValue("combine") {
      val (recordsWithNotifications, manualRecords) =
          payments.partition { it.notificationId != null }
      val paymentsByNotificationId = recordsWithNotifications.associateBy { it.notificationId!! }

      val fromNotifications =
          notifications.grouped.map { group ->
            val assigned: Notification? =
                group.notifications.firstOrNull { notification ->
                  notification.time in paymentsByNotificationId
                }

            val matched by
                lazy(LazyThreadSafetyMode.NONE) {
                  matchers.firstOrNull { it.matches(group.notifications.first()) }
                }

            when {
              assigned != null ->
                  PaypalPayment(paymentsByNotificationId.getValue(assigned.time), assigned)
              matched != null -> requireNotNull(matched).convert(group.notifications.first())
              else -> InboxPayment(group.notifications.first())
            }
          }

      val transactions =
          if (onlyInbox) {
            fromNotifications.filterIsInstance<InboxPayment>()
          } else {
            fromNotifications + manualRecords.map { ManualPayment(payment = it) }
          }
      transactions.sortedByDescending { it.time }
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
        automaticPaymentsRepository: PaymentMatcherRepository,
    ): Transactions {
      return Transactions(
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
    ): Transactions {
      return Transactions(logger, notificationsFlow, paymentsFlow, matchersFlow)
    }
  }
}

data class NotificationGroup(
    val notifications: List<Notification>,
)

class ProcessedNotifications(
    private val rawNotifications: List<Notification>,
) {
  val grouped: List<NotificationGroup>
    get() {
      return rawNotifications
          .asSequence()
          .filter { notification -> "You received" !in notification.text }
          .filter { notification -> "Partial refund" !in notification.text }
          .filter { notification -> !notification.text.startsWith("Are you trying") }
          .filter { notification -> notification.sum() != 0 }
          .groupBy { it.text }
          .flatMap { (_, notificationsWithEqualText) ->
            clusterNotifications(notificationsWithEqualText)
          }
          .map { NotificationGroup(it) }
    }

  private val CLUSTER_SIZE = 3 * 60 * 60 * 1000 // 3 hours

  private fun clusterNotifications(notifications: List<Notification>): List<List<Notification>> {
    return notifications
        .sortedBy { it.time }
        .fold(emptyList<List<Notification>>() to emptyList<Notification>()) {
            (clusters: List<List<Notification>>, cluster: List<Notification>),
            next ->
          when {
            cluster.isEmpty() -> clusters to listOf(next)
            // add to current cluster
            next.time - cluster.last().time <= CLUSTER_SIZE -> clusters to (cluster + next)
            // add to new cluster
            else -> {
              val withAddedCluster = clusters + listOf(cluster)
              val newCluster = listOf(next)
              withAddedCluster to newCluster
            }
          }
        }
        .let { (clusters, lastCluster) -> clusters + listOf(lastCluster) }
        .filterNot { it.isEmpty() }
  }
}

@OptIn(ExperimentalTime::class)
private inline fun <T> Logger.logTimedValue(name: String, block: () -> T): T {
  val (value, duration) = measureTimedValue(block)
  debug { "$name took $duration" }
  return value
}

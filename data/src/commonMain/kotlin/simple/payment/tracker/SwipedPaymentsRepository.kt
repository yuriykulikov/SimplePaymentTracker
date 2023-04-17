package simple.payment.tracker

import kotlinx.coroutines.flow.Flow

@kotlinx.serialization.Serializable
data class SwipedPayment(
    val swipedBy: String,
    val notification: Notification,
)

interface SwipedPaymentsRepository {
  fun swiped(): Flow<Set<Notification>>
  suspend fun swipe(notification: Notification)
  suspend fun remove(notificationId: Long?)
}

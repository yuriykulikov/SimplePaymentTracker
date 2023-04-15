package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine

class SwipedPaymentsRepository(
    firebaseDatabase: FirebaseDatabase,
    private val signedInUserEmail: StateFlow<String?>,
) {
  private val ref = firebaseDatabase.reference("swiped")

  fun swiped(): Flow<Set<Notification>> {
    return combine(signedInUserEmail, this.ref.valueEvents) { user, snapshot ->
      runCatching {
            snapshot
                .value<Map<String, SwipedPayment>?>()
                ?.values
                .orEmpty()
                .filter { it.swipedBy == user }
                .map { it.notification }
                .toSet()
          }
          .getOrDefault(emptySet())
    }
  }

  suspend fun swipe(notification: Notification) {
    ref.child(notification.time.toString())
        .setValue(SwipedPayment(signedInUserEmail.value ?: "", notification))
  }

  suspend fun remove(notificationId: Long?) {
    if (notificationId != null) {
      ref.child(notificationId.toString()).removeValue()
    }
  }
}

@kotlinx.serialization.Serializable
private data class SwipedPayment(
    val swipedBy: String,
    val notification: Notification,
)

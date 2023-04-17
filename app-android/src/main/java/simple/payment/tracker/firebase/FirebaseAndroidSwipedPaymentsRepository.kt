package simple.payment.tracker.firebase

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import simple.payment.tracker.Notification
import simple.payment.tracker.SwipedPayment
import simple.payment.tracker.SwipedPaymentsRepository

class FirebaseAndroidSwipedPaymentsRepository(
    firebaseDatabase: FirebaseDatabase,
    private val signedInUserEmail: StateFlow<String?>,
) : SwipedPaymentsRepository {
  private val ref = firebaseDatabase.reference("swiped")

  override fun swiped(): Flow<Set<Notification>> {
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

  override suspend fun swipe(notification: Notification) {
    ref.child(notification.time.toString())
        .setValue(SwipedPayment(signedInUserEmail.value ?: "", notification))
  }

  override suspend fun remove(notificationId: Long?) {
    if (notificationId != null) {
      ref.child(notificationId.toString()).removeValue()
    }
  }
}

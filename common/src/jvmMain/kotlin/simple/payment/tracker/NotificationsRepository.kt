package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.Serializable
import simple.payment.tracker.logging.Logger

@Serializable
data class Notification(
    val time: Long,
    val text: String,
    val device: String? = null,
)

/** Notifications can only be added, never removed. */
class NotificationsRepository(
    private val logger: Logger,
    private val firebaseDatabase: FirebaseDatabase
) {
  private val scope = CoroutineScope(Dispatchers.Unconfined)
  private val notificationsRef = firebaseDatabase.reference("notifications")

  private val notifications: Flow<List<Notification>> =
      notificationsRef.valueEvents
          .map { it.value<Map<String, Notification>>().values.toList() }
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  fun notifications(): Flow<List<Notification>> = notifications

  suspend fun addNotifications(newNotifications: List<Notification>) {
    logger.debug { "Adding notifications: $newNotifications" }
    newNotifications.forEach { notification ->
      notificationsRef.child(notification.time.toString()).setValue(notification)
    }
  }
}

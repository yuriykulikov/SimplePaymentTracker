package simple.payment.tracker.firebase

import dev.gitlive.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import simple.payment.tracker.Notification
import simple.payment.tracker.NotificationsRepository
import simple.payment.tracker.logging.Logger

/** Notifications can only be added, never removed. */
class FirebaseAndroidNotificationsRepository(
    private val logger: Logger,
    private val firebaseDatabase: FirebaseDatabase
) : NotificationsRepository {
  private val scope = CoroutineScope(Dispatchers.Default)
  private val notificationsRef = firebaseDatabase.reference("notifications")

  private val notifications: Flow<List<Notification>> =
      notificationsRef.valueEvents
          .map { it.value<Map<String, Notification>>().values.toList() }
          .shareIn(scope, SharingStarted.WhileSubscribed(250), 1)

  override fun notifications(): Flow<List<Notification>> = notifications

  override suspend fun addNotifications(newNotifications: List<Notification>) {
    logger.debug { "Adding notifications: $newNotifications" }
    newNotifications.forEach { notification ->
      notificationsRef.child(notification.time.toString()).setValue(notification)
    }
  }
}

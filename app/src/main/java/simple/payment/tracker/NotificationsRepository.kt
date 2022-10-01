package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable
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
  private val notificationsRef = firebaseDatabase.reference("notifications")

  private val notifications: Observable<List<Notification>> =
      notificationsRef.valueEvents
          .map { it.value<Map<String, Notification>>().values.toList() }
          .asObservable()
          .replay(1)
          .refCount()

  fun notifications(): Observable<List<Notification>> = notifications

  suspend fun addNotifications(newNotifications: List<Notification>) {
    logger.debug { "Adding notifications: $newNotifications" }
    newNotifications.forEach { notification ->
      notificationsRef.child(notification.time.toString()).setValue(notification)
    }
  }
}

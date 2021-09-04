package simple.payment.tracker

import io.reactivex.Observable

data class Notification(
    val time: Long,
    val text: String,
    val device: String?,
)

/** Notifications can only be added, never removed. */
class NotificationsRepository(private val logger: Logger, private val firebaseDatabase: Firebase) {
  private val notificationsRef =
      firebaseDatabase.child(
          "notifications",
          mapper = { map ->
            Notification(
                time = map["time"] as Long,
                text = map["text"] as String,
                device = map["device"] as String?,
            )
          })
  private val notifications: Observable<List<Notification>> =
      notificationsRef.observe().map { it.values.toList() }

  fun notifications(): Observable<List<Notification>> = notifications

  fun addNotifications(newNotifications: List<Notification>) {
    logger.debug { "Adding notifications: $newNotifications" }
    newNotifications.forEach { notification ->
      notificationsRef.put(notification.time.toString(), notification)
    }
  }
}

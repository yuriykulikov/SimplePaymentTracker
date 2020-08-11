package simple.payment.tracker

import com.squareup.moshi.Moshi
import io.reactivex.Observable
import simple.payment.tracker.stores.FileDataStore
import simple.payment.tracker.stores.Filer
import simple.payment.tracker.stores.listDataStore
import simple.payment.tracker.stores.modify

data class Notification(
  val time: Long,
  val text: String
)

/**
 * Notifications can only be added, never removed.
 */
class NotificationsRepository(
  private val logger: Logger,
  private val filer: Filer,
  private val moshi: Moshi,
  private val firebaseDatabase: Firebase
) {
  private val fileStore: FileDataStore<List<Notification>> = FileDataStore.listDataStore(
    filer,
    "notifications.txt",
    "[]",
    moshi
  )

  private val notificationsRef = firebaseDatabase
    .child("notifications", mapper = { map ->
      Notification(
        time = map["time"] as Long,
        text = map["text"] as String
      )
    })
  private val notifications: Observable<List<Notification>> = notificationsRef
    .observe()
    .map { it.values.toList() }

  fun notifications(): Observable<List<Notification>> = notifications

  fun addNotifications(newNotifications: List<Notification>) {
    logger.debug { "Adding notifications: $newNotifications" }
    fileStore.modify { plus(newNotifications) }
    newNotifications.forEach { notification ->
      notificationsRef.put(notification.time.toString(), notification)
    }
  }
}

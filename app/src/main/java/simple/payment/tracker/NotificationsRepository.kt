package simple.payment.tracker

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
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
    private val firebaseDatabase: FirebaseDatabase
) {
    private val fileStore: FileDataStore<List<Notification>> = FileDataStore.listDataStore(
        filer,
        "notifications.txt",
        "[]",
        moshi
    )

    private val notifications: Observable<List<Notification>>

    private val notificationsReference: DatabaseReference = firebaseDatabase
        .reference
        .child("notifications")

    init {
        notifications = notificationsReference
            .observe(mapper = { map ->
                Notification(
                    time = map["time"] as Long,
                    text = map["text"] as String
                )
            })
            .map { it.values.toList() }
            .replay(1)
            .refCount()
    }

    fun notifications(): Observable<List<Notification>> = notifications

    fun addNotifications(newNotifications: List<Notification>) {
        logger.debug { "Adding notifications: $newNotifications" }
        fileStore.modify { plus(newNotifications) }
        newNotifications.forEach { notification ->
            notificationsReference.child(notification.time.toString()).setValue(notification)
        }
    }
}

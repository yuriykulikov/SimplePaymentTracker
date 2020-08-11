package simple.payment.tracker

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable

interface FireChild<T> {
  fun observe(): Observable<Map<String, T>>
  fun put(id: String, value: T)
  fun remove(id: String)
}

class Firebase(
  private val logger: Logger,
  dataBase: FirebaseDatabase
) {
  private val reference: DatabaseReference = dataBase.reference

  fun <T> child(pathString: String, mapper: (HashMap<String, Any>) -> T): FireChild<T> {
    val childRef = reference.child(pathString)

    return object : FireChild<T> {
      override fun observe(): Observable<Map<String, T>> {
        return Observable.create<Map<String, T>> { emitter ->
          logger.debug { "Subscribing with $mapper" }

          val listener = childRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
              emitter.onError(RuntimeException("onCancelled: $databaseError"))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
              val hashMap = snapshot.value as HashMap<String, HashMap<String, Any>>?
              if (hashMap != null) {

                val updated = hashMap.mapValues { (_, value) ->
                  mapper(value)
                }

                logger.debug { "Emitting values for $mapper" }
                emitter.onNext(updated)
              }
            }
          })

          emitter.setCancellable {
            logger.debug { "Unsubscribing with $mapper" }
            childRef.removeEventListener(listener)
          }
        }
          .replay(1)
          .refCount()
      }

      override fun put(id: String, value: T) {
        childRef.child(id).setValue(value)
      }

      override fun remove(id: String) {
        childRef.child(id).removeValue()
      }
    }
  }
}
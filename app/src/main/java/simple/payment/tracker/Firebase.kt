package simple.payment.tracker

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable

fun <T> DatabaseReference.observe(mapper: (HashMap<String, Any>) -> T): Observable<Map<String, T>> {
    return Observable.create { emitter ->
        val listener = addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                emitter.onError(RuntimeException("onCancelled: $databaseError"))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val hashMap = snapshot.value as HashMap<String, HashMap<String, Any>>?
                if (hashMap != null) {

                    val updated = hashMap.mapValues { (_, value) ->
                        mapper(value)
                    }

                    emitter.onNext(updated)
                }
            }
        })

        emitter.setCancellable {
            removeEventListener(listener)
        }
    }
}
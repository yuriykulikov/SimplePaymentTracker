package simple.payment.tracker

import dev.gitlive.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asObservable

class TinkoffPaymentsRepository(private val firebaseDatabase: FirebaseDatabase) {
  val payments: Observable<List<Payment>> =
      firebaseDatabase
          .reference("tinkoffpayments")
          .valueEvents
          .map { it.value<Map<String, Payment>>().values.toList() }
          .asObservable()
          .replay(1)
          .refCount()
}

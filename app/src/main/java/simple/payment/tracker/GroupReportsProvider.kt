package simple.payment.tracker

import io.reactivex.Observable

interface GroupReportsProvider {

  fun reports(): Observable<List<GroupReport>>
  fun report(name: String): Observable<GroupReport>
}

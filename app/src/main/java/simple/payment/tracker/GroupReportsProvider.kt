package simple.payment.tracker

import kotlinx.coroutines.flow.Flow

interface GroupReportsProvider {

  fun reports(): Flow<List<GroupReport>>
  fun report(name: String): Flow<GroupReport>
}

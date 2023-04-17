package simple.payment.tracker

import kotlinx.coroutines.flow.Flow

interface TinkoffPaymentsRepository {
  val payments: Flow<List<Payment>>
}

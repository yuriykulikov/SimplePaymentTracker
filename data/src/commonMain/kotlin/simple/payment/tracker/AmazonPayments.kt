package simple.payment.tracker

import kotlinx.coroutines.flow.Flow

interface AmazonPaymentsRepository {
  val payments: Flow<List<AmazonPayment>>
}

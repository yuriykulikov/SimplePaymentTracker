package simple.payment.tracker

import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

class Recurring {
  private val firebaseAccess = FirebaseTestAccess()

  @Test
  fun `read recurring payments`() =
      runBlocking<Unit> {
        val read: Map<String, RecurringPaymentRecord> = firebaseAccess.recurring()
        read.values.forEach { println(it) }
        read.values shouldHaveSize 20
      }

  @Ignore("write")
  @Test
  fun `write recurring payments`() = runBlocking {
    firebaseAccess.put(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/recurringpayments.json",
        firebaseAccess.recurring())
  }
}

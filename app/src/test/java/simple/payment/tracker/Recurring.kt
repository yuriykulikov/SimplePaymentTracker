package simple.payment.tracker

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class Recurring {
  private val firebaseAccess = FirebaseTestAccess()

  @Test
  fun `read recurring payments`() =
      runBlocking<Unit> {
        val read: Map<String, RecurrringPayment> = firebaseAccess.recurring()
        read.values.forEach { println(it) }
        Assertions.assertThat(read.values).hasSize(16)
      }

  @Disabled
  @Test
  fun `write recurring payments`() = runBlocking {
    firebaseAccess.put(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/recurringpayments.json",
        firebaseAccess.recurring())
  }
}

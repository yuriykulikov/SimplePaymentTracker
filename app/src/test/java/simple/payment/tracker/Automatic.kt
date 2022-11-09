package simple.payment.tracker

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class Automatic {
  private val firebaseAccess = FirebaseTestAccess()

  @Test
  fun `read automatic payments`() =
      runBlocking<Unit> {
        val read: Map<String, PaymentMatcher> = firebaseAccess.automatic()
        read.values.forEach { println(it) }
        assertThat(read.values).hasSize(31)
      }

  @Disabled
  @Test
  fun `write automatic payments`() = runBlocking {
    firebaseAccess.put(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/automatic.json",
        firebaseAccess.automatic())
  }

  @Disabled
  @Test
  fun `add automatic payment`() = runBlocking {
    firebaseAccess.post(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/automatic.json",
        PaymentMatcher(
            category = "",
            merchant = "",
        ))
  }
}

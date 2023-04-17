package simple.payment.tracker

import io.kotest.matchers.collections.shouldHaveSize
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

class Automatic {
  private val firebaseAccess = FirebaseTestAccess()

  @Test
  fun `read automatic payments`() =
      runBlocking<Unit> {
        val read: Map<String, PaymentMatcher> = firebaseAccess.automatic()
        read.values.forEach { println(it) }
        read.values shouldHaveSize 31
      }

  @Ignore("write")
  @Test
  fun `write automatic payments`() = runBlocking {
    firebaseAccess.put(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/automatic.json",
        firebaseAccess.automatic())
  }

  @Ignore("write")
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

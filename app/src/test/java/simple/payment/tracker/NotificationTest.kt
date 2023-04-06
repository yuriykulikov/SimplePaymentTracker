package simple.payment.tracker

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class NotificationTest {
  @Test
  fun parse() {
    Notification(0, "You sent 290.00 € EUR to Marcus Feustel.", "Pixel 4a").sumCents() shouldBe
        29000
  }
}

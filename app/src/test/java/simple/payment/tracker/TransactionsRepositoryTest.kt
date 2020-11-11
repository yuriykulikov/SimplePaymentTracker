package simple.payment.tracker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TransactionsRepositoryTest {
  @Test
  fun `unassigned notifications from two devices are shown as one transaction`() {
    val aggregated = aggregate(
      emptyList(),
      listOf(
        Notification(
          time = 1600267655959,
          device = "Pixel 3a",
          text = "You paid 17.00 EUR to GRUBIGSTEINBAHN IV"
        ),
        Notification(
          time = 1600267718253,
          device = "Pixel 4",
          text = "You paid 17.00 EUR to GRUBIGSTEINBAHN IV",
        )
      ).associateBy { it.time }
    )

    assertThat(aggregated).hasSize(1)
  }
}
package simple.payment.tracker

import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class AmazonPaymentsParsing {
  @Disabled
  @Test
  fun `upload amazon json to firebase`() =
      runBlocking<Unit> {
        FirebaseTestAccess()
            .put(
                "https://simplepaymenttracker.firebaseio.com/amazonpayments.json",
                parseAmazonPayments().associateBy { it.orderId })
      }

  private val dateFormats =
      listOf(
          SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY),
          SimpleDateFormat("dd MMM YYYY", Locale.GERMANY),
          SimpleDateFormat("dd MMM YYYY", Locale.US),
      )

  private fun parseAmazonPayments(): List<AmazonPayment> {
    val loader = requireNotNull(Thread.currentThread().contextClassLoader)

    return listOf(
            loader.getResource("Amazon - 2019.tsv"),
            loader.getResource("Amazon - 2020.tsv"),
            loader.getResource("Amazon - 2021.tsv"),
            loader.getResource("Amazon - 2022.tsv"),
        )
        .map { it.readText(charset = Charsets.UTF_8) }
        .flatMap { it.lines() }
        .filterNot { line -> line.startsWith("order id") }
        .mapNotNull { parseLine(it) }
  }

  private fun parseLine(line: String): AmazonPayment? {
    val split = line.split("\t")
    val orderId = split[0]
    val dateText = split[3]
    val (sumText, category, comment) = split.takeLast(3)
    val sum = sumText.substringBefore(",").toInt()
    val date = dateFormats.firstNotNullOf { runCatching { it.parse(dateText) }.getOrNull() }
    return if (sum != 0) {
      AmazonPayment(
          category = category, time = date.time, sum = sum, comment = comment, orderId = orderId)
    } else {
      null
    }
  }
}

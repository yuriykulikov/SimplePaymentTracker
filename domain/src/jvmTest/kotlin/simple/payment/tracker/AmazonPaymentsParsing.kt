package simple.payment.tracker

import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test

class AmazonPaymentsParsing {
  @Test
  fun printAmazonPayments() {
    parseAmazonPayments().forEach { println(it) }
  }

  @Ignore
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

    return listOfNotNull(
            loader.getResource("Amazon - 2019.tsv"),
            loader.getResource("Amazon - 2020.tsv"),
            loader.getResource("Amazon - 2021.tsv"),
            loader.getResource("Amazon - 2022.tsv"),
            loader.getResource("Amazon - 2023.tsv"),
        )
        .map { it.readText(charset = Charsets.UTF_8) }
        .flatMap {
          val lines = it.lines()
          val indexOfSum = lines.first().split("\t").indexOf("sum")
          val indexOfTransfer = lines.first().split("\t").indexOf("transfer")
          lines.drop(1).mapNotNull { line -> parseLine(line, indexOfSum, indexOfTransfer) }
        }
  }

  private fun parseLine(line: String, indexOfSum: Int, indexOfTransfer: Int): AmazonPayment? {
    val split = line.split("\t")
    val orderId = split[0]
    val dateText = split[3]
    val (sumText, category, comment, user) = split.subList(indexOfSum, indexOfSum + 4)
    val sum = sumText.substringBefore(",").toInt()
    val refund = split.getOrNull(indexOfSum + 5)?.substringBefore(",")?.toIntOrNull() ?: 0
    val transfer = split.getOrNull(indexOfTransfer)?.substringBefore(",")?.toIntOrNull() ?: 0
    val date = dateFormats.firstNotNullOf { runCatching { it.parse(dateText) }.getOrNull() }
    return if (sum != 0) {
      AmazonPayment(
          category = category,
          time = date.time,
          sum = sum - refund - transfer,
          comment = comment,
          orderId = orderId,
          user = user)
    } else {
      null
    }
  }
}

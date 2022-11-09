package simple.payment.tracker

import ch.qos.logback.core.ConsoleAppender
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.rxObservable
import org.junit.jupiter.api.Test
import simple.payment.tracker.logging.addAppender
import simple.payment.tracker.logging.hide
import simple.payment.tracker.logging.logback
import simple.payment.tracker.logging.patternLayoutEncoder

class Crunching {
  private val firebase = FirebaseTestAccess()
  private val logger =
      logback {
            addAppender(ConsoleAppender()) { //
              encoder = patternLayoutEncoder("[%thread] - %msg%n")
            }
          }
          .hide()
          .createLogger("test")

  private suspend fun merged(): List<Payment> {
    val aggregated: List<Transaction> =
        TransactionsRepository.createForTest(
                logger,
                rxObservable { trySend(firebase.notifications().values.toList()) },
                rxObservable { trySend(firebase.payments().values.toList()) },
                rxObservable { trySend(firebase.automatic().values.toList()) },
            )
            .transactions()
            .blockingFirst()

    val july2019 = dateFormat.parse("2019-07-01")?.time ?: 0
    return (aggregated.mapNotNull { it.payment } +
            firebase.amazon().values.map { it.toPayment() } +
            firebase.recurring().values.toList().toPayments())
        .filter { it.time >= july2019 }
  }

  @Test
  fun `month sums`() = runBlocking {
    val merged = merged()
    MonthlyStatistics.monthly(merged).forEach { println(it.toString()) }
  }

  @Test
  fun `category sums average`() = runBlocking {
    val merged = merged()

    println(merged.sumOf { it.sum / 36 })

    merged
        .filter { it.trip == null }
        .groupBy { it.category }
        .mapValues { (key, value) -> value.sumOf { it.sum } / 36 }
        .entries
        .sortedByDescending { (cat, sum) -> sum }
        .forEach { (cat, sum) -> println("${cat.padEnd(25)}: $sum") }
  }

  @Test
  fun `crunch trips`() = runBlocking {
    firebase
        .payments()
        .values
        .filter { it.trip != null }
        .groupBy { it.trip }
        .mapValues { (trip, payments) -> payments.sumBy { it.sum ?: 0 } }
        .forEach { (trip, sum) -> println("$trip: $sum") }

    firebase
        .payments()
        .values
        .filter { it.trip != null }
        .groupBy { it.trip }
        .forEach { (trip, payments) ->
          println("#########   $trip")
          payments
              .groupBy { it.category }
              .mapValues { (_, payments) -> payments.sumOf { it.sum } }
              .forEach { (category, sum) -> println("${category.padEnd(20)} $sum") }
        }
  }

  val yearAndMonth = SimpleDateFormat("yyyy-MM", Locale.GERMANY)

  @Test
  fun `food prices`() = runBlocking {
    val merged: List<Payment> = merged()

    merged
        // .filter { it.trip == null }
        .filter { it.category == "Еда" || it.category == "Гедонизм" }
        .groupBy { yearAndMonth.format(Date.from(Instant.ofEpochMilli(it.time))) }
        .mapValues { (k, v) -> v.sumOf { it.sum } }
        .forEach { println(it) }
  }
}

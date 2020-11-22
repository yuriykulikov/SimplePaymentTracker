package simple.payment.tracker

import io.reactivex.Observable

class PaymentMatcher(
  val merchant: String,
  val category: String,
  val comment: String? = null,
  val sum: Int? = null,
) {
  fun matches(notification: Notification): Boolean {
    return notification.merchant() == merchant
      && sum?.equals(notification.sum()) ?: true
  }

  fun convert(notification: Notification): Transaction {
    return Transaction(
      Payment(
        category = category,
        time = notification.time,
        comment = comment ?: "",
        merchant = merchant,
        sum = notification.sum(),
        notificationId = notification.time,
        cancelled = false,
        trip = null,
        auto = true,
      ), notification
    )
  }
}

class AutomaticPaymentsRepository {
  fun matchers(): Observable<List<PaymentMatcher>> {
    return Observable.just(
      listOf(
        PaymentMatcher("Der Beck Fil. 352", "Еда"),
        PaymentMatcher("Der Beck Fil. 353", "Еда"),
        PaymentMatcher("Gold Thai Imbiss", "Ресторан"),
        PaymentMatcher("Takeaway.com Payments B.V.", ""),
        PaymentMatcher("Google", "Развлечения"),
        PaymentMatcher("Spotify Finance Limited", "Развлечения"),
        PaymentMatcher("Netflix.com", "Развлечения"),
        PaymentMatcher("Voi Technology Germany GmbH", "Транспорт"),
        PaymentMatcher("Tier Mobility GmbH", "Транспорт"),
        PaymentMatcher("gemeinnützige Wikimedia Fördergesellschaft mbH", "Разное"),
        PaymentMatcher("OMV 7532", "Разное"),
        PaymentMatcher("Deutsche Post AG", "Разное"),
        PaymentMatcher("DocMorris NV", "Аптека"),
        PaymentMatcher("Mozart Apotheke", "Аптека"),
        PaymentMatcher("top12 GmbH", "Гедонизм"),
        PaymentMatcher("coffeefair online", "Гедонизм"),
        PaymentMatcher("CREW Republic Brewery GmbH", "Гедонизм"),
      )
    )
  }
}
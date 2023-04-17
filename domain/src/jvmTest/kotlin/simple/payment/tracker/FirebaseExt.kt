package simple.payment.tracker

suspend fun FirebaseTestAccess.automatic(): Map<String, PaymentMatcher> =
    getOrLoad(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/automatic.json",
        cacheFile = "build/testcache/automatic.json",
    )

suspend fun FirebaseTestAccess.notifications(prod: Boolean = false): Map<String, Notification> {
  val testPrefix = if (prod) "" else "test"
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/${testPrefix}notifications.json",
      cacheFile = "build/testcache/${testPrefix}notifications.json")
}

suspend fun FirebaseTestAccess.payments(prod: Boolean = false): Map<String, PaymentRecord> {
  val testPrefix = if (prod) "" else "test"
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/${testPrefix}payments.json",
      cacheFile = "build/testcache/${testPrefix}payments.json")
}

suspend fun FirebaseTestAccess.recurring(): Map<String, RecurringPaymentRecord> {
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/recurringpayments.json",
      cacheFile = "build/testcache/recurringpayments.json")
}

suspend fun FirebaseTestAccess.amazon(): Map<String, AmazonPayment> {
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/amazonpayments.json",
      cacheFile = "build/testcache/amazonpayments.json")
}

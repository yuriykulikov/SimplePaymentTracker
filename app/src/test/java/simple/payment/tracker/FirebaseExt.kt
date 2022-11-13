package simple.payment.tracker

suspend fun FirebaseTestAccess.automatic(): Map<String, PaymentMatcher> =
    getOrLoad(
        firebaseUrl = "https://simplepaymenttracker.firebaseio.com/automatic.json",
        cacheFile = "build/testcache/automatic.json",
    )

suspend fun FirebaseTestAccess.notifications(): Map<String, Notification> {
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/testnotifications.json",
      cacheFile = "build/testcache/notifications.json")
}

suspend fun FirebaseTestAccess.payments(): Map<String, PaymentRecord> {
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/testpayments.json",
      cacheFile = "build/testcache/payments.json")
}

suspend fun FirebaseTestAccess.recurring(): Map<String, RecurrringPayment> {
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/recurringpayments.json",
      cacheFile = "build/testcache/recurring.json")
}

suspend fun FirebaseTestAccess.amazon(): Map<String, AmazonPayment> {
  return getOrLoad(
      firebaseUrl = "https://simplepaymenttracker.firebaseio.com/amazonpayments.json",
      cacheFile = "build/testcache/amazonpayments.json")
}

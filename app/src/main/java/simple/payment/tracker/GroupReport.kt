package simple.payment.tracker

data class GroupReport(val name: String, val payments: List<Payment>) {
  val sum: Int by lazy { payments.sumOf { it.sum } }
  val date: Long by lazy { payments.maxByOrNull { it.time }?.time ?: 0L }
  val categorySums by lazy {
    payments
        .groupBy { it.category }
        .mapValues { (cat, pmnts) -> pmnts.sumOf { it.sum } }
        .entries
        .sortedByDescending { (cat, sum) -> sum }
  }
}

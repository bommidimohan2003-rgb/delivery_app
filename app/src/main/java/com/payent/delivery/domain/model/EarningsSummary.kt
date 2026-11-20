package com.payent.delivery.domain.model

data class EarningsSummary(
    val totalEarnings: Double,
    val todayEarnings: Double,
    val weeklyEarnings: Double,
    val monthlyEarnings: Double,
    val completedDeliveriesCount: Int,
    val nextPayoutDate: String,
    val payoutStatus: String
)

enum class EarningsTimeRange {
    DAILY,
    WEEKLY,
    MONTHLY
}

data class ChartDataPoint(
    val label: String,
    val amountRupees: Double,
    val deliveriesCount: Int = 1,
    val dateFullText: String = ""
)

data class PayoutRecord(
    val id: String,
    val reference: String,
    val period: String,
    val amountRupees: Double,
    val scheduledDate: String,
    val statusText: String
)

data class DailyOrderEarning(
    val orderId: String,
    val orderNumber: String,
    val completedTimeText: String,
    val completedTimestampMillis: Long = System.currentTimeMillis(),
    val dateIso: String,
    val earningsRupees: Double,
    val pickupLocation: String,
    val deliveryLocation: String,
    val statusText: String = "Completed"
)

data class DayEarningsBreakdown(
    val dayOfWeek: String,
    val dayOfMonth: Int,
    val fullDateText: String,
    val dateIso: String,
    val totalEarningsRupees: Double,
    val completedDeliveriesCount: Int,
    val orders: List<DailyOrderEarning> = emptyList()
)

data class WeekEarningsBreakdown(
    val weekNumber: Int,
    val weekLabel: String,
    val dateRangeText: String,
    val totalEarningsRupees: Double,
    val completedDeliveriesCount: Int,
    val days: List<DayEarningsBreakdown> = emptyList()
)

data class MonthEarningsBreakdown(
    val monthName: String,
    val year: Int,
    val monthYearText: String,
    val totalEarningsRupees: Double,
    val completedDeliveriesCount: Int,
    val averageWeeklyEarningsRupees: Double,
    val weeks: List<WeekEarningsBreakdown> = emptyList()
)

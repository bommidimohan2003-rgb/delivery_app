package com.payent.delivery.feature.earnings

import com.payent.delivery.domain.model.ChartDataPoint
import com.payent.delivery.domain.model.DailyOrderEarning
import com.payent.delivery.domain.model.DayEarningsBreakdown
import com.payent.delivery.domain.model.EarningsSummary
import com.payent.delivery.domain.model.EarningsTimeRange
import com.payent.delivery.domain.model.MonthEarningsBreakdown
import com.payent.delivery.domain.model.PayoutRecord
import com.payent.delivery.domain.model.WeekEarningsBreakdown
import java.util.Calendar

data class EarningsUiState(
    val selectedTimeRange: EarningsTimeRange = EarningsTimeRange.DAILY,

    // Period Totals
    val periodEarningsRupees: Double = 0.0,
    val completedDeliveriesCount: Int = 0,
    val transactionCount: Int = 0,
    val averageMetricLabel: String = "Average Daily",
    val averageMetricRupees: Double = 0.0,

    // Daily breakdown state
    val selectedDailyDate: Calendar = Calendar.getInstance(),
    val dailyBreakdown: DayEarningsBreakdown = DayEarningsBreakdown(
        dayOfWeek = "Thu",
        dayOfMonth = 6,
        fullDateText = "Today, 06 Nov 2026",
        dateIso = "2026-11-06",
        totalEarningsRupees = 0.0,
        completedDeliveriesCount = 0
    ),
    val selectedOrderInDaily: DailyOrderEarning? = null,
    val isDailyNextDayDisabled: Boolean = true,

    // Weekly breakdown state
    val selectedWeekOffset: Int = 0,
    val weeklyBreakdown: WeekEarningsBreakdown = WeekEarningsBreakdown(
        weekNumber = 1,
        weekLabel = "Current Week",
        dateRangeText = "",
        totalEarningsRupees = 0.0,
        completedDeliveriesCount = 0
    ),
    val selectedDayInWeekly: DayEarningsBreakdown? = null,
    val isWeeklyNextWeekDisabled: Boolean = true,

    // Monthly breakdown state
    val selectedMonthOffset: Int = 0,
    val monthlyBreakdown: MonthEarningsBreakdown = MonthEarningsBreakdown(
        monthName = "November",
        year = 2026,
        monthYearText = "November 2026",
        totalEarningsRupees = 0.0,
        completedDeliveriesCount = 0,
        averageWeeklyEarningsRupees = 0.0
    ),
    val selectedWeekInMonthly: WeekEarningsBreakdown? = null,
    val isMonthlyNextMonthDisabled: Boolean = true,

    // Legacy fields preserved for backward compatibility
    val chartDataPoints: List<ChartDataPoint> = emptyList(),
    val selectedDataPoint: ChartDataPoint? = null,
    val totalEarningsRupees: Double = 3850.0,
    val pendingEarningsRupees: Double = 420.0,
    val adjustmentsRupees: Double = 0.0,
    val nextPayoutDateText: String = "Thursday, Nov 12, 2026",
    val eligiblePayoutEstimateRupees: Double = 2270.0,
    val bankAccountMasked: String = "Demo Bank Account •••• 1234",
    val payoutHistory: List<PayoutRecord> = emptyList(),
    val earningsSummary: EarningsSummary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isBackendConnected: Boolean = false
)

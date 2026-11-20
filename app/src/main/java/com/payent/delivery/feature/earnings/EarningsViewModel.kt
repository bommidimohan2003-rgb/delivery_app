package com.payent.delivery.feature.earnings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.ChartDataPoint
import com.payent.delivery.domain.model.DailyOrderEarning
import com.payent.delivery.domain.model.DayEarningsBreakdown
import com.payent.delivery.domain.model.EarningsSummary
import com.payent.delivery.domain.model.EarningsTimeRange
import com.payent.delivery.domain.model.MonthEarningsBreakdown
import com.payent.delivery.domain.model.PayoutRecord
import com.payent.delivery.domain.model.WeekEarningsBreakdown
import com.payent.delivery.domain.repository.EarningsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EarningsViewModel(
    private val earningsRepository: EarningsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()

    companion object {
        val sampleWeeklyPoints = listOf(
            ChartDataPoint(label = "Mon", amountRupees = 420.0, deliveriesCount = 2, dateFullText = "Mon"),
            ChartDataPoint(label = "Tue", amountRupees = 560.0, deliveriesCount = 2, dateFullText = "Tue"),
            ChartDataPoint(label = "Wed", amountRupees = 0.0, deliveriesCount = 0, dateFullText = "Wed"),
            ChartDataPoint(label = "Thu", amountRupees = 650.0, deliveriesCount = 2, dateFullText = "Thu"),
            ChartDataPoint(label = "Fri", amountRupees = 380.0, deliveriesCount = 1, dateFullText = "Fri"),
            ChartDataPoint(label = "Sat", amountRupees = 480.0, deliveriesCount = 2, dateFullText = "Sat"),
            ChartDataPoint(label = "Sun", amountRupees = 0.0, deliveriesCount = 0, dateFullText = "Sun")
        )

        val sampleDailyPoints = listOf(
            ChartDataPoint(label = "09:30 AM", amountRupees = 210.0, deliveriesCount = 1, dateFullText = "09:30 AM"),
            ChartDataPoint(label = "11:45 AM", amountRupees = 340.0, deliveriesCount = 1, dateFullText = "11:45 AM"),
            ChartDataPoint(label = "02:15 PM", amountRupees = 180.0, deliveriesCount = 1, dateFullText = "02:15 PM"),
            ChartDataPoint(label = "04:30 PM", amountRupees = 420.0, deliveriesCount = 1, dateFullText = "04:30 PM"),
            ChartDataPoint(label = "07:15 PM", amountRupees = 290.0, deliveriesCount = 1, dateFullText = "07:15 PM")
        )

        val sampleMonthlyPoints = listOf(
            ChartDataPoint(label = "W1", amountRupees = 1450.0, deliveriesCount = 4, dateFullText = "Week 1"),
            ChartDataPoint(label = "W2", amountRupees = 2490.0, deliveriesCount = 9, dateFullText = "Week 2"),
            ChartDataPoint(label = "W3", amountRupees = 2100.0, deliveriesCount = 7, dateFullText = "Week 3"),
            ChartDataPoint(label = "W4", amountRupees = 1980.0, deliveriesCount = 6, dateFullText = "Week 4")
        )

        val samplePayoutHistory = listOf(
            PayoutRecord(
                id = "payout_001",
                reference = "PAYOUT-2026-081",
                period = "Oct 29 - Nov 04, 2026",
                amountRupees = 2490.00,
                scheduledDate = "Thu, Nov 05, 2026",
                statusText = "PAID (DEMO)"
            ),
            PayoutRecord(
                id = "payout_002",
                reference = "PAYOUT-2026-074",
                period = "Oct 22 - Oct 28, 2026",
                amountRupees = 1850.00,
                scheduledDate = "Thu, Oct 29, 2026",
                statusText = "PAID (DEMO)"
            ),
            PayoutRecord(
                id = "payout_003",
                reference = "PAYOUT-2026-067",
                period = "Oct 15 - Oct 21, 2026",
                amountRupees = 2100.00,
                scheduledDate = "Thu, Oct 22, 2026",
                statusText = "PAID (DEMO)"
            )
        )
    }

    init {
        val nextThursdayText = calculateNextThursdayDateText()
        val today = Calendar.getInstance()
        
        // Initialize state
        _uiState.update {
            it.copy(
                selectedTimeRange = EarningsTimeRange.WEEKLY,
                nextPayoutDateText = nextThursdayText,
                chartDataPoints = sampleWeeklyPoints,
                selectedDataPoint = sampleWeeklyPoints[3],
                payoutHistory = samplePayoutHistory,
                selectedDailyDate = today
            )
        }
        refreshAllBreakdowns()
        loadEarningsData()
    }

    fun selectTimeRange(range: EarningsTimeRange) {
        val points = when (range) {
            EarningsTimeRange.DAILY -> sampleDailyPoints
            EarningsTimeRange.WEEKLY -> sampleWeeklyPoints
            EarningsTimeRange.MONTHLY -> sampleMonthlyPoints
        }

        _uiState.update {
            it.copy(
                selectedTimeRange = range,
                chartDataPoints = points,
                selectedDataPoint = points.firstOrNull { p -> p.amountRupees > 0 } ?: points.firstOrNull()
            )
        }
        updateActivePeriodSummary()
    }

    fun selectChartPoint(point: ChartDataPoint) {
        _uiState.update { it.copy(selectedDataPoint = point) }
    }

    // -------------------------------------------------------------
    // DAILY VIEW ACTIONS
    // -------------------------------------------------------------

    fun previousDay() {
        val newCal = _uiState.value.selectedDailyDate.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, -1)
        updateDailyDate(newCal)
    }

    fun nextDay() {
        val today = Calendar.getInstance()
        val current = _uiState.value.selectedDailyDate
        if (isSameDay(current, today) || current.after(today)) return

        val newCal = current.clone() as Calendar
        newCal.add(Calendar.DAY_OF_YEAR, 1)
        updateDailyDate(newCal)
    }

    fun selectDailyDate(calendar: Calendar) {
        updateDailyDate(calendar)
    }

    fun selectOrder(order: DailyOrderEarning) {
        _uiState.update { it.copy(selectedOrderInDaily = order) }
    }

    private fun updateDailyDate(calendar: Calendar) {
        val today = Calendar.getInstance()
        val isFutureOrToday = isSameDay(calendar, today) || calendar.after(today)
        val breakdown = computeDailyBreakdown(calendar)

        _uiState.update {
            it.copy(
                selectedDailyDate = calendar,
                dailyBreakdown = breakdown,
                selectedOrderInDaily = breakdown.orders.firstOrNull(),
                isDailyNextDayDisabled = isFutureOrToday
            )
        }
        if (_uiState.value.selectedTimeRange == EarningsTimeRange.DAILY) {
            updateActivePeriodSummary()
        }
    }

    // -------------------------------------------------------------
    // WEEKLY VIEW ACTIONS
    // -------------------------------------------------------------

    fun previousWeek() {
        val newOffset = _uiState.value.selectedWeekOffset - 1
        updateWeeklyOffset(newOffset)
    }

    fun nextWeek() {
        if (_uiState.value.selectedWeekOffset >= 0) return
        val newOffset = _uiState.value.selectedWeekOffset + 1
        updateWeeklyOffset(newOffset)
    }

    fun selectDayInWeekly(day: DayEarningsBreakdown) {
        _uiState.update { it.copy(selectedDayInWeekly = day) }
    }

    fun navigateToDailyForDate(dateIso: String) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateIso)
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                updateDailyDate(cal)
                selectTimeRange(EarningsTimeRange.DAILY)
            }
        } catch (_: Exception) {}
    }

    private fun updateWeeklyOffset(offset: Int) {
        val breakdown = computeWeeklyBreakdown(offset)
        _uiState.update {
            it.copy(
                selectedWeekOffset = offset,
                weeklyBreakdown = breakdown,
                selectedDayInWeekly = breakdown.days.find { d -> d.totalEarningsRupees > 0 } ?: breakdown.days.firstOrNull(),
                isWeeklyNextWeekDisabled = offset >= 0
            )
        }
        if (_uiState.value.selectedTimeRange == EarningsTimeRange.WEEKLY) {
            updateActivePeriodSummary()
        }
    }

    // -------------------------------------------------------------
    // MONTHLY VIEW ACTIONS
    // -------------------------------------------------------------

    fun previousMonth() {
        val newOffset = _uiState.value.selectedMonthOffset - 1
        updateMonthlyOffset(newOffset)
    }

    fun nextMonth() {
        if (_uiState.value.selectedMonthOffset >= 0) return
        val newOffset = _uiState.value.selectedMonthOffset + 1
        updateMonthlyOffset(newOffset)
    }

    fun selectWeekInMonthly(week: WeekEarningsBreakdown) {
        _uiState.update { it.copy(selectedWeekInMonthly = week) }
    }

    fun navigateToWeeklyForWeek(weekIndex: Int) {
        selectTimeRange(EarningsTimeRange.WEEKLY)
    }

    private fun updateMonthlyOffset(offset: Int) {
        val breakdown = computeMonthlyBreakdown(offset)
        _uiState.update {
            it.copy(
                selectedMonthOffset = offset,
                monthlyBreakdown = breakdown,
                selectedWeekInMonthly = breakdown.weeks.find { w -> w.totalEarningsRupees > 0 } ?: breakdown.weeks.firstOrNull(),
                isMonthlyNextMonthDisabled = offset >= 0
            )
        }
        if (_uiState.value.selectedTimeRange == EarningsTimeRange.MONTHLY) {
            updateActivePeriodSummary()
        }
    }

    // -------------------------------------------------------------
    // BREAKDOWN CALCULATIONS WITH STRICT MATHEMATICAL ACCURACY
    // -------------------------------------------------------------

    private fun refreshAllBreakdowns() {
        val currentDailyCal = _uiState.value.selectedDailyDate
        val daily = computeDailyBreakdown(currentDailyCal)
        val weekly = computeWeeklyBreakdown(_uiState.value.selectedWeekOffset)
        val monthly = computeMonthlyBreakdown(_uiState.value.selectedMonthOffset)

        val today = Calendar.getInstance()
        _uiState.update {
            it.copy(
                dailyBreakdown = daily,
                selectedOrderInDaily = daily.orders.firstOrNull(),
                isDailyNextDayDisabled = isSameDay(currentDailyCal, today) || currentDailyCal.after(today),

                weeklyBreakdown = weekly,
                selectedDayInWeekly = weekly.days.find { d -> d.totalEarningsRupees > 0 } ?: weekly.days.firstOrNull(),
                isWeeklyNextWeekDisabled = it.selectedWeekOffset >= 0,

                monthlyBreakdown = monthly,
                selectedWeekInMonthly = monthly.weeks.find { w -> w.totalEarningsRupees > 0 } ?: monthly.weeks.firstOrNull(),
                isMonthlyNextMonthDisabled = it.selectedMonthOffset >= 0
            )
        }
        updateActivePeriodSummary()
    }

    private fun updateActivePeriodSummary() {
        val state = _uiState.value
        when (state.selectedTimeRange) {
            EarningsTimeRange.DAILY -> {
                val daily = state.dailyBreakdown
                _uiState.update {
                    it.copy(
                        periodEarningsRupees = daily.totalEarningsRupees,
                        completedDeliveriesCount = daily.completedDeliveriesCount,
                        transactionCount = daily.orders.size,
                        averageMetricLabel = "Avg per Order",
                        averageMetricRupees = if (daily.completedDeliveriesCount > 0) daily.totalEarningsRupees / daily.completedDeliveriesCount else 0.0
                    )
                }
            }
            EarningsTimeRange.WEEKLY -> {
                val weekly = state.weeklyBreakdown
                val avgDaily = if (weekly.days.isNotEmpty()) weekly.totalEarningsRupees / weekly.days.size else 0.0
                _uiState.update {
                    it.copy(
                        periodEarningsRupees = weekly.totalEarningsRupees,
                        completedDeliveriesCount = weekly.completedDeliveriesCount,
                        transactionCount = weekly.days.sumOf { d -> d.orders.size },
                        averageMetricLabel = "Avg Daily",
                        averageMetricRupees = avgDaily
                    )
                }
            }
            EarningsTimeRange.MONTHLY -> {
                val monthly = state.monthlyBreakdown
                _uiState.update {
                    it.copy(
                        periodEarningsRupees = monthly.totalEarningsRupees,
                        completedDeliveriesCount = monthly.completedDeliveriesCount,
                        transactionCount = monthly.weeks.sumOf { w -> w.days.sumOf { d -> d.orders.size } },
                        averageMetricLabel = "Avg Weekly",
                        averageMetricRupees = monthly.averageWeeklyEarningsRupees
                    )
                }
            }
        }
    }

    private fun computeDailyBreakdown(calendar: Calendar): DayEarningsBreakdown {
        val sdfDateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val sdfFull = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
        val sdfDayOfWeek = SimpleDateFormat("EEE", Locale.US)

        val dateIso = sdfDateIso.format(calendar.time)
        val fullDateText = if (isSameDay(calendar, Calendar.getInstance())) {
            "Today, " + SimpleDateFormat("dd MMM yyyy", Locale.US).format(calendar.time)
        } else {
            sdfFull.format(calendar.time)
        }

        val orders = getOrdersForDate(dateIso, calendar)
        val total = orders.sumOf { it.earningsRupees }

        return DayEarningsBreakdown(
            dayOfWeek = sdfDayOfWeek.format(calendar.time),
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
            fullDateText = fullDateText,
            dateIso = dateIso,
            totalEarningsRupees = total,
            completedDeliveriesCount = orders.size,
            orders = orders
        )
    }

    private fun computeWeeklyBreakdown(weekOffset: Int): WeekEarningsBreakdown {
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
        
        // Go to Monday of this week
        val currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val daysFromMonday = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)

        val daysList = mutableListOf<DayEarningsBreakdown>()
        val sdfDay = SimpleDateFormat("EEE", Locale.US)
        val sdfFull = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
        val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val mondayCal = cal.clone() as Calendar
        for (i in 0..6) {
            val dayCal = cal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_MONTH, i)
            val dateIso = sdfIso.format(dayCal.time)
            val orders = getOrdersForDate(dateIso, dayCal)
            val dayTotal = orders.sumOf { it.earningsRupees }

            daysList.add(
                DayEarningsBreakdown(
                    dayOfWeek = sdfDay.format(dayCal.time),
                    dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH),
                    fullDateText = sdfFull.format(dayCal.time),
                    dateIso = dateIso,
                    totalEarningsRupees = dayTotal,
                    completedDeliveriesCount = orders.size,
                    orders = orders
                )
            )
        }

        val sundayCal = cal.clone() as Calendar
        sundayCal.add(Calendar.DAY_OF_MONTH, 6)

        val monText = SimpleDateFormat("dd MMM", Locale.US).format(mondayCal.time)
        val sunText = SimpleDateFormat("dd MMM yyyy", Locale.US).format(sundayCal.time)
        val dateRangeText = "Mon, $monText - Sun, $sunText"

        val total = daysList.sumOf { it.totalEarningsRupees }
        val deliveries = daysList.sumOf { it.completedDeliveriesCount }

        return WeekEarningsBreakdown(
            weekNumber = 1,
            weekLabel = "Current Week",
            dateRangeText = dateRangeText,
            totalEarningsRupees = total,
            completedDeliveriesCount = deliveries,
            days = daysList
        )
    }

    private fun computeMonthlyBreakdown(monthOffset: Int): MonthEarningsBreakdown {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val year = cal.get(Calendar.YEAR)
        val monthZeroBased = cal.get(Calendar.MONTH)
        val monthName = SimpleDateFormat("MMMM", Locale.US).format(cal.time)
        val monthShort = SimpleDateFormat("MMM", Locale.US).format(cal.time)
        val monthYearText = "$monthName $year"

        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val weeks = mutableListOf<WeekEarningsBreakdown>()

        var currentDay = 1
        var weekNum = 1

        val sdfDay = SimpleDateFormat("EEE", Locale.US)
        val sdfFull = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.US)
        val sdfIso = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        while (currentDay <= maxDay) {
            val weekStartDay = currentDay
            val weekDaysList = mutableListOf<DayEarningsBreakdown>()

            while (currentDay <= maxDay) {
                val dayCal = Calendar.getInstance().apply {
                    clear()
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, monthZeroBased)
                    set(Calendar.DAY_OF_MONTH, currentDay)
                }
                val dayOfWeekNum = dayCal.get(Calendar.DAY_OF_WEEK)
                val dateIso = sdfIso.format(dayCal.time)

                val orders = getOrdersForDate(dateIso, dayCal)
                val dayTotal = orders.sumOf { it.earningsRupees }

                weekDaysList.add(
                    DayEarningsBreakdown(
                        dayOfWeek = sdfDay.format(dayCal.time),
                        dayOfMonth = currentDay,
                        fullDateText = sdfFull.format(dayCal.time),
                        dateIso = dateIso,
                        totalEarningsRupees = dayTotal,
                        completedDeliveriesCount = orders.size,
                        orders = orders
                    )
                )

                currentDay++
                // Monday-start week: week ends on Sunday
                if (dayOfWeekNum == Calendar.SUNDAY) {
                    break
                }
            }

            val weekEndDay = currentDay - 1
            val dateRangeText = if (weekStartDay == weekEndDay) {
                "Week $weekNum: $weekStartDay $monthShort"
            } else {
                "Week $weekNum: $weekStartDay $monthShort – $weekEndDay $monthShort"
            }

            val weekTotal = weekDaysList.sumOf { it.totalEarningsRupees }
            val weekDeliveries = weekDaysList.sumOf { it.completedDeliveriesCount }

            weeks.add(
                WeekEarningsBreakdown(
                    weekNumber = weekNum,
                    weekLabel = "W$weekNum",
                    dateRangeText = dateRangeText,
                    totalEarningsRupees = weekTotal,
                    completedDeliveriesCount = weekDeliveries,
                    days = weekDaysList
                )
            )
            weekNum++
        }

        val monthlyTotal = weeks.sumOf { it.totalEarningsRupees }
        val monthlyDeliveries = weeks.sumOf { it.completedDeliveriesCount }
        val avgWeekly = if (weeks.isNotEmpty()) monthlyTotal / weeks.size else 0.0

        return MonthEarningsBreakdown(
            monthName = monthName,
            year = year,
            monthYearText = monthYearText,
            totalEarningsRupees = monthlyTotal,
            completedDeliveriesCount = monthlyDeliveries,
            averageWeeklyEarningsRupees = avgWeekly,
            weeks = weeks
        )
    }

    /**
     * Deterministic, mathematically stable completed delivery orders generator:
     * - Returns empty list for future dates.
     * - Returns realistic, reproducible order items with exact amounts.
     * - Every order belongs to a date; day sum = sum(order amounts).
     */
    fun getOrdersForDate(dateIso: String, calendar: Calendar): List<DailyOrderEarning> {
        val today = Calendar.getInstance()
        if (calendar.after(today) && !isSameDay(calendar, today)) {
            return emptyList()
        }

        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Monday-first calendar grouping:
        // For November 2026, adhere to the reference data:
        // W1 (1 Nov): 3 orders totaling ₹1,010.00
        // W2 (2-8 Nov): Completed orders totaling ₹2,690.00
        // W3, W4, W5, W6: Upcoming / rest weeks (₹0.00)
        // Month Total = ₹3,700.00
        if (dateIso.startsWith("2026-11")) {
            return when (dateIso) {
                "2026-11-01" -> listOf(
                    DailyOrderEarning(
                        orderId = "ORD-91021",
                        orderNumber = "PAY-9102",
                        completedTimeText = "09:30 AM",
                        dateIso = dateIso,
                        earningsRupees = 210.0,
                        pickupLocation = "Sony Center, Koramangala",
                        deliveryLocation = "Indiranagar 100ft Rd"
                    ),
                    DailyOrderEarning(
                        orderId = "ORD-91022",
                        orderNumber = "PAY-9103",
                        completedTimeText = "01:15 PM",
                        dateIso = dateIso,
                        earningsRupees = 380.0,
                        pickupLocation = "Camera & Rig Hub, Jayanagar",
                        deliveryLocation = "JP Nagar Phase 3"
                    ),
                    DailyOrderEarning(
                        orderId = "ORD-91023",
                        orderNumber = "PAY-9104",
                        completedTimeText = "05:50 PM",
                        dateIso = dateIso,
                        earningsRupees = 420.0,
                        pickupLocation = "Live Gear Express, Domlur",
                        deliveryLocation = "MG Road Brigade Towers"
                    )
                )
                "2026-11-02" -> listOf(
                    DailyOrderEarning(
                        orderId = "ORD-9201",
                        orderNumber = "PAY-9201",
                        completedTimeText = "10:30 AM",
                        dateIso = dateIso,
                        earningsRupees = 240.0,
                        pickupLocation = "AudioPro Hub, Koramangala",
                        deliveryLocation = "Indiranagar 100ft Rd"
                    ),
                    DailyOrderEarning(
                        orderId = "ORD-9202",
                        orderNumber = "PAY-9202",
                        completedTimeText = "03:45 PM",
                        dateIso = dateIso,
                        earningsRupees = 320.0,
                        pickupLocation = "Stage Sound Warehouse, HSR",
                        deliveryLocation = "Bellandur Tech Village"
                    )
                )
                "2026-11-03" -> listOf(
                    DailyOrderEarning(
                        orderId = "ORD-9301",
                        orderNumber = "PAY-9301",
                        completedTimeText = "11:15 AM",
                        dateIso = dateIso,
                        earningsRupees = 310.0,
                        pickupLocation = "Rental Gear Center, Koramangala",
                        deliveryLocation = "Sony World Signal"
                    ),
                    DailyOrderEarning(
                        orderId = "ORD-9302",
                        orderNumber = "PAY-9302",
                        completedTimeText = "04:30 PM",
                        dateIso = dateIso,
                        earningsRupees = 340.0,
                        pickupLocation = "Audio Trunk Return, Indiranagar",
                        deliveryLocation = "Whitefield Main Rd"
                    )
                )
                "2026-11-05" -> listOf(
                    DailyOrderEarning(
                        orderId = "ORD-9501",
                        orderNumber = "PAY-9501",
                        completedTimeText = "10:00 AM",
                        dateIso = dateIso,
                        earningsRupees = 360.0,
                        pickupLocation = "Lighting Rig Stash, Ulsoor",
                        deliveryLocation = "Frazer Town"
                    ),
                    DailyOrderEarning(
                        orderId = "ORD-9502",
                        orderNumber = "PAY-9502",
                        completedTimeText = "02:45 PM",
                        dateIso = dateIso,
                        earningsRupees = 420.0,
                        pickupLocation = "Studio Handover Depot, HSR",
                        deliveryLocation = "Sarjapur Road"
                    )
                )
                "2026-11-06" -> listOf(
                    DailyOrderEarning(
                        orderId = "ORD-9601",
                        orderNumber = "PAY-9601",
                        completedTimeText = "11:20 AM",
                        dateIso = dateIso,
                        earningsRupees = 320.0,
                        pickupLocation = "Audio Equipment Depot, Richmond Town",
                        deliveryLocation = "Cunningham Road"
                    ),
                    DailyOrderEarning(
                        orderId = "ORD-9602",
                        orderNumber = "PAY-9602",
                        completedTimeText = "04:10 PM",
                        dateIso = dateIso,
                        earningsRupees = 380.0,
                        pickupLocation = "AudioPro Accessories, Shivaji Nagar",
                        deliveryLocation = "Vasanth Nagar"
                    )
                )
                else -> emptyList()
            }
        }

        // Return deterministic orders based on date
        return when (dayOfMonth % 5) {
            0 -> listOf(
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}1",
                    orderNumber = "PAY-9102",
                    completedTimeText = "10:15 AM",
                    dateIso = dateIso,
                    earningsRupees = 240.0,
                    pickupLocation = "AudioPro Hub, Koramangala",
                    deliveryLocation = "Indiranagar 100ft Rd"
                ),
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}2",
                    orderNumber = "PAY-9145",
                    completedTimeText = "03:40 PM",
                    dateIso = dateIso,
                    earningsRupees = 320.0,
                    pickupLocation = "Stage Sound Warehouse, HSR",
                    deliveryLocation = "Bellandur Tech Village"
                )
            )
            1 -> listOf(
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}1",
                    orderNumber = "PAY-8210",
                    completedTimeText = "09:30 AM",
                    dateIso = dateIso,
                    earningsRupees = 210.0,
                    pickupLocation = "Mic Station, BTM 2nd Stage",
                    deliveryLocation = "Koramangala 4th Block"
                ),
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}2",
                    orderNumber = "PAY-8235",
                    completedTimeText = "01:15 PM",
                    dateIso = dateIso,
                    earningsRupees = 380.0,
                    pickupLocation = "Camera & Rig Hub, Jayanagar",
                    deliveryLocation = "JP Nagar Phase 3"
                ),
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}3",
                    orderNumber = "PAY-8288",
                    completedTimeText = "05:50 PM",
                    dateIso = dateIso,
                    earningsRupees = 420.0,
                    pickupLocation = "Live Gear Express, Domlur",
                    deliveryLocation = "MG Road Brigade Towers"
                )
            )
            2 -> listOf(
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}1",
                    orderNumber = "PAY-7012",
                    completedTimeText = "11:00 AM",
                    dateIso = dateIso,
                    earningsRupees = 350.0,
                    pickupLocation = "Studio Handover Depot, HSR",
                    deliveryLocation = "Sarjapur Road"
                )
            )
            3 -> listOf(
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}1",
                    orderNumber = "PAY-6421",
                    completedTimeText = "10:45 AM",
                    dateIso = dateIso,
                    earningsRupees = 290.0,
                    pickupLocation = "Rental Gear Center, Koramangala",
                    deliveryLocation = "Sony World Signal"
                ),
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}2",
                    orderNumber = "PAY-6477",
                    completedTimeText = "04:20 PM",
                    dateIso = dateIso,
                    earningsRupees = 480.0,
                    pickupLocation = "Audio Trunk Return, Indiranagar",
                    deliveryLocation = "Whitefield Main Rd"
                )
            )
            else -> listOf(
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}1",
                    orderNumber = "PAY-5110",
                    completedTimeText = "11:20 AM",
                    dateIso = dateIso,
                    earningsRupees = 250.0,
                    pickupLocation = "Audio Equipment Depot, Richmond Town",
                    deliveryLocation = "Cunningham Road"
                ),
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}2",
                    orderNumber = "PAY-5166",
                    completedTimeText = "02:45 PM",
                    dateIso = dateIso,
                    earningsRupees = 310.0,
                    pickupLocation = "Lighting Rig Stash, Ulsoor",
                    deliveryLocation = "Frazer Town"
                ),
                DailyOrderEarning(
                    orderId = "ORD-${dateIso.replace("-", "").takeLast(4)}3",
                    orderNumber = "PAY-5198",
                    completedTimeText = "06:30 PM",
                    dateIso = dateIso,
                    earningsRupees = 190.0,
                    pickupLocation = "AudioPro Accessories, Shivaji Nagar",
                    deliveryLocation = "Vasanth Nagar"
                )
            )
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun loadEarningsData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = earningsRepository.getEarningsSummary()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            earningsSummary = result.data,
                            isBackendConnected = true
                        )
                    }
                }
                else -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isBackendConnected = false,
                            earningsSummary = EarningsSummary(
                                totalEarnings = 3850.0,
                                todayEarnings = 480.0,
                                weeklyEarnings = 1850.0,
                                monthlyEarnings = 7020.0,
                                completedDeliveriesCount = 5,
                                nextPayoutDate = _uiState.value.nextPayoutDateText,
                                payoutStatus = "SCHEDULED (DEMO)"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun calculateNextThursdayDateText(): String {
        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        var daysUntilThursday = Calendar.THURSDAY - dayOfWeek
        if (daysUntilThursday <= 0) {
            daysUntilThursday += 7
        }
        cal.add(Calendar.DAY_OF_YEAR, daysUntilThursday)

        val formatter = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)
        return formatter.format(cal.time)
    }

    class Factory(
        private val earningsRepository: EarningsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EarningsViewModel(earningsRepository) as T
        }
    }
}

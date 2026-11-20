package com.payent.delivery

import com.payent.delivery.core.network.NetworkResult
import com.payent.delivery.domain.model.EarningsSummary
import com.payent.delivery.domain.model.EarningsTimeRange
import com.payent.delivery.domain.repository.EarningsRepository
import com.payent.delivery.feature.earnings.EarningsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EarningsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeEarningsSummary = EarningsSummary(
        totalEarnings = 3850.0,
        todayEarnings = 480.0,
        weeklyEarnings = 1850.0,
        monthlyEarnings = 7020.0,
        completedDeliveriesCount = 5,
        nextPayoutDate = "Thursday, Mar 6, 2025",
        payoutStatus = "SCHEDULED (DEMO)"
    )

    private val fakeEarningsRepository = object : EarningsRepository {
        override suspend fun getEarningsSummary(): NetworkResult<EarningsSummary> =
            NetworkResult.Success(fakeEarningsSummary)
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun timeRangeSwitching_updatesChartPointsAndPeriodSummary() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)

        // Default: WEEKLY
        assertEquals(EarningsTimeRange.WEEKLY, viewModel.uiState.value.selectedTimeRange)
        assertEquals(7, viewModel.uiState.value.chartDataPoints.size)

        // Switch to DAILY
        viewModel.selectTimeRange(EarningsTimeRange.DAILY)
        assertEquals(EarningsTimeRange.DAILY, viewModel.uiState.value.selectedTimeRange)
        assertEquals(5, viewModel.uiState.value.chartDataPoints.size)

        // Switch to MONTHLY
        viewModel.selectTimeRange(EarningsTimeRange.MONTHLY)
        assertEquals(EarningsTimeRange.MONTHLY, viewModel.uiState.value.selectedTimeRange)
        assertEquals(4, viewModel.uiState.value.chartDataPoints.size)
    }

    @Test
    fun selectChartPoint_updatesSelectedDataPoint() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        val point = viewModel.uiState.value.chartDataPoints.last()

        viewModel.selectChartPoint(point)

        assertEquals(point, viewModel.uiState.value.selectedDataPoint)
    }

    @Test
    fun ThursdayPayoutDate_isCalculatedAndFormatted() = runTest {
        val viewModel = EarningsViewModel(fakeEarningsRepository)
        val state = viewModel.uiState.value

        assertNotNull(state.nextPayoutDateText)
        assertTrue(state.nextPayoutDateText.contains("Thursday"))
        assertTrue(state.payoutHistory.isNotEmpty())
    }
}

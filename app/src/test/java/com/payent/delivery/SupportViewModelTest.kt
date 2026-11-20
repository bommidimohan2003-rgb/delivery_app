package com.payent.delivery

import com.payent.delivery.feature.support.SupportCategory
import com.payent.delivery.feature.support.SupportViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportViewModelTest {

    @Test
    fun faqs_loadedAndExpandable() {
        val viewModel = SupportViewModel()
        val firstFaq = viewModel.uiState.value.faqs.first()

        assertFalse(firstFaq.isExpanded)

        viewModel.toggleFaqExpanded(firstFaq.id)

        val updatedFaq = viewModel.uiState.value.faqs.first { it.id == firstFaq.id }
        assertTrue(updatedFaq.isExpanded)
    }

    @Test
    fun submitDemoTicket_validatesInput() {
        val viewModel = SupportViewModel()
        viewModel.onSubjectChanged("")
        viewModel.onDescriptionChanged("Too short")

        val result = viewModel.submitDemoTicket()

        assertFalse(result)
        assertNotNull(viewModel.uiState.value.subjectError)
        assertNotNull(viewModel.uiState.value.descriptionError)
    }

    @Test
    fun submitDemoTicket_addsTicketLocally_whenValid() {
        val viewModel = SupportViewModel()
        viewModel.onSubjectChanged("Order Assignment Query")
        viewModel.selectTicketCategory(SupportCategory.ORDERS)
        viewModel.onDescriptionChanged("How do I request order reassignment in demo mode?")

        val initialCount = viewModel.uiState.value.supportTickets.size
        val result = viewModel.submitDemoTicket()

        assertTrue(result)
        assertEquals(initialCount + 1, viewModel.uiState.value.supportTickets.size)
        assertNotNull(viewModel.uiState.value.submitSuccessNotice)
    }
}

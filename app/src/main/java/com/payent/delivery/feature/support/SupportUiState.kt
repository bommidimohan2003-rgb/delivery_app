package com.payent.delivery.feature.support

data class SupportUiState(
    val selectedTab: SupportTab = SupportTab.TICKETS,
    val searchQuery: String = "",
    val selectedCategoryFilter: SupportCategory = SupportCategory.ALL,
    val isScopedToCategory: Boolean = false,
    val pageTitle: String = "Help & Support",
    val pageSubtitle: String? = null,
    val scopePrompt: String? = null,
    val faqs: List<FaqItem> = emptyList(),

    // Contact Support Form Fields
    val isCreateTicketDialogOpen: Boolean = false,
    val ticketSubject: String = "",
    val ticketCategory: SupportCategory = SupportCategory.ORDERS,
    val ticketDescription: String = "",
    val subjectError: String? = null,
    val descriptionError: String? = null,
    val submitSuccessNotice: String? = null,

    // Ticket History
    val supportTickets: List<SupportTicket> = emptyList(),
    val selectedTicketForDetails: SupportTicket? = null,

    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

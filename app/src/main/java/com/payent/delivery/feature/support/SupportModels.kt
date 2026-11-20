package com.payent.delivery.feature.support

enum class SupportTab {
    TICKETS,
    FAQ
}

enum class TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED
}

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: SupportCategory,
    val isExpanded: Boolean = false
)

data class SupportTicket(
    val id: String,
    val reference: String,
    val subject: String,
    val category: SupportCategory,
    val description: String,
    val createdDateText: String,
    val statusText: String,
    val status: TicketStatus = TicketStatus.OPEN
)

enum class SupportCategory {
    ALL,
    ACCOUNT,
    KYC,
    ORDERS,
    PICKUP,
    DELIVERY,
    EARNINGS_PAYMENTS
}

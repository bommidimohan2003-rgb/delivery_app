package com.payent.delivery.feature.support

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SupportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    companion object {
        val sampleFaqs = listOf(
            // ORDERS Help & Support
            FaqItem(
                id = "faq_ord_001",
                question = "How do I accept a rental delivery order?",
                answer = "Browse available orders in the Orders tab or Home dashboard. Tap 'View & Accept', review the pickup vendor location and destination distance, then tap 'Confirm Accept' to take custody of the delivery assignment.",
                category = SupportCategory.ORDERS
            ),
            FaqItem(
                id = "faq_ord_002",
                question = "Why don't I see any available orders in the list?",
                answer = "Make sure your availability switch on the Home dashboard is toggled to 'Online' and GPS location permissions are granted. New rental delivery requests appear as nearby vendors dispatch rental orders.",
                category = SupportCategory.ORDERS
            ),
            FaqItem(
                id = "faq_ord_003",
                question = "Can I cancel an order after accepting it?",
                answer = "If an emergency arises before reaching the vendor location, you can request order cancellation or re-assignment from the order details screen without affecting your fulfillment rating.",
                category = SupportCategory.ORDERS
            ),
            FaqItem(
                id = "faq_ord_004",
                question = "Where do I find my active in-progress deliveries?",
                answer = "Switch to the 'Active Orders' tab from the top header selector or from the Home screen shortcut to view accepted deliveries, start turn-by-turn navigation, and proceed with pickup.",
                category = SupportCategory.ORDERS
            ),
            FaqItem(
                id = "faq_ord_005",
                question = "How are pickup and delivery route distances calculated?",
                answer = "Distances are calculated in real-time along road networks from your current partner location to the rental vendor, and from the vendor to the customer's delivery destination.",
                category = SupportCategory.ORDERS
            ),
            FaqItem(
                id = "faq_ord_006",
                question = "What should I do if the vendor store is closed or unavailable?",
                answer = "Tap 'Report Issue' or 'Help' on the pickup screen to notify partner dispatch. Dispatch will confirm the closure and cancel the assignment without penalty.",
                category = SupportCategory.ORDERS
            ),

            // EARNINGS & PAYMENTS Help
            FaqItem(
                id = "faq_earn_001",
                question = "Where can I view my earnings breakdown?",
                answer = "Open the 'Earnings' tab from the bottom navigation. You can toggle between Daily, Weekly, and Monthly views to see completed delivery trips, surge bonuses, and base pay.",
                category = SupportCategory.EARNINGS_PAYMENTS
            ),
            FaqItem(
                id = "faq_earn_002",
                question = "When are Thursday payouts deposited into my bank account?",
                answer = "Partner earnings accumulated throughout the payout cycle are automatically processed and credited directly into your registered bank account every Thursday by 6:00 PM.",
                category = SupportCategory.EARNINGS_PAYMENTS
            ),
            FaqItem(
                id = "faq_earn_003",
                question = "How is the delivery fee calculated per order?",
                answer = "Your payout per delivery includes a base order fee, distance allowance based on kilometer traveled, dynamic peak-demand incentives, and 100% of customer tips.",
                category = SupportCategory.EARNINGS_PAYMENTS
            ),
            FaqItem(
                id = "faq_earn_004",
                question = "What should I do if my weekly payout is delayed?",
                answer = "Ensure your bank account number and IFSC code are verified under Profile -> KYC Documents. Bank clearing may take up to 24 hours depending on national holidays and banking networks.",
                category = SupportCategory.EARNINGS_PAYMENTS
            ),
            FaqItem(
                id = "faq_earn_005",
                question = "How do incentives and surge bonuses work?",
                answer = "Surge multipliers are applied automatically during peak rental delivery hours and adverse weather conditions. Bonus amounts are shown on each order card before acceptance.",
                category = SupportCategory.EARNINGS_PAYMENTS
            ),

            // PROFILE & ACCOUNT Help
            FaqItem(
                id = "faq_acc_001",
                question = "How do I update my profile or phone number?",
                answer = "Go to Profile -> Edit Profile to update your registered display name, contact phone number, email address, or emergency contact details.",
                category = SupportCategory.ACCOUNT
            ),
            FaqItem(
                id = "faq_acc_002",
                question = "How do I go online or offline?",
                answer = "Use the availability toggle switch card at the top of the Home dashboard to change your status. Going offline stops new order notifications while allowing you to complete in-progress tasks.",
                category = SupportCategory.ACCOUNT
            ),
            FaqItem(
                id = "faq_acc_003",
                question = "How do I update my registered delivery vehicle?",
                answer = "Open Profile -> Edit Profile and select your current vehicle type (e.g., Electric Scooter, Motorcycle, Bicycle, or Light Commercial Van).",
                category = SupportCategory.ACCOUNT
            ),
            FaqItem(
                id = "faq_acc_004",
                question = "How is my partner performance rating determined?",
                answer = "Your partner score reflects on-time pickups, successful customer handovers, and adherence to verification protocols (PIN and OTP entry).",
                category = SupportCategory.ACCOUNT
            ),

            // KYC VERIFICATION Help
            FaqItem(
                id = "faq_kyc_001",
                question = "Which documents are required for partner KYC?",
                answer = "You must submit a valid Driving Licence (or government photo ID for bicycles), an Aadhaar card or National ID, and Bank Account Proof / passbook for direct deposits.",
                category = SupportCategory.KYC
            ),
            FaqItem(
                id = "faq_kyc_002",
                question = "How long does document verification take?",
                answer = "Our partner onboarding desk reviews submitted documents within 2 to 4 business hours. You will receive an in-app notification once verified.",
                category = SupportCategory.KYC
            ),
            FaqItem(
                id = "faq_kyc_003",
                question = "Can I deliver orders while KYC is under review?",
                answer = "You can explore all app features and review the interface, but accepting live rental orders requires an approved KYC verification status.",
                category = SupportCategory.KYC
            ),

            // PICKUP & VENDOR HANDOVER Help
            FaqItem(
                id = "faq_pick_001",
                question = "How does pickup PIN verification work?",
                answer = "The vendor enters their secure secret PIN in their Payent Vendor App. Once validated, a 6-digit confirmation code is generated to securely release the rental package.",
                category = SupportCategory.PICKUP
            ),
            FaqItem(
                id = "faq_pick_002",
                question = "What should I inspect before taking custody of the product?",
                answer = "Check the rental item packaging, inspect for visible cosmetic damage, and verify power cables, accessories, and manuals are included before confirming pickup.",
                category = SupportCategory.PICKUP
            ),

            // DELIVERY & CUSTOMER HANDOVER Help
            FaqItem(
                id = "faq_del_001",
                question = "How does customer delivery OTP verification work?",
                answer = "Upon arriving at the renter's address, tap 'Send OTP'. The renter receives an SMS challenge on their phone and provides the code to unlock handover verification.",
                category = SupportCategory.DELIVERY
            ),
            FaqItem(
                id = "faq_del_002",
                question = "What should I do if the renter reports a problem during inspection?",
                answer = "Tap 'Report a Problem', select the defect category (damaged, missing accessories, or wrong model), and submit. The return workflow will automatically activate.",
                category = SupportCategory.DELIVERY
            ),
            FaqItem(
                id = "faq_del_003",
                question = "What if the customer is unreachable at the delivery destination?",
                answer = "Call the customer using the in-app phone action. If unreachable after 10 minutes, submit a delivery hold ticket to reschedule with dispatch.",
                category = SupportCategory.DELIVERY
            )
        )

        val sampleTickets = listOf(
            SupportTicket(
                id = "ticket_1001",
                reference = "#TKT1001",
                subject = "Payout not received",
                category = SupportCategory.EARNINGS_PAYMENTS,
                description = "My payout for last week is not credited to my bank account.",
                createdDateText = "12 Sep 2026",
                statusText = "Open",
                status = TicketStatus.OPEN
            ),
            SupportTicket(
                id = "ticket_10987",
                reference = "#TKT10987",
                subject = "Document verification issue",
                category = SupportCategory.KYC,
                description = "My driving licence is not getting verified.",
                createdDateText = "10 Sep 2026",
                statusText = "In Progress",
                status = TicketStatus.IN_PROGRESS
            ),
            SupportTicket(
                id = "ticket_0954",
                reference = "#TKT0954",
                subject = "App navigation problem",
                category = SupportCategory.ORDERS,
                description = "Facing issue with map navigation.",
                createdDateText = "5 Sep 2026",
                statusText = "Resolved",
                status = TicketStatus.RESOLVED
            )
        )
    }

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update {
            it.copy(
                faqs = sampleFaqs,
                supportTickets = sampleTickets
            )
        }
    }

    fun selectTab(tab: SupportTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openCreateTicketDialog() {
        _uiState.update {
            it.copy(
                isCreateTicketDialogOpen = true,
                subjectError = null,
                descriptionError = null
            )
        }
    }

    fun dismissCreateTicketDialog() {
        _uiState.update { it.copy(isCreateTicketDialogOpen = false) }
    }

    /**
     * Scopes help strictly to the requested page purpose.
     */
    fun setPageScope(category: SupportCategory) {
        val (title, subtitle, prompt) = when (category) {
            SupportCategory.ORDERS -> Triple(
                "Orders Help & Support",
                "Assistance with finding, accepting, and managing rental delivery assignments",
                "Describe your order issue or question..."
            )
            SupportCategory.EARNINGS_PAYMENTS -> Triple(
                "Earnings Help & Support",
                "Assistance with your weekly payouts, rates, and Thursday bank deposits",
                "Describe your earnings or payout inquiry..."
            )
            SupportCategory.ACCOUNT -> Triple(
                "Profile & Account Help",
                "Assistance with your partner profile, vehicle model, and account status",
                "Describe your account or vehicle inquiry..."
            )
            SupportCategory.KYC -> Triple(
                "KYC Verification Help",
                "Guidance for document upload and partner verification approval",
                "Describe your KYC document question..."
            )
            SupportCategory.PICKUP -> Triple(
                "Pickup Verification Help",
                "Assistance with vendor store handover, item inspection, and secret PIN verification",
                "Describe your vendor pickup issue..."
            )
            SupportCategory.DELIVERY -> Triple(
                "Delivery Handover Help",
                "Guidance for customer arrival, renter OTP verification, and product inspection",
                "Describe your customer delivery issue..."
            )
            SupportCategory.ALL -> Triple(
                "Help & Support",
                "Browse all partner frequently asked questions or submit an inquiry",
                "Describe your inquiry..."
            )
        }
        _uiState.update {
            it.copy(
                selectedCategoryFilter = category,
                ticketCategory = if (category == SupportCategory.ALL) SupportCategory.ORDERS else category,
                isScopedToCategory = category != SupportCategory.ALL,
                pageTitle = title,
                pageSubtitle = subtitle,
                scopePrompt = prompt,
                searchQuery = "",
                submitSuccessNotice = null
            )
        }
    }

    /**
     * Resets page scope to allow viewing all categories.
     */
    fun clearPageScope() {
        _uiState.update {
            it.copy(
                selectedCategoryFilter = SupportCategory.ALL,
                isScopedToCategory = false,
                pageTitle = "Help & Support",
                pageSubtitle = "Browse all partner frequently asked questions or submit an inquiry",
                scopePrompt = "Describe your inquiry...",
                searchQuery = ""
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun selectCategoryFilter(category: SupportCategory) {
        _uiState.update { it.copy(selectedCategoryFilter = category, ticketCategory = if (category != SupportCategory.ALL) category else it.ticketCategory) }
    }

    fun toggleFaqExpanded(id: String) {
        _uiState.update { state ->
            val updatedFaqs = state.faqs.map {
                if (it.id == id) it.copy(isExpanded = !it.isExpanded) else it
            }
            state.copy(faqs = updatedFaqs)
        }
    }

    fun toggleFaqExpansion(id: String) = toggleFaqExpanded(id)

    fun onSubjectChanged(subject: String) {
        _uiState.update { it.copy(ticketSubject = subject, subjectError = null, submitSuccessNotice = null) }
    }

    fun selectTicketCategory(category: SupportCategory) {
        _uiState.update { it.copy(ticketCategory = category, submitSuccessNotice = null) }
    }

    fun onDescriptionChanged(desc: String) {
        _uiState.update { it.copy(ticketDescription = desc, descriptionError = null, submitSuccessNotice = null) }
    }

    fun submitDemoTicket(): Boolean {
        val state = _uiState.value
        val subject = state.ticketSubject.trim()
        val description = state.ticketDescription.trim()

        var hasError = false
        var subErr: String? = null
        var descErr: String? = null

        if (subject.isBlank()) {
            subErr = "Ticket subject is required"
            hasError = true
        }

        if (description.length < 10) {
            descErr = "Description must be at least 10 characters"
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    subjectError = subErr,
                    descriptionError = descErr
                )
            }
            return false
        }

        val newTicket = SupportTicket(
            id = "ticket_${System.currentTimeMillis()}",
            reference = "#TKT${(1000..9999).random()}",
            subject = subject,
            category = state.ticketCategory,
            description = description,
            createdDateText = "Today",
            statusText = "Open",
            status = TicketStatus.OPEN
        )

        _uiState.update {
            it.copy(
                supportTickets = listOf(newTicket) + it.supportTickets,
                isCreateTicketDialogOpen = false,
                ticketSubject = "",
                ticketDescription = "",
                submitSuccessNotice = "Support ticket ${newTicket.reference} submitted successfully."
            )
        }
        return true
    }

    fun selectTicketForDetails(ticket: SupportTicket) {
        _uiState.update { it.copy(selectedTicketForDetails = ticket) }
    }

    fun clearNoticeMessage() {
        _uiState.update { it.copy(submitSuccessNotice = null, errorMessage = null) }
    }
}

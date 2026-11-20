package com.payent.delivery.feature.support

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Editorial Redesign of Help & Support (Screen 5) matching the reference design.
 *
 * Structure:
 * 1. Top Bar: Back arrow on left + Centered "Help & Support" title
 * 2. Two Tabs: "Support Tickets" (Selected: Black) and "FAQ" (Inactive: Light grey)
 * 3. Support Tickets Tab:
 *    - Full-width black button: "+ Create New Ticket"
 *    - Ticket cards (#TKT1001, #TKT10987, #TKT0954) with date, status badges (Open, In Progress, Resolved), bold subjects, description, and chevron
 *    - Create Ticket modal dialog with category, subject, description validation
 * 4. FAQ Tab: Search input + expandable FAQ question/answer accordions
 */
@Composable
fun HelpSupportScreen(
    viewModel: SupportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTicketHistory: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    val isDark = MaterialTheme.colorScheme.background == DarkBackground ||
            MaterialTheme.colorScheme.surface == DarkSurface

    // Editorial Palette
    val screenBg = if (isDark) DarkBackground else Color.White
    val cardBg = if (isDark) Color(0xFF141414) else Color.White
    val cardBorder = if (isDark) Color(0xFF282828) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF111111)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)
    val tabInactiveBg = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)

    // Create New Ticket Dialog
    if (uiState.isCreateTicketDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCreateTicketDialog() },
            title = {
                Text(
                    text = "Create Support Ticket",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Subject",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.ticketSubject,
                        onValueChange = { viewModel.onSubjectChanged(it) },
                        placeholder = { Text("e.g. Payout not received", fontSize = 13.sp) },
                        isError = uiState.subjectError != null,
                        supportingText = uiState.subjectError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = uiState.ticketDescription,
                        onValueChange = { viewModel.onDescriptionChanged(it) },
                        placeholder = { Text("Explain your issue in detail...", fontSize = 13.sp) },
                        isError = uiState.descriptionError != null,
                        supportingText = uiState.descriptionError?.let { { Text(it, color = Color(0xFFEF4444)) } },
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                        .clickable { viewModel.submitDemoTicket() }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Submit Ticket",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissCreateTicketDialog() }) {
                    Text(text = "Cancel", color = textSecondary)
                }
            },
            containerColor = cardBg
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Section 1: Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "Help & Support",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section 2: Two Clean Pill Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(tabInactiveBg)
                    .padding(4.dp)
            ) {
                // Tab 1: Support Tickets
                val isTicketsSelected = uiState.selectedTab == SupportTab.TICKETS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(
                            if (isTicketsSelected) {
                                if (isDark) Color.White else Color.Black
                            } else Color.Transparent
                        )
                        .clickable { viewModel.selectTab(SupportTab.TICKETS) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Support Tickets",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isTicketsSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isTicketsSelected) {
                            if (isDark) Color.Black else Color.White
                        } else textSecondary
                    )
                }

                // Tab 2: FAQ
                val isFaqSelected = uiState.selectedTab == SupportTab.FAQ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(
                            if (isFaqSelected) {
                                if (isDark) Color.White else Color.Black
                            } else Color.Transparent
                        )
                        .clickable { viewModel.selectTab(SupportTab.FAQ) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "FAQ",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = if (isFaqSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isFaqSelected) {
                            if (isDark) Color.Black else Color.White
                        } else textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section 3: Tab Contents
            if (uiState.selectedTab == SupportTab.TICKETS) {
                // Button: + Create New Ticket
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .clickable { viewModel.openCreateTicketDialog() },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Create New Ticket",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Support Tickets List
                if (uiState.supportTickets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active support tickets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary
                        )
                    }
                } else {
                    uiState.supportTickets.forEach { ticket ->
                        SupportTicketCard(
                            ticket = ticket,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            isDark = isDark,
                            onClick = {
                                viewModel.selectTicketForDetails(ticket)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            } else {
                // FAQ Tab Content
                val filteredFaqs = uiState.faqs.filter { faq ->
                    uiState.searchQuery.isBlank() ||
                            faq.question.contains(uiState.searchQuery, ignoreCase = true) ||
                            faq.answer.contains(uiState.searchQuery, ignoreCase = true)
                }

                // Search Box
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search help topics...", fontSize = 13.5.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = textPrimary,
                        unfocusedBorderColor = cardBorder
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                filteredFaqs.forEach { faq ->
                    EditorialFaqCard(
                        faq = faq,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onToggle = { viewModel.toggleFaqExpanded(faq.id) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Bottom padding to avoid overlapping the floating bottom navigation dock
            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

/**
 * Editorial Support Ticket Card matching Phone 5 in reference image
 */
@Composable
private fun SupportTicketCard(
    ticket: SupportTicket,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val (statusBg, statusFg) = when (ticket.status) {
        TicketStatus.OPEN -> Pair(
            if (isDark) Color(0xFF132E1E) else Color(0xFFDCFCE7),
            Color(0xFF16A34A)
        )
        TicketStatus.IN_PROGRESS -> Pair(
            if (isDark) Color(0xFF1E2838) else Color(0xFFDBEAFE),
            Color(0xFF2563EB)
        )
        TicketStatus.RESOLVED -> Pair(
            if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6),
            Color(0xFF4B5563)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Row 1: Ticket ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.reference,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = textSecondary
                )

                // Status Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ticket.statusText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = statusFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Row 2: Date
            Text(
                text = ticket.createdDateText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp
                ),
                color = textSecondary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Row 3: Subject + Right Chevron
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ticket.subject,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Row 4: Description
            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Editorial FAQ Expandable Card
 */
@Composable
private fun EditorialFaqCard(
    faq: FaqItem,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (faq.isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = faq.isExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = faq.answer,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        ),
                        color = textSecondary
                    )
                }
            }
        }
    }
}

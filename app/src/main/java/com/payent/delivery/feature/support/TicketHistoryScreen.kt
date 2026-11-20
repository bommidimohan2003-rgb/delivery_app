package com.payent.delivery.feature.support

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payent.delivery.core.ui.components.DemoModeBanner
import com.payent.delivery.core.ui.components.EmptyStateView
import com.payent.delivery.core.ui.components.PayentCard
import com.payent.delivery.core.ui.components.PayentSecondaryButton
import com.payent.delivery.core.ui.components.StatusTag
import com.payent.delivery.ui.theme.StatusSuccess
import com.payent.delivery.ui.theme.StatusWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketHistoryScreen(
    viewModel: SupportViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTicketDetail: (SupportTicket) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Support Tickets",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ticket History",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "View local support inquiries and status updates.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.supportTickets.isEmpty()) {
            EmptyStateView(
                title = "No support tickets created yet",
                message = "Submit a inquiry on the Help & Support screen to track ticket status here.",
                icon = Icons.Outlined.SupportAgent,
                actionButtonText = "Back to Support",
                onActionClick = onNavigateBack
            )
        } else {
            uiState.supportTickets.forEach { ticket ->
                TicketCardItem(
                    ticket = ticket,
                    onClick = {
                        viewModel.selectTicketForDetails(ticket)
                        onNavigateToTicketDetail(ticket)
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PayentSecondaryButton(
            text = "Back to Help & Support",
            onClick = onNavigateBack
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun TicketCardItem(
    ticket: SupportTicket,
    onClick: () -> Unit
) {
    PayentCard(onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ConfirmationNumber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = ticket.reference,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                StatusTag(
                    text = ticket.statusText,
                    color = if (ticket.statusText.contains("RESOLVED")) StatusSuccess else StatusWarning
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.subject,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ticket.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Submitted: ${ticket.createdDateText} • Category: ${ticket.category.name}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

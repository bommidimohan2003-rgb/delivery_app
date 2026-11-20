package com.payent.delivery.feature.orders

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.payent.delivery.core.ui.components.PayentCard
import com.payent.delivery.core.ui.components.PayentPrimaryButton
import com.payent.delivery.core.ui.components.PayentSecondaryButton
import com.payent.delivery.core.ui.components.StatusTag
import com.payent.delivery.ui.theme.StatusError
import com.payent.delivery.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductInspectionScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToReportProblem: () -> Unit,
    onNavigateToPin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()
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
                    text = "Product Inspection",
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
            text = "Product Inspection Checklist",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Verify equipment condition with the renter prior to final PIN handover.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Problem Report Warning Banner (if issue reported)
        if (uiState.hasOpenProblemReport) {
            PayentCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.ReportProblem,
                            contentDescription = null,
                            tint = StatusError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ISSUE REPORTED — DEMO DELIVERY BLOCKED",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = StatusError
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.problemReportSummary ?: "An issue report was submitted. Delivery completion is blocked.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Inspection Checklist Card
        PayentCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.FactCheck,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Handover Inspection Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    StatusTag(
                        text = if (uiState.isInspectionMarkedComplete) "INSPECTED DEMO" else "IN PROGRESS",
                        color = if (uiState.isInspectionMarkedComplete) StatusSuccess else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ChecklistItem(
                    label = "Correct product received (${order.rentalPeriod})",
                    isChecked = uiState.isCheckItemProductCorrect,
                    onCheckedChange = { viewModel.toggleCheckItem(1) }
                )

                ChecklistItem(
                    label = "Product condition inspected (No visible physical damage)",
                    isChecked = uiState.isCheckItemConditionInspected,
                    onCheckedChange = { viewModel.toggleCheckItem(2) }
                )

                ChecklistItem(
                    label = "All expected accessories present and intact",
                    isChecked = uiState.isCheckItemAccessoriesPresent,
                    onCheckedChange = { viewModel.toggleCheckItem(3) }
                )

                ChecklistItem(
                    label = "Product details match the rental order summary",
                    isChecked = uiState.isCheckItemMatchesOrder,
                    onCheckedChange = { viewModel.toggleCheckItem(4) }
                )

                ChecklistItem(
                    label = "Renter has had an opportunity to inspect the product",
                    isChecked = uiState.isCheckItemRenterInspected,
                    onCheckedChange = { viewModel.toggleCheckItem(5) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        OutlinedButton(
            onClick = onNavigateToReportProblem,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Outlined.ReportProblem,
                contentDescription = null,
                tint = StatusError
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Report a Problem",
                color = StatusError,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        PayentPrimaryButton(
            text = "Mark Inspection Complete & Proceed to PIN",
            enabled = !uiState.hasOpenProblemReport,
            onClick = {
                viewModel.markInspectionComplete(order)
                onNavigateToPin()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PayentSecondaryButton(
            text = "Back to Renter Verification",
            onClick = onNavigateBack
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ChecklistItem(
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

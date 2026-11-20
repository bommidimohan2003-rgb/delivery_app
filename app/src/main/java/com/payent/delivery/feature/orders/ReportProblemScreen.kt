package com.payent.delivery.feature.orders

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.payent.delivery.ui.theme.StatusError
import com.payent.delivery.ui.theme.StatusSuccess

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReportProblemScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val order = uiState.selectedOrderForDetails ?: OrdersViewModel.sampleDemoOrders.first()
    val scrollState = rememberScrollState()

    val categories = listOf(
        "Product damaged",
        "Missing accessories",
        "Wrong product",
        "Product condition dispute",
        "Incorrect order",
        "Other"
    )

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        val name = uri?.lastPathSegment ?: "issue_photo_sample.jpg"
        viewModel.attachProblemPhoto(uri?.toString(), name)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Report a Problem",
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
            text = "Report a Delivery Issue",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Order ${order.orderNumber} — Select a problem category and describe the issue.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error Banner
        if (uiState.errorMessage != null) {
            PayentCard(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = uiState.errorMessage ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = StatusError,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // Category Selection FlowRow
        PayentCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.ReportProblem,
                        contentDescription = null,
                        tint = StatusError
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "1. Select Problem Category (Required)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = uiState.selectedProblemCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectProblemCategory(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = StatusError.copy(alpha = 0.2f),
                                selectedLabelColor = StatusError
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Description Field Card
        PayentCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Issue Description (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.problemDescription,
                    onValueChange = { viewModel.onProblemDescriptionChanged(it) },
                    label = { Text("Describe the equipment issue or discrepancy...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Photo Attachment Card
        PayentCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Attach Issue Photo (Optional)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!uiState.problemPhotoFileName.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = StatusSuccess
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.problemPhotoFileName ?: "issue_photo.jpg",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo or Use Sample")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PayentPrimaryButton(
            text = "Submit Demo Report",
            onClick = {
                if (viewModel.submitProblemReport(order)) {
                    onNavigateBack()
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PayentSecondaryButton(
            text = "Cancel",
            onClick = onNavigateBack
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

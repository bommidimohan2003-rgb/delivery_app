package com.payent.delivery.feature.profile

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.domain.model.KycDocument
import com.payent.delivery.domain.model.KycDocumentStatus
import com.payent.delivery.domain.model.KycDocumentType
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Editorial Redesign of Complete KYC Verification (Screen 3) matching the reference design.
 *
 * Structure:
 * 1. Top Bar: Back arrow on left + Centered "Complete KYC Verification" title
 * 2. Subtitle: "Upload the required documents to start accepting orders."
 * 3. Connected 3-Step Verification Progress:
 *    - Step 1: Upload Documents (Upload clear photos of the required documents)
 *    - Step 2: Under Review (We will verify your documents)
 *    - Step 3: Approved (Start accepting delivery orders)
 * 4. Document Upload Cards:
 *    - Driving Licence (Front side of your driving licence + preview + Upload button)
 *    - Aadhaar Card (Front side of your Aadhaar card + preview + Upload button)
 *    - Bank Account Details (Passbook or cancelled cheque + preview + Upload button)
 * 5. Full-width Black Action Button: "Submit for Verification →"
 */
@Composable
fun KycScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val isDark = MaterialTheme.colorScheme.background == DarkBackground ||
            MaterialTheme.colorScheme.surface == DarkSurface

    // Editorial Palette
    val screenBg = if (isDark) DarkBackground else Color.White
    val cardBg = if (isDark) Color(0xFF141414) else Color.White
    val cardBorder = if (isDark) Color(0xFF282828) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF111111)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)

    var activeDocTypeForPicker by remember { mutableStateOf<KycDocumentType?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        activeDocTypeForPicker?.let { type ->
            if (uri != null) {
                val fileName = uri.lastPathSegment ?: "document_photo.jpg"
                viewModel.selectKycDocument(type, uri.toString(), fileName)
                Toast.makeText(context, "Document selected", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.selectKycDocument(type, "sample_local_uri.jpg", "sample_doc.jpg")
            }
        }
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
                    text = "Complete KYC Verification",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentKycStatus = uiState.partner?.kycStatus ?: KycStatus.NOT_STARTED

            when (currentKycStatus) {
                KycStatus.DOCUMENTS_SUBMITTED, KycStatus.PENDING, KycStatus.SUBMITTED_DEMO -> {
                    // CONFIRMATION VIEW (State: Submitted / Under Review)
                    KycSubmittedConfirmationView(
                        uiState = uiState,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        isDark = isDark,
                        onBackToDashboard = onNavigateBack
                    )
                }

                KycStatus.APPROVED, KycStatus.VERIFIED -> {
                    // APPROVAL SUCCESS VIEW (State: Approved)
                    KycApprovalSuccessView(
                        uiState = uiState,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        isDark = isDark,
                        onStartDelivering = onNavigateBack
                    )
                }

                KycStatus.REJECTED -> {
                    // REJECTION VIEW (State: Rejected)
                    KycRejectionView(
                        uiState = uiState,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        isDark = isDark,
                        onResubmit = { viewModel.resubmitKycDocuments() }
                    )
                }

                else -> {
                    // NOT_STARTED / DOCUMENT UPLOAD VIEW
                    KycDocumentUploadView(
                        uiState = uiState,
                        viewModel = viewModel,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        cardBg = cardBg,
                        cardBorder = cardBorder,
                        isDark = isDark,
                        onPickDocument = { type ->
                            activeDocTypeForPicker = type
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                }
            }

            // Bottom padding to avoid overlapping the floating bottom navigation dock
            Spacer(modifier = Modifier.height(115.dp))
        }
    }
}

@Composable
private fun KycDocumentUploadView(
    uiState: ProfileUiState,
    viewModel: ProfileViewModel,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color,
    isDark: Boolean,
    onPickDocument: (KycDocumentType) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Upload the required documents to start accepting orders.",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.5.sp,
                lineHeight = 18.sp
            ),
            color = textSecondary,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Connected 3-Step Verification Progress
        KycProgressTimeline(
            activeStep = 1,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Document Upload Cards
        val dlDoc = uiState.kycDocuments.find { it.type == KycDocumentType.DRIVING_LICENCE }
        val aadhaarDoc = uiState.kycDocuments.find { it.type == KycDocumentType.AADHAAR_CARD }
        val bankDoc = uiState.kycDocuments.find { it.type == KycDocumentType.BANK_PASSBOOK }

        // Document 1: Driving Licence
        KycDocumentCard(
            icon = Icons.Outlined.Badge,
            title = "Driving Licence",
            subtitle = "Front side of your driving licence",
            document = dlDoc,
            cardBg = cardBg,
            cardBorder = cardBorder,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark,
            onUploadClick = { onPickDocument(KycDocumentType.DRIVING_LICENCE) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Document 2: Aadhaar Card
        KycDocumentCard(
            icon = Icons.Outlined.Badge,
            title = "Aadhaar Card",
            subtitle = "Front side of your Aadhaar card",
            document = aadhaarDoc,
            cardBg = cardBg,
            cardBorder = cardBorder,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark,
            onUploadClick = { onPickDocument(KycDocumentType.AADHAAR_CARD) }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Document 3: Bank Account Details
        KycDocumentCard(
            icon = Icons.Outlined.AccountBalance,
            title = "Bank Account Details",
            subtitle = "Passbook or cancelled cheque",
            document = bankDoc,
            cardBg = cardBg,
            cardBorder = cardBorder,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark,
            onUploadClick = { onPickDocument(KycDocumentType.BANK_PASSBOOK) }
        )

        // Validation / Notice Feedback message
        if (uiState.kycValidationMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.kycValidationMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFFEF4444)
            )
        } else if (uiState.kycNoticeMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = uiState.kycNoticeMessage,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF16A34A)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Full-width Black Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(Color.Black)
                .clickable { viewModel.submitKycDocuments() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Submit for Verification",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun KycSubmittedConfirmationView(
    uiState: ProfileUiState,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color,
    isDark: Boolean,
    onBackToDashboard: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(12.dp))

        // Connected 3-Step Verification Progress (Step 2 active)
        KycProgressTimeline(
            activeStep = 2,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Confirmation Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = "Pending Review",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "KYC Submitted Successfully",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Under Admin Review",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = Color(0xFFD97706)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Your documents have been submitted. Our admin team will review your application.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    ),
                    color = textSecondary
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Metadata Details Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF1E222A) else Color(0xFFF9FAFB))
                        .border(1.dp, if (isDark) Color(0xFF2E3440) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Submission Status",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = textSecondary
                            )
                            Text(
                                text = "DOCUMENTS_SUBMITTED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFFD97706)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Submission Date",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = textSecondary
                            )
                            Text(
                                text = uiState.kycSubmissionDate ?: uiState.partner?.kycSubmissionDate ?: "Today",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = textPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Verification",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                                color = textSecondary
                            )
                            Text(
                                text = "PENDING",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submitted Documents List
                Text(
                    text = "Submitted Documents",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary
                )

                Spacer(modifier = Modifier.height(10.dp))

                uiState.kycDocuments.forEach { doc ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = "Uploaded",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = doc.selectedFileName ?: "Uploaded ✓",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.5.sp),
                            color = textSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Return to Dashboard Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.Black)
                .clickable(onClick = onBackToDashboard),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Back to Dashboard",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun KycApprovalSuccessView(
    uiState: ProfileUiState,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color,
    isDark: Boolean,
    onStartDelivering: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(12.dp))

        // 3-Step Timeline (All 3 steps completed)
        KycProgressTimeline(
            activeStep = 3,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Approval Celebration Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDCFCE7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Approved",
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Application Approved!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Status: APPROVED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF16A34A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Credential Email Notification Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF16231C) else Color(0xFFF0FDF4))
                        .border(1.dp, if (isDark) Color(0xFF22543D) else Color(0xFFBBF7D0), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = "Email",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Login Credentials Dispatched",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "Your delivery partner login credentials have been sent to your registered email address (${uiState.partner?.email ?: "your registered email"}).",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                ),
                                color = textSecondary
                            )
                        }
                    }
                }

                // Partner ID Card (Only shown if returned by authenticated API/session)
                val partnerId = uiState.partner?.id
                if (!partnerId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF1E222A) else Color(0xFFF9FAFB))
                            .border(1.dp, if (isDark) Color(0xFF2E3440) else Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Delivery Partner ID",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = textSecondary
                            )
                            Text(
                                text = partnerId,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Start Delivering Primary Action Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.Black)
                .clickable(onClick = onStartDelivering),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Start Delivering",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

@Composable
private fun KycRejectionView(
    uiState: ProfileUiState,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    cardBorder: Color,
    isDark: Boolean,
    onResubmit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(12.dp))

        // Main Rejection Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = "Rejected",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "KYC Verification Rejected",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Status: REJECTED",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFFDC2626)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Rejection Reason Box
                val reason = uiState.kycRejectionReason
                    ?: uiState.partner?.kycRejectionReason
                    ?: "Document photos were unclear or did not match registered details. Please upload clear, high-resolution document images."

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Color(0xFF291515) else Color(0xFFFEF2F2))
                        .border(1.dp, if (isDark) Color(0xFF5A2020) else Color(0xFFFECACA), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Admin Review Feedback:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFB91C1C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.5.sp,
                                lineHeight = 18.sp
                            ),
                            color = textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Review & Resubmit Black Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color.Black)
                .clickable(onClick = onResubmit),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Review & Resubmit Documents",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        }
    }
}

/**
 * 3-Step Connected Verification Progress matching the reference design
 */
@Composable
private fun KycProgressTimeline(
    activeStep: Int,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Step 1
        TimelineStepRow(
            stepNumber = 1,
            title = "Upload Documents",
            subtitle = "Upload clear photos of the required documents",
            isActive = activeStep >= 1,
            isCompleted = activeStep > 1,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark,
            hasFollowingLine = true
        )

        // Step 2
        TimelineStepRow(
            stepNumber = 2,
            title = "Under Review",
            subtitle = "We will verify your documents",
            isActive = activeStep >= 2,
            isCompleted = activeStep > 2,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark,
            hasFollowingLine = true
        )

        // Step 3
        TimelineStepRow(
            stepNumber = 3,
            title = "Approved",
            subtitle = "Start accepting delivery orders",
            isActive = activeStep >= 3,
            isCompleted = activeStep >= 3,
            textPrimary = textPrimary,
            textSecondary = textSecondary,
            isDark = isDark,
            hasFollowingLine = false
        )
    }
}

@Composable
private fun TimelineStepRow(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isActive: Boolean,
    isCompleted: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    hasFollowingLine: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Left Column: Circle Number + Vertical Connecting Line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> Color(0xFF16A34A)
                            isActive -> if (isDark) Color.White else Color.Black
                            else -> if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "$stepNumber",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = when {
                            isActive -> if (isDark) Color.Black else Color.White
                            else -> if (isDark) Color(0xFF888888) else Color(0xFF6B7280)
                        }
                    )
                }
            }

            if (hasFollowingLine) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(30.dp)
                        .background(if (isDark) Color(0xFF282828) else Color(0xFFE5E7EB))
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Column: Title + Subtitle
        Column(modifier = Modifier.padding(bottom = if (hasFollowingLine) 12.dp else 0.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (isActive) textPrimary else textSecondary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp
                ),
                color = textSecondary
            )
        }
    }
}

/**
 * Editorial Document Upload Card matching the reference design
 */
@Composable
private fun KycDocumentCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    document: KycDocument?,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    isDark: Boolean,
    onUploadClick: () -> Unit
) {
    val isUploaded = document?.status == KycDocumentStatus.SELECTED_LOCALLY ||
            document?.status == KycDocumentStatus.SUBMITTED_DEMO

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Icon + Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = textPrimary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lower Row: Mini Document Preview Card + Black Upload Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Graphic Illustration Card
                Box(
                    modifier = Modifier
                        .size(width = 82.dp, height = 48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDark) Color(0xFF1E222A) else Color(0xFFF1F5F9))
                        .border(0.8.dp, if (isDark) Color(0xFF2E3440) else Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                        .padding(5.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini photo frame
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isUploaded) Color(0xFFDCFCE7) else (if (isDark) Color(0xFF2A303C) else Color(0xFFCBD5E1))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploaded) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF16A34A),
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF9CA3AF) else Color(0xFF64748B),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(5.dp))

                        // Mini document text lines
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(if (isDark) Color(0xFF333C4E) else Color(0xFF94A3B8), RoundedCornerShape(1.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(3.dp)
                                    .background(if (isDark) Color(0xFF333C4E) else Color(0xFF94A3B8), RoundedCornerShape(1.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.45f)
                                    .height(3.dp)
                                    .background(if (isDark) Color(0xFF333C4E) else Color(0xFF94A3B8), RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }

                // Black Upload Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isUploaded) (if (isDark) Color.White else Color.Black) else Color.Black)
                        .clickable(onClick = onUploadClick)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isUploaded) "Uploaded ✓" else "Upload",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isUploaded && isDark) Color.Black else Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = "Upload",
                            tint = if (isUploaded && isDark) Color.Black else Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

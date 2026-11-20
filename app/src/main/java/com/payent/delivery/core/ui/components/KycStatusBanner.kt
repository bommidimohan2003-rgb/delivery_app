package com.payent.delivery.core.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface
import com.payent.delivery.ui.theme.StatusError
import com.payent.delivery.ui.theme.StatusWarning

@Composable
fun KycStatusBanner(
    kycStatus: KycStatus,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (kycStatus.isApproved) return

    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface

    val (title, message, buttonText, icon, statusColor) = when (kycStatus) {
        KycStatus.DOCUMENTS_SUBMITTED, KycStatus.SUBMITTED_DEMO, KycStatus.PENDING -> Tuple5(
            "Verification in progress",
            "Your documents have been submitted and are awaiting review.",
            "View Status",
            Icons.Outlined.HourglassTop,
            StatusWarning
        )
        KycStatus.REJECTED -> Tuple5(
            "KYC needs attention",
            "Review your document status and resubmit required documents.",
            "Review Documents",
            Icons.Outlined.ErrorOutline,
            StatusError
        )
        else -> Tuple5(
            "Complete your KYC",
            "Submit your required documents to unlock order acceptance.",
            "Complete Verification",
            Icons.Outlined.Badge,
            StatusError
        )
    }

    val bannerBgColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        Color(0xFFFFEAEA)
    }

    PayentCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onActionClick,
        containerColor = bannerBgColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp).padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onActionClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

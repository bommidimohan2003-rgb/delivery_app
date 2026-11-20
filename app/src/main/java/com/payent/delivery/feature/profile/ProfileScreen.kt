package com.payent.delivery.feature.profile

import android.net.Uri
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
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.HeadsetMic
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.core.ui.components.LoadingView
import com.payent.delivery.domain.model.KycStatus
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Editorial Redesign of Profile (Screen 1) matching the reference design.
 *
 * Structure:
 * 1. Top Bar: Centered "Profile" title + Top-right settings gear icon
 * 2. Profile Summary Card:
 *    - Large circular avatar with camera overlay button
 *    - User's name, registered mobile number
 *    - Online/Offline status pill + animated switch toggle
 * 3. KYC Banner:
 *    - Prominent black rounded banner with shield icon, title, description, and "Continue ->" button
 *    - Dynamic states: NOT_STARTED, PENDING/UNDER_REVIEW, APPROVED, REJECTED
 * 4. Profile Menu Card:
 *    - My Orders (View and manage your orders)
 *    - My Delivery Details (View delivery history & details)
 *    - My Earnings & Payouts (Track your earnings and payouts)
 *    - Notifications (View recent updates + red count badge)
 *    - Help & Support (Get help or raise a ticket)
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToKyc: () -> Unit = {},
    onNavigateToOrders: () -> Unit = {},
    onNavigateToEarnings: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val partner = uiState.partner
    val scrollState = rememberScrollState()

    val isDark = MaterialTheme.colorScheme.background == DarkBackground ||
            MaterialTheme.colorScheme.surface == DarkSurface

    // Editorial Palette
    val screenBg = if (isDark) DarkBackground else Color.White
    val cardBg = if (isDark) Color(0xFF141414) else Color.White
    val cardBorder = if (isDark) Color(0xFF282828) else Color(0xFFE5E7EB)
    val dividerCol = if (isDark) Color(0xFF222222) else Color(0xFFF1F3F5)
    val textPrimary = if (isDark) Color.White else Color(0xFF111111)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // User picked new avatar
            onNavigateToEditProfile()
        }
    }

    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Sign Out",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of Payent Delivery?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmDialog = false
                        viewModel.logout(onLoggedOut)
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(text = "Cancel", color = textPrimary)
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
        if (uiState.isLoading && uiState.partner == null) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 18.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(36.dp))

                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textPrimary
                    )

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Profile Summary Header (Seamlessly fitted to background matching reference design)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onNavigateToEditProfile)
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Circular Avatar with Camera Overlay
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF222834) else Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Profile Photo",
                                tint = if (isDark) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(44.dp)
                            )
                        }

                        // Camera Icon Button Overlay
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color.Black)
                                .border(2.dp, if (isDark) DarkBackground else Color.White, CircleShape)
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Center: Partner Details & Online Switch
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = partner?.name?.ifBlank { "Mohan B" } ?: "Mohan B",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = "View Profile Details",
                                tint = textSecondary.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = partner?.phoneNumber?.ifBlank { "+91 98765 43210" } ?: "+91 98765 43210",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp
                            ),
                            color = textSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Online/Offline status row with switch
                        val isOnline = partner?.isOnline ?: true
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Status Pill
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isOnline) {
                                            if (isDark) Color(0xFF132E1E) else Color(0xFFDCFCE7)
                                        } else {
                                            if (isDark) Color(0xFF242424) else Color(0xFFF3F4F6)
                                        }
                                    )
                                    .padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            if (isOnline) Color(0xFF16A34A) else Color(0xFF9CA3AF),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isOnline) "Online" else "Offline",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isOnline) Color(0xFF16A34A) else Color(0xFF6B7280)
                                )
                            }

                            // Toggle Switch
                            Switch(
                                checked = isOnline,
                                onCheckedChange = { viewModel.toggleOnlineStatus(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF22C55E),
                                    uncheckedThumbColor = Color(0xFFD1D5DB),
                                    uncheckedTrackColor = if (isDark) Color(0xFF333333) else Color(0xFFE5E7EB)
                                ),
                                modifier = Modifier.size(width = 44.dp, height = 24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Prominent Black KYC Banner
                val kycStatus = partner?.kycStatus ?: KycStatus.NOT_STARTED
                val (bannerTitle, bannerSubtitle, bannerAction) = when (kycStatus) {
                    KycStatus.APPROVED, KycStatus.VERIFIED -> Triple(
                        "KYC Verified",
                        "Identity verified. Ready for all delivery orders.",
                        "Verified ✓"
                    )
                    KycStatus.DOCUMENTS_SUBMITTED, KycStatus.PENDING, KycStatus.SUBMITTED_DEMO -> Triple(
                        "KYC Under Review",
                        "Your documents are under review. Turnaround: 24-48 hrs.",
                        "View Status →"
                    )
                    KycStatus.REJECTED -> Triple(
                        "KYC Needs Attention",
                        "Please update your documents to start accepting orders.",
                        "Re-verify →"
                    )
                    else -> Triple(
                        "Complete Your KYC",
                        "Verify your documents to start accepting orders.",
                        "Continue →"
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                        .clickable(onClick = onNavigateToKyc)
                        .padding(horizontal = 14.dp, vertical = 13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shield badge on left
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E222A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Center texts
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bannerTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bannerSubtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 14.sp
                                ),
                                color = Color(0xFF9CA3AF),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // White action pill button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable(onClick = onNavigateToKyc)
                                .padding(horizontal = 12.dp, vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bannerAction,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 4: Profile Menu Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Row 1: My Orders
                        ProfileMenuRow(
                            icon = Icons.AutoMirrored.Outlined.Assignment,
                            title = "My Orders",
                            subtitle = "View and manage your orders",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onNavigateToOrders
                        )

                        HorizontalDivider(thickness = 0.8.dp, color = dividerCol)

                        // Row 2: My Delivery Details
                        ProfileMenuRow(
                            icon = Icons.Outlined.LocalShipping,
                            title = "My Delivery Details",
                            subtitle = "View delivery history & profile details",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onNavigateToEditProfile
                        )

                        HorizontalDivider(thickness = 0.8.dp, color = dividerCol)

                        // Row 3: My Earnings & Payouts
                        ProfileMenuRow(
                            icon = Icons.Outlined.CreditCard,
                            title = "My Earnings & Payouts",
                            subtitle = "Track your earnings and payouts",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onNavigateToEarnings
                        )

                        HorizontalDivider(thickness = 0.8.dp, color = dividerCol)

                        // Row 4: Notifications (with red unread count badge)
                        ProfileMenuRow(
                            icon = Icons.Outlined.Notifications,
                            title = "Notifications",
                            subtitle = "View recent updates",
                            badgeText = "3",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onNavigateToNotifications
                        )

                        HorizontalDivider(thickness = 0.8.dp, color = dividerCol)

                        // Row 5: Help & Support
                        ProfileMenuRow(
                            icon = Icons.Outlined.HeadsetMic,
                            title = "Help & Support",
                            subtitle = "Get help or raise a ticket",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = onNavigateToSupport
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sign Out subtle text row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showLogoutConfirmDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFFEF4444)
                    )
                }

                // Bottom padding to avoid overlapping the floating bottom navigation dock
                Spacer(modifier = Modifier.height(110.dp))
            }
        }
    }
}

/**
 * Reusable clean menu row for Profile screen matching reference design
 */
@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeText: String? = null,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading Icon
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = textPrimary,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Center Title & Subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp
                ),
                color = textSecondary
            )
        }

        // Optional Red Badge (e.g. for unread notifications)
        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        // Trailing Right Chevron
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = textSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}

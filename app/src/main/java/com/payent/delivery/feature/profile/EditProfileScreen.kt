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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkSurface

/**
 * Editorial Redesign of Edit Profile (Screen 2) matching the reference design.
 *
 * Structure:
 * 1. Top Bar: Back arrow on left + Centered "Edit Profile" title
 * 2. Centered Circular Avatar with Camera Icon Overlay Badge
 * 3. Five Distinct Rounded Form Cards:
 *    - Full Name (Person icon)
 *    - Email (Mail icon)
 *    - Phone Number (Phone icon)
 *    - Address (Location pin icon + chevron)
 *    - Pincode (Target / navigation icon + chevron)
 * 4. Full-width Black Action Button: "Save Changes →"
 */
@Composable
fun EditProfileScreen(
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
    val cardBg = if (isDark) Color(0xFF141414) else Color(0xFFFAFAFA)
    val cardBorder = if (isDark) Color(0xFF282828) else Color(0xFFE5E7EB)
    val textPrimary = if (isDark) Color.White else Color(0xFF111111)
    val textSecondary = if (isDark) Color(0xFFAAAAAA) else Color(0xFF6B7280)

    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri
            Toast.makeText(context, "Profile photo selected", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initEditProfileForm()
    }

    LaunchedEffect(uiState.isProfileSaveSuccessMessage) {
        if (uiState.isProfileSaveSuccessMessage != null) {
            Toast.makeText(context, uiState.isProfileSaveSuccessMessage, Toast.LENGTH_SHORT).show()
            onNavigateBack()
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
                    text = "Edit Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Centered Circular Profile Avatar with Camera Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF222834) else Color(0xFFE2E8F0))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "Profile Photo",
                            tint = if (isDark) Color.White else Color(0xFF475569),
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    // Camera Circle Overlay Badge
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(2.dp, Color.White, CircleShape)
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
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 3: Form Fields
            // Field 1: Full Name
            EditorialInputField(
                label = "Full Name",
                value = uiState.editName,
                onValueChange = { viewModel.onEditNameChanged(it) },
                icon = Icons.Outlined.Person,
                placeholder = "Mohan B",
                errorMessage = uiState.editNameError,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 2: Email
            EditorialInputField(
                label = "Email",
                value = uiState.editEmail,
                onValueChange = { viewModel.onEditEmailChanged(it) },
                icon = Icons.Outlined.MailOutline,
                placeholder = "mohanb@gmail.com",
                errorMessage = uiState.editEmailError,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 3: Phone Number
            EditorialInputField(
                label = "Phone Number",
                value = uiState.editPhone,
                onValueChange = { viewModel.onEditPhoneChanged(it) },
                icon = Icons.Outlined.Phone,
                placeholder = "+91 98765 43210",
                errorMessage = uiState.editPhoneError,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 4: Address (with right chevron)
            EditorialInputField(
                label = "Address",
                value = uiState.editAddress,
                onValueChange = { viewModel.onEditAddressChanged(it) },
                icon = Icons.Outlined.LocationOn,
                placeholder = "123, 4th Street, Gachibowli",
                errorMessage = uiState.editAddressError,
                showTrailingChevron = true,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Field 5: Pincode (with right chevron)
            EditorialInputField(
                label = "Pincode",
                value = uiState.editPincode,
                onValueChange = { viewModel.onEditPincodeChanged(it) },
                icon = Icons.Outlined.NearMe,
                placeholder = "500032",
                errorMessage = uiState.editPincodeError,
                showTrailingChevron = true,
                cardBg = cardBg,
                cardBorder = cardBorder,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Section 4: Full-width Black Action Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(Color.Black)
                    .clickable {
                        viewModel.saveProfileChanges()
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Save Changes",
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

            // Bottom padding to avoid overlapping the floating bottom navigation dock
            Spacer(modifier = Modifier.height(115.dp))
        }
    }
}

/**
 * Editorial rounded input field card matching reference design
 */
@Composable
private fun EditorialInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    placeholder: String,
    errorMessage: String? = null,
    showTrailingChevron: Boolean = false,
    cardBg: Color,
    cardBorder: Color,
    textPrimary: Color,
    textSecondary: Color,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(
                    width = 1.dp,
                    color = if (errorMessage != null) Color(0xFFEF4444) else cardBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = textSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Input Content (Label + BasicTextField)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = textSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = TextStyle(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textPrimary
                        ),
                        cursorBrush = SolidColor(textPrimary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = keyboardType,
                            imeAction = imeAction
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = textSecondary.copy(alpha = 0.6f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Optional Right Chevron
                if (showTrailingChevron) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Inline Error Message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.5.sp
                ),
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

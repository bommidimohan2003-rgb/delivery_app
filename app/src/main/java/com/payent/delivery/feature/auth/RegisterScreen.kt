package com.payent.delivery.feature.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Editorial Redesign of Screen 3: Create Partner Account
 * matching the reference design (Phone 3).
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToOtpVerify: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var legalDialogTitle by remember { mutableStateOf<String?>(null) }
    var legalDialogContent by remember { mutableStateOf<String?>(null) }

    // When OTP is generated, navigate to OTP verification screen
    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            onNavigateToOtpVerify()
        }
    }

    if (legalDialogTitle != null) {
        AlertDialog(
            onDismissRequest = {
                legalDialogTitle = null
                legalDialogContent = null
            },
            title = {
                Text(
                    text = legalDialogTitle ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuthTokens.TextPrimary
                )
            },
            text = {
                Text(
                    text = legalDialogContent ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthTokens.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        legalDialogTitle = null
                        legalDialogContent = null
                    }
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold, color = AuthTokens.TextPrimary)
                }
            },
            containerColor = Color.White
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthTokens.ScreenBg)
    ) {
        // Subtle pale-green decorative shapes in top-right and bottom-left
        PaleGreenCurvedBackground(
            showTopRight = true,
            showBottomLeft = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // 1. Back button + Header
            AuthScreenHeader(
                title = "Create Partner Account",
                subtitle = "Register to start accepting Payent marketplace delivery orders.",
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Field 1: Full Name
            PartnerTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChanged,
                placeholder = "Full Name",
                leadingIcon = Icons.Outlined.Person,
                errorMessage = uiState.fullNameError,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Field 2: Email Address
            PartnerTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                placeholder = "Email Address",
                leadingIcon = Icons.Outlined.MailOutline,
                errorMessage = uiState.emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Field 3: Mobile Number (+91 | Mobile Number)
            PartnerPhoneTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChanged,
                placeholder = "Mobile Number",
                leadingIcon = Icons.Outlined.Phone,
                errorMessage = uiState.phoneNumberError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Field 4: Password
            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                placeholder = "Password",
                leadingIcon = Icons.Outlined.Lock,
                isPasswordVisible = uiState.isPasswordVisible,
                onToggleVisibility = viewModel::togglePasswordVisibility,
                errorMessage = uiState.passwordError,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 6. Field 5: Confirm Password
            PasswordTextField(
                value = uiState.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChanged,
                placeholder = "Confirm Password",
                leadingIcon = Icons.Outlined.Lock,
                isPasswordVisible = uiState.isConfirmPasswordVisible,
                onToggleVisibility = viewModel::toggleConfirmPasswordVisibility,
                errorMessage = uiState.confirmPasswordError,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.register() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Password Guideline Indicator: "✓ 8+ characters"
            val isMinLengthMet = uiState.password.length >= 8
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    tint = if (isMinLengthMet) AuthTokens.BrandGreen else Color(0xFF9CA3AF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "8+ characters",
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isMinLengthMet) AuthTokens.BrandGreen else Color(0xFF6B7280)
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 8. Partner Benefits (Row of 3 equal chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PartnerBenefitChip(
                    title = "Fast\nOnboarding",
                    icon = Icons.Outlined.Bolt,
                    iconTint = AuthTokens.BrandGreen,
                    modifier = Modifier.weight(1f)
                )

                PartnerBenefitChip(
                    title = "Secure\n& Trusted",
                    icon = Icons.Outlined.Shield,
                    iconTint = AuthTokens.TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                PartnerBenefitChip(
                    title = "Flexible\nEarnings",
                    icon = Icons.Outlined.LocationOn,
                    iconTint = AuthTokens.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Global Error Banner (if any)
            AnimatedVisibility(
                visible = uiState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (uiState.errorMessage != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFEF2F2))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                color = Color(0xFFB91C1C)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            // 9. Primary Action: "Register & Verify Mobile →"
            PrimaryBlackButton(
                text = "Register & Verify Mobile",
                onClick = { viewModel.register() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 10. Legal Notice at the bottom
            val annotatedLegalText = buildAnnotatedString {
                append("By registering, you agree to the Payent Partner ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AuthTokens.TextPrimary)) {
                    append("Terms of Service")
                }
                append(" and ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = AuthTokens.TextPrimary)) {
                    append("Privacy Policy")
                }
                append(".")
            }

            Text(
                text = annotatedLegalText,
                style = TextStyle(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = AuthTokens.TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        legalDialogTitle = "Payent Partner Terms & Privacy"
                        legalDialogContent = "Payent Partner platform agreements ensure secure payouts, verified deliveries, and equipment handling guidelines."
                    }
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Already have an account row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        color = AuthTokens.TextSecondary
                    )
                )
                Text(
                    text = "Sign In →",
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTokens.TextPrimary
                    ),
                    modifier = Modifier
                        .clickable(onClick = onNavigateToLogin)
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

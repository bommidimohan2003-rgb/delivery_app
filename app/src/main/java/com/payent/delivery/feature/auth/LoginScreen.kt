package com.payent.delivery.feature.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Editorial Redesign of Screen 2: Partner Sign In
 * matching the reference design (Phone 2).
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToOtpVerify: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    // When OTP is dispatched for mobile login, navigate to OTP verification screen
    LaunchedEffect(uiState.isOtpSent) {
        if (uiState.isOtpSent) {
            onNavigateToOtpVerify()
        }
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AuthTokens.TextPrimary
                )
            },
            text = {
                Text(
                    text = "A password reset link will be sent to your registered partner email address. Please contact partner support if you need immediate assistance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuthTokens.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showForgotPasswordDialog = false
                        Toast.makeText(context, "Password reset instructions sent", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.Bold,
                        color = AuthTokens.TextPrimary
                    )
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
        // Subtle pale-green decorative corner wave
        PaleGreenCurvedBackground(
            showTopRight = false,
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
                title = "Delivery Partner Sign In",
                subtitle = "Enter your Delivery Partner ID and password to access your delivery dashboard.",
                onNavigateBack = onNavigateBack
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 2. Delivery Partner ID Input Card
            PartnerTextField(
                value = if (uiState.partnerId.isNotBlank()) uiState.partnerId else uiState.email,
                onValueChange = { input ->
                    viewModel.onPartnerIdChanged(input)
                    viewModel.onEmailChanged(input)
                },
                placeholder = "Delivery Partner ID",
                leadingIcon = Icons.Outlined.Badge,
                errorMessage = uiState.partnerIdError ?: uiState.emailError,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Password Input Card
            PasswordTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChanged,
                placeholder = "Password",
                leadingIcon = Icons.Outlined.Lock,
                isPasswordVisible = uiState.isPasswordVisible,
                onToggleVisibility = viewModel::togglePasswordVisibility,
                errorMessage = uiState.passwordError,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.login() }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Right-aligned Forgot Password?
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "Forgot Password?",
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTokens.TextPrimary
                    ),
                    modifier = Modifier
                        .clickable { showForgotPasswordDialog = true }
                        .padding(vertical = 4.dp, horizontal = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Backend Capability / Compatibility Notice Banner (if any)
            AnimatedVisibility(
                visible = uiState.backendCompatibilityNotice != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (uiState.backendCompatibilityNotice != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = uiState.backendCompatibilityNotice ?: "",
                            style = TextStyle(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFF1E40AF)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

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

            // 5. Primary Sign In Black Button
            PrimaryBlackButton(
                text = "Sign In",
                onClick = { viewModel.login() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. "OR" Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE5E7EB),
                    thickness = 1.dp
                )
                Text(
                    text = "OR",
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTokens.TextMuted
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFE5E7EB),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 7. Alternative Mobile Login Section
            Text(
                text = "Login with Mobile Number",
                style = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthTokens.TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Enter your 10-digit mobile number to receive a one-time password.",
                style = TextStyle(
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = AuthTokens.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Input (+91 | Mobile Number)
            PartnerPhoneTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChanged,
                placeholder = "Mobile Number",
                leadingIcon = Icons.Outlined.PhoneAndroid,
                errorMessage = uiState.phoneNumberError,
                imeAction = ImeAction.Done,
                onImeAction = { viewModel.requestOtpForLogin() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Soft Pill Button: "▷ Send OTP →"
            SoftPillButton(
                text = "Send OTP",
                leadingIcon = Icons.Outlined.PlayCircleOutline,
                onClick = { viewModel.requestOtpForLogin() },
                isLoading = uiState.isLoading
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Demo Login Button (Preserving existing demo capability cleanly)
            SecondaryOutlinedButton(
                text = "Demo Login (Instant Access)",
                onClick = { viewModel.startDemoLogin() },
                showArrow = false
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 8. Footer: "Don't have a partner account? Register →"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have a partner account? ",
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        color = AuthTokens.TextSecondary
                    )
                )
                Text(
                    text = "Register →",
                    style = TextStyle(
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTokens.TextPrimary
                    ),
                    modifier = Modifier
                        .clickable(onClick = onNavigateToRegister)
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

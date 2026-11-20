package com.payent.delivery.feature.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Editorial design tokens for PayentDelivery Onboarding, Login & Registration.
 */
object AuthTokens {
    val ScreenBg = Color(0xFFFFFFFF)
    val InputSurface = Color(0xFFFAFAFA)
    val InputBorder = Color(0xFFE5E7EB)
    val InputBorderFocused = Color(0xFF111111)
    val InputBorderError = Color(0xFFEF4444)

    val TextPrimary = Color(0xFF111111)
    val TextSecondary = Color(0xFF6B7280)
    val TextMuted = Color(0xFF9CA3AF)

    val BrandGreen = Color(0xFF16A34A)
    val BrandGreenSoft = Color(0xFFDCFCE7)
    val BrandGreenPale = Color(0xFFF0FDF4)

    val ButtonPrimaryBg = Color(0xFF000000)
    val ButtonPrimaryText = Color(0xFFFFFFFF)

    val ButtonSecondaryBg = Color(0xFFFFFFFF)
    val ButtonSecondaryBorder = Color(0xFFE5E7EB)
    val ButtonSecondaryText = Color(0xFF111111)

    val SoftPillBg = Color(0xFFF1F5F9)
    val SoftPillText = Color(0xFF1E293B)

    val CardRadius = 16.dp
    val ButtonRadius = 26.dp
    val ChipRadius = 12.dp

    val ButtonHeight = 52.dp
    val InputHeight = 52.dp
}

/**
 * Subtle pale green curved decorative background wave matching the reference design.
 */
@Composable
fun PaleGreenCurvedBackground(
    modifier: Modifier = Modifier,
    showTopRight: Boolean = false,
    showBottomLeft: Boolean = true
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (showBottomLeft) {
            // Smooth pale-green wave in bottom-left
            val bottomPath = Path().apply {
                moveTo(0f, height * 0.90f)
                cubicTo(
                    width * 0.15f, height * 0.88f,
                    width * 0.35f, height * 0.94f,
                    width * 0.50f, height
                )
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = bottomPath,
                color = Color(0xFFDCFCE7).copy(alpha = 0.45f)
            )

            val bottomSubPath = Path().apply {
                moveTo(0f, height * 0.93f)
                cubicTo(
                    width * 0.20f, height * 0.92f,
                    width * 0.40f, height * 0.96f,
                    width * 0.65f, height
                )
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = bottomSubPath,
                color = Color(0xFFF0FDF4).copy(alpha = 0.65f)
            )
        }

        if (showTopRight) {
            // Soft pale-green glow in top-right
            val topPath = Path().apply {
                moveTo(width * 0.70f, 0f)
                cubicTo(
                    width * 0.80f, height * 0.08f,
                    width * 0.90f, height * 0.12f,
                    width, height * 0.15f
                )
                lineTo(width, 0f)
                close()
            }
            drawPath(
                path = topPath,
                color = Color(0xFFDCFCE7).copy(alpha = 0.35f)
            )
        }
    }
}

/**
 * Clean Payent brand logo header:
 * Stylized green 'p' icon mark + bold "payent" wordmark + "DELIVERY" letter-spaced subtitle.
 */
@Composable
fun PayentBrandHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Stylized 'p' mark in green
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AuthTokens.BrandGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "p",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = "payent",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = AuthTokens.TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                text = "DELIVERY",
                style = TextStyle(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AuthTokens.TextSecondary,
                    letterSpacing = 2.5.sp
                )
            )
        }
    }
}

/**
 * Back Navigation Header with title and supporting subtitle.
 */
@Composable
fun AuthScreenHeader(
    title: String,
    subtitle: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .padding(0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = AuthTokens.TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            ),
            color = AuthTokens.TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp
            ),
            color = AuthTokens.TextSecondary
        )
    }
}

/**
 * Modern rounded input field matching the reference image.
 */
@Composable
fun PartnerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AuthTokens.InputHeight)
                .clip(RoundedCornerShape(AuthTokens.CardRadius))
                .background(AuthTokens.InputSurface)
                .border(
                    width = 1.dp,
                    color = if (errorMessage != null) AuthTokens.InputBorderError else AuthTokens.InputBorder,
                    shape = RoundedCornerShape(AuthTokens.CardRadius)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = placeholder,
                    tint = AuthTokens.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuthTokens.TextPrimary
                    ),
                    cursorBrush = SolidColor(AuthTokens.TextPrimary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onImeAction() },
                        onDone = { onImeAction() }
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AuthTokens.TextMuted
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        InlineValidationMessage(errorMessage)
    }
}

/**
 * Mobile Number input field with Indian country code (+91) and separator.
 */
@Composable
fun PartnerPhoneTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Mobile Number",
    leadingIcon: ImageVector,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AuthTokens.InputHeight)
                .clip(RoundedCornerShape(AuthTokens.CardRadius))
                .background(AuthTokens.InputSurface)
                .border(
                    width = 1.dp,
                    color = if (errorMessage != null) AuthTokens.InputBorderError else AuthTokens.InputBorder,
                    shape = RoundedCornerShape(AuthTokens.CardRadius)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = "Phone",
                    tint = AuthTokens.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "+91",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTokens.TextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Vertical separator line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(18.dp)
                        .background(AuthTokens.InputBorder)
                )

                Spacer(modifier = Modifier.width(10.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuthTokens.TextPrimary
                    ),
                    cursorBrush = SolidColor(AuthTokens.TextPrimary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onImeAction() },
                        onDone = { onImeAction() }
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AuthTokens.TextMuted
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        InlineValidationMessage(errorMessage)
    }
}

/**
 * Secure password input field with lock leading icon and visibility eye toggle.
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPasswordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AuthTokens.InputHeight)
                .clip(RoundedCornerShape(AuthTokens.CardRadius))
                .background(AuthTokens.InputSurface)
                .border(
                    width = 1.dp,
                    color = if (errorMessage != null) AuthTokens.InputBorderError else AuthTokens.InputBorder,
                    shape = RoundedCornerShape(AuthTokens.CardRadius)
                )
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = placeholder,
                    tint = AuthTokens.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuthTokens.TextPrimary
                    ),
                    cursorBrush = SolidColor(AuthTokens.TextPrimary),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = imeAction
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { onImeAction() },
                        onDone = { onImeAction() }
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = AuthTokens.TextMuted
                                )
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = "Toggle password visibility",
                        tint = AuthTokens.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        InlineValidationMessage(errorMessage)
    }
}

/**
 * Full-width black action pill button matching the reference design.
 */
@Composable
fun PrimaryBlackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    showArrow: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AuthTokens.ButtonHeight)
            .clip(RoundedCornerShape(AuthTokens.ButtonRadius))
            .background(if (enabled) AuthTokens.ButtonPrimaryBg else Color(0xFF9CA3AF))
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuthTokens.ButtonPrimaryText
                    )
                )
                if (showArrow) {
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
}

/**
 * Full-width white outlined action pill button.
 */
@Composable
fun SecondaryOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showArrow: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AuthTokens.ButtonHeight)
            .clip(RoundedCornerShape(AuthTokens.ButtonRadius))
            .background(AuthTokens.ButtonSecondaryBg)
            .border(
                width = 1.dp,
                color = AuthTokens.ButtonSecondaryBorder,
                shape = RoundedCornerShape(AuthTokens.ButtonRadius)
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthTokens.ButtonSecondaryText
                )
            )
            if (showArrow) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = AuthTokens.ButtonSecondaryText,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Soft grey pill action button (e.g. "▷ Send OTP →").
 */
@Composable
fun SoftPillButton(
    text: String,
    leadingIcon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(AuthTokens.SoftPillBg)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = AuthTokens.TextPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = AuthTokens.SoftPillText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = TextStyle(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTokens.SoftPillText
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = AuthTokens.SoftPillText,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Screen 1 Three-column Benefits Card.
 */
@Composable
fun WelcomeBenefitsCard(
    item1Title: String,
    item1Icon: ImageVector,
    item2Title: String,
    item2Icon: ImageVector,
    item3Title: String,
    item3Icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFEDF0F3), RoundedCornerShape(18.dp))
            .padding(vertical = 16.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item 1
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item1Icon,
                    contentDescription = item1Title,
                    tint = AuthTokens.TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item1Title,
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTokens.TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }

            // Separator 1
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0xFFE2E8F0))
            )

            // Item 2
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item2Icon,
                    contentDescription = item2Title,
                    tint = AuthTokens.TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item2Title,
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTokens.TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }

            // Separator 2
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Color(0xFFE2E8F0))
            )

            // Item 3
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = item3Icon,
                    contentDescription = item3Title,
                    tint = AuthTokens.TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item3Title,
                    style = TextStyle(
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuthTokens.TextPrimary,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

/**
 * Screen 3 Compact Partner Benefit Chip.
 */
@Composable
fun PartnerBenefitChip(
    title: String,
    icon: ImageVector,
    iconTint: Color = AuthTokens.TextPrimary,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AuthTokens.ChipRadius))
            .background(Color(0xFFF9FAFB))
            .border(1.dp, AuthTokens.InputBorder, RoundedCornerShape(AuthTokens.ChipRadius))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuthTokens.TextPrimary,
                    lineHeight = 14.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Inline validation error with smooth fade.
 */
@Composable
fun InlineValidationMessage(
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = errorMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = TextStyle(
                    fontSize = 11.5.sp,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                ),
                modifier = modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

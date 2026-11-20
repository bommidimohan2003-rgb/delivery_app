package com.payent.delivery.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.DarkBackground
import com.payent.delivery.ui.theme.DarkBorder
import com.payent.delivery.ui.theme.DarkSurface
import com.payent.delivery.ui.theme.StatusError
import com.payent.delivery.ui.theme.TextMutedDark

/**
 * 8-Digit Secret PIN Input Component.
 *
 * Provides a high-contrast visual slot layout for entering an 8-digit numeric secret PIN.
 * Treats the PIN as a strict String, ensuring leading zeros (e.g., "01234567") are preserved.
 * Supports masked entry (bullet dots) with an optional visibility toggle and accessible touch focus.
 */
@Composable
fun EightDigitPinInput(
    pin: String,
    onPinChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isMasked: Boolean = true,
    onToggleMask: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    onImeActionDone: (() -> Unit)? = null
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    val isDark = MaterialTheme.colorScheme.background == DarkBackground || MaterialTheme.colorScheme.surface == DarkSurface

    val slotBg = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF5F5F5)
    val slotBorderNormal = if (isDark) DarkBorder else Color(0xFFD0D0D0)
    val slotBorderActive = if (isDark) Color.White else Color.Black
    val slotBorderError = StatusError
    val textColor = if (isDark) Color.White else Color.Black
    val textMuted = if (isDark) TextMutedDark else Color(0xFF757575)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hidden BasicTextField capturing keyboard inputs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    if (enabled) focusRequester.requestFocus()
                },
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = pin,
                onValueChange = { input ->
                    if (enabled) {
                        // Keep numeric digits only, up to 8 characters (preserving leading zeros as string)
                        val sanitized = input.filter { it.isDigit() }.take(8)
                        onPinChange(sanitized)
                    }
                },
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onImeActionDone?.invoke() }
                ),
                cursorBrush = SolidColor(Color.Transparent)
            )

            // Visual 8-Digit Slot Grid (arranged in 2 groups of 4 with a separator dash)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Group 1: First 4 digits (indices 0..3)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0..3) {
                        PinDigitCell(
                            digit = pin.getOrNull(i),
                            isFocused = pin.length == i && enabled,
                            isMasked = isMasked,
                            isError = isError,
                            backgroundColor = slotBg,
                            borderColorNormal = slotBorderNormal,
                            borderColorActive = slotBorderActive,
                            borderColorError = slotBorderError,
                            textColor = textColor
                        )
                    }
                }

                // Center Separator
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(8.dp)
                        .height(2.dp)
                        .background(textMuted, CircleShape)
                )

                // Group 2: Second 4 digits (indices 4..7)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 4..7) {
                        PinDigitCell(
                            digit = pin.getOrNull(i),
                            isFocused = pin.length == i && enabled,
                            isMasked = isMasked,
                            isError = isError,
                            backgroundColor = slotBg,
                            borderColorNormal = slotBorderNormal,
                            borderColorActive = slotBorderActive,
                            borderColorError = slotBorderError,
                            textColor = textColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visibility Toggle Button & Digit Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onToggleMask != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onToggleMask)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isMasked) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                        contentDescription = if (isMasked) "Show PIN digits" else "Hide PIN digits",
                        tint = textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMasked) "Show PIN" else "Hide PIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = textMuted
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Text(
                text = "${pin.length}/8 digits",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = if (pin.length == 8) (if (isDark) Color.White else Color.Black) else textMuted
            )
        }

        // Error message row
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = slotBorderError,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PinDigitCell(
    digit: Char?,
    isFocused: Boolean,
    isMasked: Boolean,
    isError: Boolean,
    backgroundColor: Color,
    borderColorNormal: Color,
    borderColorActive: Color,
    borderColorError: Color,
    textColor: Color
) {
    val borderCol = when {
        isError -> borderColorError
        isFocused -> borderColorActive
        digit != null -> borderColorActive.copy(alpha = 0.6f)
        else -> borderColorNormal
    }

    Box(
        modifier = Modifier
            .size(width = 34.dp, height = 46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused || isError) 1.5.dp else 1.dp,
                color = borderCol,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (digit != null) {
            if (isMasked) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .background(textColor, CircleShape)
                )
            } else {
                Text(
                    text = digit.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

package com.payent.delivery.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.ui.theme.StatusError

/**
 * High-contrast 4-digit PIN input component matching the Payent monochrome design system.
 * Strictly manages PIN state as a [String] to preserve leading zeros.
 * Features 4 segmented visual slots, masked bullet display by default, unmask toggle,
 * and high-contrast error states.
 */
@Composable
fun FourDigitPinInput(
    pin: String,
    onPinChange: (String) -> Unit,
    isMasked: Boolean,
    onToggleMask: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    onImeConfirm: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Invisible input field that captures numeric keyboard events
        Box(contentAlignment = Alignment.Center) {
            BasicTextField(
                value = pin,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(4)
                    onPinChange(filtered)
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
                    onDone = { onImeConfirm() }
                ),
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.Transparent)
            )

            // 4 Segmented Visual Slots: [1] [2] [3] [4]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        focusRequester.requestFocus()
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until 4) {
                    val digit = pin.getOrNull(index)?.toString()
                    val isFocused = pin.length == index && enabled
                    PinDigitSlot(
                        digit = digit,
                        isMasked = isMasked,
                        isFocused = isFocused,
                        isError = isError
                    )
                    if (index < 3) {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Controls row: Show/Hide Toggle & Digit Counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable(onClick = onToggleMask)
            ) {
                Icon(
                    imageVector = if (isMasked) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    contentDescription = if (isMasked) "Show PIN" else "Hide PIN",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isMasked) "Show PIN" else "Hide PIN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${pin.length}/4 digits",
                style = MaterialTheme.typography.labelSmall,
                color = if (pin.length == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (pin.length == 4) FontWeight.Bold else FontWeight.Normal
            )
        }

        // Error message if present
        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = StatusError,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun PinDigitSlot(
    digit: String?,
    isMasked: Boolean,
    isFocused: Boolean,
    isError: Boolean
) {
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> StatusError
            isFocused -> MaterialTheme.colorScheme.primary
            digit != null -> MaterialTheme.colorScheme.outline
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        label = "slotBorderColor"
    )

    val backgroundColor = when {
        digit != null -> Color(0xFF1E1E1E)
        isFocused -> Color(0xFF1A1A1A)
        else -> Color(0xFF141414)
    }

    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused || isError) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (digit != null) {
            if (isMasked) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color.White)
                )
            } else {
                Text(
                    text = digit,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )
            }
        } else if (isFocused) {
            Box(
                modifier = Modifier
                    .size(width = 2.dp, height = 24.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

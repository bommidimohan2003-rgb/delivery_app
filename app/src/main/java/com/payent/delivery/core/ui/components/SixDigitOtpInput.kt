package com.payent.delivery.core.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.payent.delivery.ui.theme.isDark

/**
 * Six-digit segmented numeric OTP input component matching the Payent design system.
 * Renders 6 distinct visual slots with clean dashes for empty states and bold digits when entered.
 */
@Composable
fun SixDigitOtpInput(
    otp: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    slotBgColor: Color = Color.White,
    digitColor: Color = Color(0xFF111111),
    onImeConfirm: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Invisible input field capturing numeric keyboard events
            BasicTextField(
                value = otp,
                onValueChange = { input ->
                    val filtered = input.filter { it.isDigit() }.take(6)
                    onOtpChange(filtered)
                },
                modifier = Modifier
                    .size(1.dp)
                    .focusRequester(focusRequester),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onImeConfirm() }
                ),
                cursorBrush = SolidColor(Color.Transparent),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.Transparent)
            )

            // 6 Segmented Visual Slots: [ - ] [ - ] [ - ] [ - ] [ - ] [ - ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        focusRequester.requestFocus()
                    },
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (index in 0 until 6) {
                    val digit = otp.getOrNull(index)?.toString()
                    val isFocusedSlot = enabled && (index == otp.length || (index == 5 && otp.length == 6))

                    val slotBorderColor = when {
                        isError -> StatusError
                        isFocusedSlot -> Color(0xFF000000)
                        digit != null -> Color(0xFF222222)
                        else -> Color(0xFFD1D5DB)
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(slotBgColor)
                            .border(
                                width = if (isFocusedSlot || isError) 1.5.dp else 1.dp,
                                color = slotBorderColor,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (digit != null) {
                            Text(
                                text = digit,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (enabled) digitColor else Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            )
                        } else {
                            Text(
                                text = "—",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                    color = Color(0xFF9CA3AF),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = StatusError,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

package com.example.melodyplayer.otp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*

@Composable
fun OtpScreen(
    email: String,
    onSuccess: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    var otpValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Melody Player",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00D9FF), Color(0xFFFF00E5))
                    )
                ),
                modifier = Modifier.padding(top = 40.dp, bottom = 60.dp)
            )

            // Title
            Text(
                text = "Xác thực OTP",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF00D9FF), Color(0xFFFF00E5))
                    )
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Subtitle
            Text(
                text = "Mã OTP đã gửi đến",
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFFB8B8B8)
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = email,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = Color(0xFF00D9FF),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            // OTP Input Boxes
            OtpInputField(
                otpValue = otpValue,
                onOtpChange = { newValue ->
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        otpValue = newValue
                        errorMessage = ""
                    }
                },
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Error Message
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Verify Button
            Button(
                onClick = {
                    if (otpValue.length == 6) {
                        isLoading = true
                        errorMessage = ""
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val success = OtpApi.verifyOtp(email, otpValue)
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    if (success) {
                                        onSuccess()
                                    } else {
                                        errorMessage = "Mã OTP không đúng"
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                    errorMessage = "Có lỗi xảy ra, vui lòng thử lại"
                                }
                            }
                        }
                    } else {
                        errorMessage = "Vui lòng nhập đủ 6 số"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF00D9FF), Color(0xFFFF00E5))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Xác thực",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend OTP
            TextButton(
                onClick = {
                    // TODO: Implement resend OTP
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "Gửi lại mã OTP",
                    color = Color(0xFF00D9FF),
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun OtpInputField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = otpValue,
        onValueChange = onOtpChange,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        decorationBox = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier
            ) {
                repeat(6) { index ->
                    OtpDigitBox(
                        digit = otpValue.getOrNull(index)?.toString() ?: "",
                        isFocused = otpValue.length == index
                    )
                }
            }
        },
        textStyle = TextStyle(color = Color.Transparent),
        modifier = Modifier.size(0.dp)
    )
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean
) {
    val borderColor = when {
        digit.isNotEmpty() -> Brush.linearGradient(
            colors = listOf(Color(0xFF00D9FF), Color(0xFFFF00E5))
        )

        isFocused -> Brush.linearGradient(
            colors = listOf(Color(0xFF00D9FF), Color(0xFFFF00E5))
        )

        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF2A2F4F), Color(0xFF2A2F4F))
        )
    }

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A1F3A))
            .border(
                width = 2.dp,
                brush = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        )
    }
}
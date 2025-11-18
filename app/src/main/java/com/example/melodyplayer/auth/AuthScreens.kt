package com.example.melodyplayer.auth

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.melodyplayer.otp.OtpApi
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlin.random.Random

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    // =============================
    // KHAI BÁO BIẾN
    // =============================
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var rememberLogin by remember { mutableStateOf(sharedPrefs.getBoolean("remember_login", false)) }

    // CAPTCHA
    var captchaCode by remember { mutableStateOf(generateCaptcha()) }
    var captchaInput by remember { mutableStateOf("") }

    // OTP SCREEN
    var showOtpScreen by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }
    var otpMessage by remember { mutableStateOf("") }
    var otpMode by remember { mutableStateOf("") }
    var isVerifyingOtp by remember { mutableStateOf(false) }

    // =============================
    // LOAD EMAIL KHI ĐĂNG NHẬP
    // =============================
    LaunchedEffect(isLogin) {
        if (isLogin) {
            email = sharedPrefs.getString("saved_email", "") ?: ""
        }
    }

    // AUTO LOGIN nếu đã nhớ tài khoản và có user
    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        val savedEmail = sharedPrefs.getString("saved_email", "")
        val rememberMe = sharedPrefs.getBoolean("remember_login", false)

        if (currentUser != null && rememberMe && !savedEmail.isNullOrEmpty()) {
            onLoginSuccess()
        }
    }

    // =============================
    // MÀN HÌNH OTP → ƯU TIÊN HIỂN TRƯỚC
    // =============================
    if (showOtpScreen) {
        OtpNeonScreen(
            email = email,
            otp = otp,
            onOtpChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    otp = it
                    otpMessage = ""
                }
            },
            message = otpMessage,
            isLoading = isVerifyingOtp,
            onVerify = {
                if (otp.length != 6) {
                    otpMessage = "Vui lòng nhập đủ 6 số"
                    return@OtpNeonScreen
                }

                isVerifyingOtp = true
                otpMessage = ""

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val ok = OtpApi.verifyOtp(email, otp)

                        withContext(Dispatchers.Main) {
                            isVerifyingOtp = false

                            if (!ok) {
                                otpMessage = "OTP không đúng"
                                return@withContext
                            }

                            if (otpMode == "register") {
                                // ĐĂNG KÝ
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            // Lưu trạng thái đăng nhập
                                            sharedPrefs.edit().apply {
                                                putBoolean("remember_login", true)
                                                putString("saved_email", email)
                                                apply()
                                            }
                                            onLoginSuccess()
                                        } else {
                                            otpMessage = it.exception?.message ?: "Lỗi tạo tài khoản"
                                        }
                                    }
                            } else {
                                // ĐĂNG NHẬP
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            // Lưu trạng thái đăng nhập
                                            sharedPrefs.edit().apply {
                                                putBoolean("remember_login", rememberLogin)
                                                if (rememberLogin) {
                                                    putString("saved_email", email)
                                                }
                                                apply()
                                            }
                                            onLoginSuccess()
                                        } else {
                                            otpMessage = it.exception?.message ?: "Lỗi đăng nhập"
                                        }
                                    }
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            isVerifyingOtp = false
                            otpMessage = "Có lỗi xảy ra, vui lòng thử lại"
                        }
                    }
                }
            },
            onBack = {
                showOtpScreen = false
                otp = ""
                otpMessage = ""
            }
        )
        return
    }

    // ============================================
    // UI CHÍNH – ĐĂNG KÝ / ĐĂNG NHẬP
    // ============================================
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0014),
                        Color(0xFF160028),
                        Color(0xFF22003E)
                    )
                )
            )
    ) {

        // Backdrop glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-120).dp, y = 100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF00FF).copy(alpha = 0.15f))
                .blur(100.dp)
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 240.dp, y = 450.dp)
                .clip(CircleShape)
                .background(Color(0xFF00FFFF).copy(alpha = 0.12f))
                .blur(120.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Logo
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(20.dp, CircleShape),
                shape = CircleShape,
                color = Color.Transparent
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(70.dp),
                        tint = Color(0xFF00FFFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Melody Player",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF00FF)
            )

            Text(
                "Neon Sound Experience 🎶",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // CARD FORM
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(20.dp, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1C002F).copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {

                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        if (isLogin) "Đăng nhập" else "Đăng ký",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FFFF)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    @Composable
                    fun fieldColors() = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A0046),
                        unfocusedContainerColor = Color(0xFF1A002E),
                        focusedIndicatorColor = Color(0xFFFF00FF),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF00FFFF)
                    )

                    // EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Color(0xFF00FFFF)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Mật khẩu", color = Color.White.copy(alpha = 0.4f)) },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFFFF00FF)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    null,
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColors()
                    )

                    // CONFIRM PASSWORD (REGISTER ONLY)
                    AnimatedVisibility(!isLogin) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = { Text("Xác nhận mật khẩu", color = Color.White.copy(alpha = 0.4f)) },
                                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF00FFFF)) },
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            null,
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                shape = RoundedCornerShape(16.dp),
                                colors = fieldColors()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ==============================
                    // CAPTCHA UI
                    // ==============================
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = captchaCode,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF00FF),
                            modifier = Modifier
                                .background(Color(0xFF2A0046), RoundedCornerShape(10.dp))
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        OutlinedTextField(
                            value = captchaInput,
                            onValueChange = { captchaInput = it },
                            placeholder = { Text("Nhập CAPTCHA", color = Color.White.copy(alpha = 0.4f)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = fieldColors()
                        )
                    }

                    // Remember me checkbox (chỉ hiện khi đăng nhập)
                    if (isLogin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberLogin,
                                onCheckedChange = { rememberLogin = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF00FFFF),
                                    uncheckedColor = Color.White.copy(alpha = 0.5f)
                                )
                            )
                            Text(
                                "Ghi nhớ đăng nhập",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // BUTTON
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            // ========================
                            // KIỂM TRA CAPTCHA
                            // ========================
                            if (captchaInput.trim() != captchaCode.trim()) {
                                errorMessage = "CAPTCHA không đúng!"
                                captchaCode = generateCaptcha()
                                captchaInput = ""
                                isLoading = false
                                return@Button
                            }

                            // Validate email
                            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                                errorMessage = "Email không hợp lệ"
                                isLoading = false
                                return@Button
                            }

                            // Validate password
                            if (password.length < 6) {
                                errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
                                isLoading = false
                                return@Button
                            }

                            // ========================
                            // GỬI OTP
                            // ========================
                            if (isLogin) {
                                // ĐĂNG NHẬP → gửi OTP
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        android.util.Log.d("AuthScreen", "Đang gửi OTP cho email: $email")
                                        val ok = OtpApi.sendOtp(email)
                                        android.util.Log.d("AuthScreen", "Kết quả gửi OTP: $ok")
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (ok) {
                                                otpMode = "login"
                                                showOtpScreen = true
                                            } else {
                                                errorMessage = "Không gửi được OTP. Vui lòng kiểm tra email hoặc kết nối mạng"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("AuthScreen", "Lỗi gửi OTP", e)
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            errorMessage = "Có lỗi xảy ra: ${e.message}"
                                        }
                                    }
                                }
                            } else {
                                // ĐĂNG KÝ
                                if (password != confirmPassword) {
                                    isLoading = false
                                    errorMessage = "Mật khẩu không khớp!"
                                    return@Button
                                }

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val ok = OtpApi.sendOtp(email)
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            if (ok) {
                                                otpMode = "register"
                                                showOtpScreen = true
                                            } else {
                                                errorMessage = "Không gửi được OTP"
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            errorMessage = "Có lỗi xảy ra: ${e.message}"
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(10.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp),
                        enabled = !isLoading
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF00FFFF),
                                            Color(0xFFFF00FF),
                                            Color(0xFF7B2FF7)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading)
                                CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                            else
                                Text(
                                    if (isLogin) "Đăng nhập" else "Đăng ký",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(
                        onClick = {
                            isLogin = !isLogin
                            errorMessage = null
                            successMessage = null
                            captchaCode = generateCaptcha()
                            captchaInput = ""
                            confirmPassword = ""
                        }
                    ) {
                        Text(
                            if (isLogin) "Chưa có tài khoản? " else "Đã có tài khoản? ",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            if (isLogin) "Đăng ký ngay" else "Đăng nhập",
                            color = Color(0xFF00FFFF),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(it, color = Color(0xFFFF4081), fontSize = 13.sp, textAlign = TextAlign.Center)
                    }

                    successMessage?.let {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(it, color = Color(0xFF00FFFF), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OtpNeonScreen(
    email: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    message: String,
    isLoading: Boolean,
    onVerify: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0014),
                        Color(0xFF160028),
                        Color(0xFF22003E)
                    )
                )
            )
    ) {

        // Back button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.White
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {

            // Header
            Text(
                text = "Melody Player",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFF00FF)
                ),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Text(
                "Xác thực OTP",
                fontSize = 28.sp,
                color = Color(0xFF00FFFF),
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text(
                "Mã OTP đã gửi đến",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Text(
                email,
                color = Color(0xFF00FFFF),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // OTP Input với 6 ô riêng biệt
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                OtpInputBoxes(
                    otpValue = otp,
                    onOtpChange = onOtpChange
                )
            }

            // Error message
            if (message.isNotEmpty()) {
                Text(
                    message,
                    color = Color(0xFFFF5252),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Verify button
            Button(
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(10.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp),
                enabled = !isLoading
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00FFFF),
                                    Color(0xFFFF00FF),
                                    Color(0xFF7B2FF7)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Xác thực",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Resend OTP
            TextButton(
                onClick = {
                    // TODO: Implement resend OTP
                },
                enabled = !isLoading
            ) {
                Text(
                    text = "Gửi lại mã OTP",
                    color = Color(0xFF00FFFF),
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun OtpInputBoxes(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Hiển thị 6 ô phía trên
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(6) { index ->
                OtpDigitBox(
                    digit = otpValue.getOrNull(index)?.toString() ?: "",
                    isFocused = otpValue.length == index
                )
            }
        }

        // TextField trong suốt phủ lên để nhận input
        BasicTextField(
            value = otpValue,
            onValueChange = onOtpChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            textStyle = TextStyle(
                color = Color.Transparent,
                fontSize = 1.sp
            ),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
            decorationBox = { innerTextField ->
                Box {
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean
) {
    val borderBrush = when {
        digit.isNotEmpty() -> Brush.linearGradient(
            colors = listOf(Color(0xFF00FFFF), Color(0xFFFF00FF))
        )
        isFocused -> Brush.linearGradient(
            colors = listOf(Color(0xFF00FFFF), Color(0xFFFF00FF))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF2A2F4F), Color(0xFF2A2F4F))
        )
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1A002E))
            .border(
                width = 2.dp,
                brush = borderBrush,
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

fun generateCaptcha(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..5).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}
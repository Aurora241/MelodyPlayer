package com.example.melodyplayer.auth

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var email by remember { mutableStateOf(sharedPrefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var captchaInput by remember { mutableStateOf("") }
    var captchaCode by remember { mutableStateOf(generateCaptcha()) }

    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var rememberLogin by remember { mutableStateOf(sharedPrefs.getBoolean("remember_login", false)) }

    LaunchedEffect(Unit) {
        if (rememberLogin && auth.currentUser != null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0D0D),
                        Color(0xFF1a1a1a),
                        Color(0xFF0f4d2e)
                    ),
                    startY = 0f,
                    endY = 2000f
                )
            )
    ) {
        // Decorative circles in background
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-100).dp, y = 100.dp)
                .clip(CircleShape)
                .background(Color(0xFF1DB954).copy(alpha = 0.1f))
                .blur(80.dp)
        )

        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = 250.dp, y = 500.dp)
                .clip(CircleShape)
                .background(Color(0xFF1DB954).copy(alpha = 0.15f))
                .blur(100.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Logo với hiệu ứng shadow
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .shadow(20.dp, CircleShape),
                shape = CircleShape,
                color = Color(0xFF1DB954).copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF1DB954).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "App Logo",
                        modifier = Modifier.size(65.dp),
                        tint = Color(0xFF1DB954)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Melody Player",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Đắm chìm trong âm nhạc 🎵",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Form Card với glass morphism effect
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1a1a1a).copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLogin) "Đăng nhập" else "Tạo tài khoản",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = if (isLogin) "Chào mừng bạn quay lại!" else "Tham gia cộng đồng âm nhạc",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Email field với style mới
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = {
                            Text(
                                "Nhập email của bạn",
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF1DB954)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF252525),
                            unfocusedContainerColor = Color(0xFF202020),
                            focusedIndicatorColor = Color(0xFF1DB954),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF1DB954)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = {
                            Text(
                                "Nhập mật khẩu",
                                color = Color.White.copy(alpha = 0.4f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF1DB954)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF252525),
                            unfocusedContainerColor = Color(0xFF202020),
                            focusedIndicatorColor = Color(0xFF1DB954),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF1DB954)
                        )
                    )

                    // Remember me checkbox (only for login)
                    AnimatedVisibility(
                        visible = isLogin,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                        ) {
                            Checkbox(
                                checked = rememberLogin,
                                onCheckedChange = {
                                    rememberLogin = it
                                    sharedPrefs.edit().putBoolean("remember_login", it).apply()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF1DB954),
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = Color.Black
                                )
                            )
                            Text(
                                "Ghi nhớ đăng nhập",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Confirm Password (for registration)
                    AnimatedVisibility(
                        visible = !isLogin,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically()
                    ) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                placeholder = {
                                    Text(
                                        "Xác nhận mật khẩu",
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color(0xFF1DB954)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                        Icon(
                                            if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF252525),
                                    unfocusedContainerColor = Color(0xFF202020),
                                    focusedIndicatorColor = Color(0xFF1DB954),
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF1DB954)
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Captcha section với style mới
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF252525)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    Color(0xFF1DB954).copy(alpha = 0.15f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 20.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = captchaCode,
                                                fontSize = 24.sp,
                                                color = Color(0xFF1DB954),
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 4.sp
                                            )
                                        }

                                        IconButton(
                                            onClick = { captchaCode = generateCaptcha() },
                                            modifier = Modifier
                                                .background(
                                                    Color(0xFF1DB954).copy(alpha = 0.2f),
                                                    CircleShape
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Làm mới",
                                                tint = Color(0xFF1DB954)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = captchaInput,
                                        onValueChange = { captchaInput = it },
                                        placeholder = {
                                            Text(
                                                "Nhập mã xác nhận",
                                                color = Color.White.copy(alpha = 0.4f)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF202020),
                                            unfocusedContainerColor = Color(0xFF202020),
                                            focusedIndicatorColor = Color(0xFF1DB954),
                                            unfocusedIndicatorColor = Color.Transparent,
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White,
                                            cursorColor = Color(0xFF1DB954)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Login/Register Button với gradient
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null
                            successMessage = null

                            if (isLogin) {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            sharedPrefs.edit()
                                                .putString("saved_email", email)
                                                .putBoolean("remember_login", rememberLogin)
                                                .apply()
                                            onLoginSuccess()
                                        } else errorMessage = task.exception?.message
                                    }
                            } else {
                                if (password != confirmPassword) {
                                    isLoading = false
                                    errorMessage = "Mật khẩu xác nhận không khớp!"
                                    return@Button
                                }
                                if (captchaInput != captchaCode) {
                                    isLoading = false
                                    errorMessage = "Mã captcha không đúng!"
                                    captchaCode = generateCaptcha()
                                    captchaInput = ""
                                    return@Button
                                }

                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            successMessage = "Đăng ký thành công! Vui lòng đăng nhập."
                                            isLogin = true
                                            email = ""; password = ""
                                            confirmPassword = ""; captchaInput = ""
                                            captchaCode = generateCaptcha()
                                        } else errorMessage = task.exception?.message
                                    }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(12.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFF1DB954),
                                            Color(0xFF1ed760)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Text(
                                    if (isLogin) "Đăng nhập" else "Tạo tài khoản",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }

                    // Forgot password (only for login)
                    if (isLogin) {
                        TextButton(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    auth.sendPasswordResetEmail(email)
                                        .addOnCompleteListener { task ->
                                            successMessage = if (task.isSuccessful) {
                                                "Đã gửi email đặt lại mật khẩu!"
                                            } else task.exception?.message
                                        }
                                } else errorMessage = "Vui lòng nhập email để khôi phục mật khẩu!"
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                "Quên mật khẩu?",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Error/Success Messages
                    errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF3B30).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF3B30),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    it,
                                    color = Color(0xFFFF3B30),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    successMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1DB954).copy(alpha = 0.15f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF1DB954),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    it,
                                    color = Color(0xFF1DB954),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Divider(
                        color = Color.White.copy(alpha = 0.1f),
                        thickness = 1.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Toggle Login/Register
                    TextButton(
                        onClick = {
                            isLogin = !isLogin
                            errorMessage = null
                            successMessage = null
                        }
                    ) {
                        Text(
                            if (isLogin) "Chưa có tài khoản? " else "Đã có tài khoản? ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                        Text(
                            if (isLogin) "Đăng ký ngay" else "Đăng nhập",
                            color = Color(0xFF1DB954),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

fun generateCaptcha(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..5).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}
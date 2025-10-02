package com.example.melodyplayer.auth

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    // 📌 Lấy email đã lưu + trạng thái ghi nhớ
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

    // ✅ Thêm trạng thái ghi nhớ đăng nhập
    var rememberLogin by remember { mutableStateOf(sharedPrefs.getBoolean("remember_login", false)) }

    // ✅ Nếu trước đó user đã chọn "ghi nhớ" thì vào thẳng app luôn
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
                    listOf(Color(0xFF191414), Color(0xFF1DB954))
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo app
            Image(
                painter = painterResource(id = android.R.drawable.ic_media_play),
                contentDescription = "App Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Melody Player",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Đắm chìm trong âm nhạc 🎵",
                fontSize = 16.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF202020)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isLogin) "Đăng nhập" else "Đăng ký",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Mật khẩu") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        colors = textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ✅ Checkbox ghi nhớ đăng nhập (chỉ hiện khi login)
                    if (isLogin) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = rememberLogin,
                                onCheckedChange = {
                                    rememberLogin = it
                                    sharedPrefs.edit().putBoolean("remember_login", it).apply()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF1DB954),
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Text("Ghi nhớ đăng nhập", color = Color.White)
                        }
                    }

                    // Confirm Password (nếu đăng ký)
                    if (!isLogin) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Xác nhận mật khẩu") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            },
                            colors = textFieldColors()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Captcha
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = captchaCode,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color = Color.Yellow,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            )
                            TextButton(onClick = { captchaCode = generateCaptcha() }) {
                                Text("Đổi mã", color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = captchaInput,
                            onValueChange = { captchaInput = it },
                            placeholder = { Text("Nhập mã captcha") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = textFieldColors()
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 🔹 Nút đăng nhập / đăng ký
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
                            .height(55.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1DB954)
                        )
                    ) {
                        Text(
                            if (isLogin) "Đăng nhập" else "Đăng ký",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // ✅ Loading Indicator
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = Color(0xFF1DB954),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLogin) {
                        TextButton(onClick = {
                            if (email.isNotEmpty()) {
                                auth.sendPasswordResetEmail(email)
                                    .addOnCompleteListener { task ->
                                        successMessage = if (task.isSuccessful) {
                                            "Đã gửi email đặt lại mật khẩu!"
                                        } else task.exception?.message
                                    }
                            } else errorMessage = "Vui lòng nhập email để khôi phục mật khẩu!"
                        }) {
                            Text("Quên mật khẩu?", color = Color.LightGray)
                        }
                    }

                    errorMessage?.let { Text(it, color = Color.Red) }
                    successMessage?.let { Text(it, color = Color.Green) }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { isLogin = !isLogin }) {
                        Text(
                            if (isLogin) "Chưa có tài khoản? Đăng ký" else "Đã có tài khoản? Đăng nhập",
                            color = Color(0xFF1DB954),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// 🎨 Style cho TextField
@Composable
fun textFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = Color(0xFF1DB954),
    unfocusedIndicatorColor = Color.Gray,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedPlaceholderColor = Color.LightGray,
    unfocusedPlaceholderColor = Color.LightGray
)

fun generateCaptcha(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..5).map { chars[Random.nextInt(chars.length)] }.joinToString("")
}

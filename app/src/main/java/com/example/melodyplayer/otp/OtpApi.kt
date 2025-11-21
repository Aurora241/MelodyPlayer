package com.example.melodyplayer.otp

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object OtpApi {
    // ⚠️ LƯU Ý: Thay IP này bằng IP thật của server Node.js bạn (ví dụ IP EC2 hoặc 10.0.2.2 nếu chạy local)
    private const val BASE_URL = "http://3.106.202.66:3000"

    private val client = OkHttpClient()

    // 1. Gửi OTP
    suspend fun sendOtp(email: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply { put("email", email) }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("$BASE_URL/send-otp").post(body).build()

                val response = client.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    // 2. Xác thực OTP
    suspend fun verifyOtp(email: String, otp: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("otp", otp)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url("$BASE_URL/verify-otp").post(body).build()

                val response = client.newCall(request).execute()
                val jsonResponse = JSONObject(response.body?.string() ?: "")
                jsonResponse.optBoolean("success", false)
            } catch (e: Exception) {
                false
            }
        }
    }

    // 3. Đổi mật khẩu (HÀM BẠN ĐANG THIẾU) 👇
    suspend fun resetPassword(email: String, newPass: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("newPassword", newPass)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL/reset-password") // Gọi đúng API server
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val jsonResponse = JSONObject(response.body?.string() ?: "")

                jsonResponse.optBoolean("success", false)
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
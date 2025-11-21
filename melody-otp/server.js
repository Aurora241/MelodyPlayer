import express from "express";
import cors from "cors";
import nodemailer from "nodemailer";
import dotenv from "dotenv";
import admin from "firebase-admin"; // Thư viện mới
import { createRequire } from "module"; // Để đọc file JSON an toàn

dotenv.config();

// --- 1. CẤU HÌNH FIREBASE ADMIN ---
// Sử dụng createRequire để import file JSON (tránh lỗi ES Module trên một số phiên bản Node)
const require = createRequire(import.meta.url);
const serviceAccount = require("./serviceAccountKey.json"); 

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const app = express();
app.use(cors());
app.use(express.json());

// --- 2. CẤU HÌNH GỬI MAIL (Giữ nguyên) ---
const otpStore = {}; // Lưu OTP tạm trong RAM

const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

// --- 3. CÁC API ---

// API 1: Gửi OTP (Giữ nguyên)
app.post("/send-otp", async (req, res) => {
    const { email } = req.body;

    if (!email) return res.status(400).json({ success: false, message: "Missing email" });

    // Tạo OTP 6 số ngẫu nhiên
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    otpStore[email] = otp;

    console.log(`OTP generated for ${email}: ${otp}`); // Log để debug trên server nếu cần

    try {
        await transporter.sendMail({
            from: '"Melody Player Support" <no-reply@melodyplayer.com>', // Tên hiển thị đẹp hơn
            to: email,
            subject: "Mã xác thực OTP - Melody Player",
            text: `Xin chào,\n\nMã OTP xác thực của bạn là: ${otp}\n\nMã này có hiệu lực trong ít phút. Vui lòng không chia sẻ cho người khác.`
        });

        res.json({ success: true, message: "OTP sent" });
    } catch (err) {
        console.error("Error sending email:", err);
        res.status(500).json({ success: false, message: err.message });
    }
});

// API 2: Verify OTP (Giữ nguyên)
app.post("/verify-otp", (req, res) => {
    const { email, otp } = req.body;

    if (!otpStore[email])
        return res.json({ success: false, message: "OTP expired or not found" });

    if (otpStore[email] !== otp)
        return res.json({ success: false, message: "Invalid OTP" });

    // Xác thực thành công -> Xóa OTP để không dùng lại được
    delete otpStore[email];

    return res.json({ success: true, message: "Verified!" });
});

// API 3: Đổi mật khẩu (MỚI THÊM)
app.post("/reset-password", async (req, res) => {
    const { email, newPassword } = req.body;

    if (!email || !newPassword) {
        return res.status(400).json({ success: false, message: "Thiếu email hoặc mật khẩu mới" });
    }

    try {
        // Bước 1: Tìm user trong Firebase bằng email để lấy UID
        const userRecord = await admin.auth().getUserByEmail(email);
        
        // Bước 2: Dùng quyền Admin cập nhật mật khẩu cho UID đó
        await admin.auth().updateUser(userRecord.uid, {
            password: newPassword
        });

        console.log(`Password updated for user: ${email}`);
        res.json({ success: true, message: "Đổi mật khẩu thành công" });

    } catch (error) {
        console.error("Error resetting password:", error);
        // Trả về lỗi để App hiển thị (ví dụ: User không tồn tại, password quá yếu...)
        res.status(500).json({ success: false, error: error.message });
    }
});

// --- 4. CHẠY SERVER ---
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`OTP Server running on port ${PORT}`));
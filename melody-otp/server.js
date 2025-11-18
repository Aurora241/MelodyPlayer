import express from "express";
import cors from "cors";
import nodemailer from "nodemailer";
import dotenv from "dotenv";

dotenv.config();

const app = express();
app.use(cors());
app.use(express.json());

// Lưu OTP tạm
const otpStore = {};

// Gmail transporter
const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

// API gửi OTP
app.post("/send-otp", async (req, res) => {
    const { email } = req.body;

    if (!email) return res.status(400).json({ success: false, message: "Missing email" });

    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    otpStore[email] = otp;

    try {
        await transporter.sendMail({
            from: "Melody Player",
            to: email,
            subject: "Mã OTP xác thực",
            text: `Mã OTP của bạn là: ${otp}`
        });

        res.json({ success: true });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
});

// API verify OTP
app.post("/verify-otp", (req, res) => {
    const { email, otp } = req.body;

    if (!otpStore[email])
        return res.json({ success: false, message: "OTP expired or not found" });

    if (otpStore[email] !== otp)
        return res.json({ success: false, message: "Invalid OTP" });

    delete otpStore[email];

    return res.json({ success: true, message: "Verified!" });
});

// Run server
app.listen(3000, () => console.log("OTP Server running on port 3000"));

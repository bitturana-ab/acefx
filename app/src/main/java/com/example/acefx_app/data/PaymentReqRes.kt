package com.example.acefx_app.data

// req for payment
data class PaymentRequest(
    val amount: Double,
    val projectId: String
)

// response of payment or order with razorpay
data class CreatePaymentResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentData?
)

data class PaymentData(
    val order: RazorpayOrder,
    val payment: PaymentInfo
)

data class RazorpayOrder(
    val id: String,
    val amount: Int,
    val currency: String,
    val receipt: String,
    val payment_capture: Int,
)

data class PaymentInfo(
    val _id: String,
    val amount: Int,
    val orderId: String,
    val status: String,
    val currency: String,
    val clientId:String,
    val projectId : String,
    val email: String,
    val signature: String,
    val paymentId: String,
)

data class VerifyPaymentResponse(
    val success: Boolean,
    val message: String
)

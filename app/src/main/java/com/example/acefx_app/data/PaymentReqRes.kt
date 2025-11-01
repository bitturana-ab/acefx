package com.example.acefx_app.data

// req for payment
data class PaymentRequest(
    val amount: Double,
    val projectId: String
)

// key response but wont use now
//data class GetRazorpayKeyResponse(
//    val key: String
//)
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
    val clientId: String,
    val projectId: String,
    val email: String,
    val signature: String,
    val paymentId: String,
)

// payment info on click on payment invoices
data class PaymentInfoForDetailsRes(
    val success: Boolean,
    val message: String,
    val data: PaymentInfoForDetails
)

data class PaymentInfoForDetails(
    val _id: String,
    val amount: Int,
    val orderId: String,
    val status: String,
    val currency: String,
    val clientId: ClientDetails,
    val projectId: ProjectIdDetails,
    val email: String,
    val signature: String,
    val paymentId: String,
)

data class ClientDetails(
    val name: String,
    val email: String
)

// payment for invoices
data class PaymentInfoForInvoice(
    val _id: String,
    val amount: Int,
    val orderId: String,
    val status: String,
    val currency: String,
    val clientId: String,
    val projectId: ProjectForInvoice,
    val email: String,
    val signature: String,
    val paymentId: String,
)

data class ProjectForInvoice(
    val title: String,
    val description: String,
    val actualAmount: String,

    )

data class VerifyPaymentResponse(
    val success: Boolean,
    val message: String,
    val data: PaymentDataVerify?
)

data class PaymentDataVerify(
    val _id: String,
    val amount: Double,
    val status: String,
    val projectId: ProjectDataVerify?,
    val orderId: String,
    val currency: String,
    val clientId: String,
    val email: String,
    val signature: String,
    val paymentId: String,
)

data class ProjectDataVerify(
    val title: String?
)

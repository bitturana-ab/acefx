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
    val payment: PaymentInfoForPaid
)

data class RazorpayOrder(
    val id: String,
    val amount: Double,
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

    val _id: String?,
    val orderId: String?,
    val fullOrderId: String?,
    val paymentId: String?,
    val fullPaymentId: String?,
    val halfPaymentId: String?,
    val signature: String?,
    val amount: Double?,
    val paidType: String?,
    val halfAmount: Double?,
    val currency: String?,
    val status: String?,
    val email: String?,
    val clientId: ClientDetails,
    val projectId: ProjectIdDetails,
)

data class ClientDetails(
    val name: String,
    val email: String
)

// payment for invoices
data class PaymentInfoForInvoice(

    val _id: String?,
    val clientId: String?,
    val orderId: String?,
    val fullOrderId: String?,
    val fullPaymentId: String?,
    val halfPaymentId: String?,
    val paymentId: String?,
    val signature: String?,
    val amount: Double?,
    val paidType: String?,
    val halfAmount: Double?,
    val currency: String?,
    val status: String?,
    val email: String?,

    val projectId: ProjectForInvoice,
)

data class ProjectForInvoice(
    val title: String,
    val description: String?,
    val deadline: String,
    val expectedAmount: String,
    val actualAmount: String,
    val status: String
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

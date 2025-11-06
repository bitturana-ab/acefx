package com.example.acefx_app.data

import java.util.Objects

// post create project response after creating project by client
// when creating project res will send these
data class ProjectResponse(
    val message: String,
    val project: ProjectData,
)

// get projects of array
data class ProjectsResponse(
    val success: Boolean,
    val message: String,
    val data: List<ProjectItem>
)

// get or fetch projects and save
data class ProjectItem(
    val _id: String,
    val title: String,
    val status: String
)

// this is before req the project or projects for creating project
data class ProjectRequest(
    val title: String,
    val description: String,
    val dataLink: String,
    val attachLink: String,
    val deadline: String,      // yyyy-MM-dd format
    val expectedAmount: Double
)

// get project [ also paymentId ] res by user or id
data class ProjectDetailResponse(
    val success: Boolean,
    val message: String,
    val data: ProjectDataByForPaid
)

// project details and payment paid or what
// new update like schema
data class PaymentInfoForPaid(
    val _id: String?,
    val clientId: String?,
    val projectId: String?,
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
)

// details of project
data class ProjectDataByForPaid(
    val _id: String,
    val title: String,
    val description: String,
    val dataLink: String,
    val attachLink: String,
    val deadline: String,
    val expectedAmount: Double,
    val clientId: String?,
    val editorId: String?,
    val status: String,
    val invoiceId: String?,
    val paymentId: PaymentInfoForPaid?,
    val actualAmount: Double,
    val deliverableUrl: String?
)

data class ProjectDataByProject(
    val _id: String,
    val title: String,
    val description: String,
    val dataLink: String,
    val attachLink: String,
    val deadline: String,
    val expectedAmount: Double,
    val clientId: String?,
    val editorId: String?,
    val status: String,
    val invoiceId: InvoiceDataByProject?,
    val actualAmount: Double,
    val deliverableUrl: String?
)

data class InvoiceDataByProject(
    val _id: String,
    val projectId: String,
    val clientId: String,
    val amount: Double,
    val currency: String = "INR",
    val paid: Boolean = false,
    val completedTime: String,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null
)

// for someone
data class ProjectData(
    val _id: String,
    val title: String,
    val description: String,
    val dataLink: String,
    val attachLink: String,
    val deadline: String,
    val expectedAmount: Double,
    val clientId: String?,
    val editorId: String?,
    val status: String,
    val invoiceId: InvoiceData?,
    val actualAmount: Double,
    val deliverableUrl: String?
)

// for invoice adapter
data class InvoiceModel(
    val projectName: String,
    val clientName: String,
    val date: String,
    val amount: Double,
    val status: String
)

// get all invoices
data class AllInvoices(
    val success: Boolean,
    val message: String,
    val data: List<InvoiceData>
)

// get payment invoices
data class GetPaymentById(
    val success: Boolean,
    val message: String,
    val data: List<PaymentInfoForInvoice>
)

// get invoice
data class InvoiceData(
    val _id: String,
    val projectId: ProjectIdDetails,
    val clientId: String,
    val amount: Double,
    val currency: String = "INR",
    val paid: Boolean = false,
    val completedTime: String,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null
)

// invoice project title
data class ProjectIdDetails(
    val _id: String,
    val dataLink: String?,
    val attachLink: String?,
    val title: String,
    val deliverableUrl: String?,
    val deadline: String?,
    val clientId: String?,
    val editorId: String?,
    val status: String,
    val invoiceId: String?,
    val description: String?,
    val expectedAmount: Double?,
    val actualAmount: Double?,
    val completedTime: String?
)
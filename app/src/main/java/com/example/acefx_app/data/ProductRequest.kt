package com.example.acefx_app.data

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

// get project res by user or id
data class ProjectDetailResponse(
    val success: Boolean,
    val message: String,
    val data: ProjectData
)
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
data class GetInvoiceById(
    val success: Boolean,
    val message: String,
    val data: InvoiceSchema
)
// single invoice schema
data class InvoiceSchema(
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
    val deadline: String,
    val clientId: String?,
    val editorId: String?,
    val status: String,
    val invoiceId: String?,
    val description: String?,
    val expectedAmount: Double?,
    val actualAmount: Double?,
    val completedTime:String?
)
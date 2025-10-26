package com.example.acefx_app.data

// post create project response after creating project by client
// when creating project res will send these
data class ProjectResponse(
    val message: String,
    val project: ProjectData,
    val invoice: InvoiceData
)
// get projects of array
data class ProjectsResponse(
    val message: String,
    val projects: List<ProjectItem>
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

// get invoice
data class InvoiceData(
    val _id: String,
    val projectId: String,
    val clientId: String,
    val amount: Double,
    val currency: String = "INR",
    val paid: Boolean = false,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null
)
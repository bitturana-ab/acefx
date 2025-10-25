package com.example.acefx_app.data

data class ProjectResponse(
    val message: String,
    val project: ProjectData,
    val invoice: InvoiceData
)
data class ProjectRequest(
    val title: String,
    val description: String,
    val dataLink: String,
    val attachLink: String,
    val deadline: String,      // yyyy-MM-dd format
    val expectedAmount: Double
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
    val invoiceId: String?
)

data class InvoiceData(
    val _id: String,
    val projectId: String,
    val userId: String,
    val amount: Double
)

package com.example.acefx_app.retrofitServices

import com.example.acefx_app.data.ChatListResponse
import com.example.acefx_app.data.ChatMessageRequest
import com.example.acefx_app.data.ChatMessageResponse
import com.example.acefx_app.data.CreatePaymentResponse
import com.example.acefx_app.data.GetPaymentById
import com.example.acefx_app.data.PaymentInfoForDetailsRes
import com.example.acefx_app.data.PaymentRequest
import com.example.acefx_app.data.ProjectDetailResponse
import com.example.acefx_app.data.ProjectRequest
import com.example.acefx_app.data.ProjectResponse
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.data.UserDetailsResponse
import com.example.acefx_app.data.VerifyPaymentResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path


interface ApiService {

    @POST("api/auth/request-otp")
    fun sendOtp(@Body body: Map<String, String>): Call<Map<String, Any>>

    @POST("api/auth/verify-otp")
    fun verifyOtp(@Body body: Map<String, String>): Call<Map<String, Any>>

    // get user update or details
    @GET("api/auth")
    fun getUserProfile(
        @Header("Authorization") token: String
    ): Call<UserDetailsResponse>

    //    not available
    @GET("dashboard")
    fun getDashboard(@Header("Authorization") token: String): Call<Map<String, Any>>

    //    for update client fields
    @POST("api/auth/update-user") // update extras info user endpoint
    fun updateUser(
        @Body body: Map<String, String>,
        @Header("Authorization") token: String
    ): Call<Map<String, Any>>

    // get projects  of  user or client
    @GET("api/projects/my-projects")
    fun getClientProjects(
        @Header("Authorization") token: String
    ): Call<ProjectsResponse>

    //    add project by client and handle json response
    @POST("api/projects")
    fun createProject(
        @Header("Authorization") token: String,
        @Body projectData: ProjectRequest
    ): Call<ProjectResponse>

    @GET("api/projects/{id}")
    fun getProjectById(
        @Header("Authorization") authToken: String,
        @Path("id") id: String
    ): Call<ProjectDetailResponse>

    // chat api
    /** Send chat message */
    @POST("api/chat/send")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: ChatMessageRequest
    ): Response<ChatMessageResponse>

    /** Get all chat messages for a specific client */
    @GET("api/chat/client/{clientId}")
    suspend fun getChatHistory(
        @Header("Authorization") token: String
    ): Response<ChatListResponse>

    // invoice get and post methods are here
//    @GET("api/invoices/my-invoices")
//    fun getMyInvoices(
//        @Header("Authorization") token: String
//    ): Call<AllInvoices>api/invoices/my-invoices")

    @GET("api/payment/my-payments")
    fun getMyPayments(
        @Header("Authorization") token: String
    ): Call<GetPaymentById>

    // get invoice by id
    @GET("api/payment/project/{paymentId}")
    fun getPaymentDetails(
        @Header("Authorization") token: String,
        @Path("paymentId") paymentId: String
    ): Call<PaymentInfoForDetailsRes>

    // razor pay
    @POST("api/payment/create-order")
    fun createPaymentOrder(
        @Header("Authorization") token: String,
        @Body paymentData: PaymentRequest
    ): Call<CreatePaymentResponse>

    @POST("api/payment/verify")
    fun verifyPayment(
        @Header("Authorization") token: String,
        @Body body: Map<String, String>
    ): Call<VerifyPaymentResponse>
    // get key from backend
    @GET("/api/payment/key")
    fun getRazorpayKey(
        @Header("Authorization") token: String
    ): Call<Map<String, String>>



}

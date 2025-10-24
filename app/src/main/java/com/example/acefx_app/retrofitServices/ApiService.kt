package com.example.acefx_app.retrofitServices

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/request-otp")
    fun sendOtp(@Body body: Map<String, String>): Call<Map<String, Any>>

    @POST("api/auth/verify-otp")
    fun verifyOtp(@Body body: Map<String, String>): Call<Map<String, Any>>

//    not available
    @GET("dashboard")
    fun getDashboard(@Header("Authorization") token: String): Call<Map<String, Any>>

//    for update client fields
    @POST("api/auth/update-user") // replace with your actual endpoint
    fun updateUser(@Body body: Map<String, String>): Call<Map<String, Any>>
}

package com.example.acefx_app.retrofitServices

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("api/auth/request-otp")
    fun sendOtp(@Body body: Map<String, String>): Call<Map<String, Any>>

    @POST("api/auth/verify-otp")
    fun verifyOtp(@Body body: Map<String, String>): Call<Map<String, Any>>

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
    ): Call<List<Map<String, Any>>>

}

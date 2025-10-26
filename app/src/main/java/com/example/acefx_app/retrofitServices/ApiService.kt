package com.example.acefx_app.retrofitServices

import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.data.ProjectItem
import com.example.acefx_app.data.ProjectRequest
import com.example.acefx_app.data.ProjectResponse
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.data.UserDetailsResponse
import retrofit2.Call
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
    ):Call<UserDetailsResponse>

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
    ): Call<ProjectData>


}

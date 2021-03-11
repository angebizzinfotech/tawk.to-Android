package com.app.vedicstudents.Retrofit

import com.app.githubuserrepo.model.ProfileResponse
import com.app.githubuserrepo.model.UserListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @GET("users")
    fun getUserlists(
        @Query("since") since: String,
    ): Call<UserListResponse>?


    @GET("users/{username}")
    fun getProfile(
        @Path("username") username: String,
    ): Call<ProfileResponse>?


}
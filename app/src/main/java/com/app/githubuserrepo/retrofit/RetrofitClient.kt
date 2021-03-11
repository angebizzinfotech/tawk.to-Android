package com.app.vedicstudents.Retrofit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

    const val BASE_URL = "https://api.github.com/"

 class RetrofitClient {

     companion object {



         public fun getClient(): Retrofit? {

             var retrofit: Retrofit? = null
             var retrofit2: Retrofit? = null

             /*Gson gson = new GsonBuilder()
                     .setLenient()
                     .create();*/
             val okHttpClient = OkHttpClient.Builder()
                 .readTimeout(180, TimeUnit.SECONDS)
                 .connectTimeout(180, TimeUnit.SECONDS)
                 .build()

             retrofit = retrofit2!!.newBuilder()
                 .baseUrl(BASE_URL)
                 .addConverterFactory(GsonConverterFactory.create())
                 .client(okHttpClient)
                 .build()
             return retrofit
         }

     }



}
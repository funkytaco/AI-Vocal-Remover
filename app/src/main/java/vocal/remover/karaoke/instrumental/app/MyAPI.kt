package vocal.remover.karaoke.instrumental.app

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import vocal.remover.karaoke.instrumental.app.models.AudioResultResponse
import vocal.remover.karaoke.instrumental.app.models.UploadResponse
import vocal.remover.karaoke.instrumental.app.utils_java.AppUtils.getUnsafeOkHttpClient
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession


interface MyAPI {

    @Multipart
//    @POST("FileUploadServlet")
    @POST("FileTest")
    fun uploadMp3(
            @Part image: MultipartBody.Part,
            @Part("desc") desc: RequestBody
    ): Call<UploadResponse>


    @POST("ProcessM")
    fun processMp3(@Query("file_name") fileName: String?): Call<AudioResultResponse>


    companion object {
        operator fun invoke(): MyAPI {
            return Retrofit.Builder()
                    .baseUrl("https://aivocalremover.com/")
                  //  .client(okHttpClient)
                    .client(getUnsafeOkHttpClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MyAPI::class.java)
        }

        val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.MINUTES)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .hostnameVerifier { s: String, sslSession: SSLSession ->
                    true
                }
                .build()
    }

}
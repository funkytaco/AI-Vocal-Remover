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
import java.util.concurrent.TimeUnit


interface MyAPI {

    @Multipart
//    @POST("FileUploadServlet")
    @POST("FileTest")
    fun uploadImage(
            @Part image: MultipartBody.Part,
            @Part("desc") desc: RequestBody
    ): Call<UploadResponse>



    @GET("ProcessM")
    fun processMp3(@Query("file_name") fileName: String?): Call<AudioResultResponse>



    companion object {
        operator fun invoke(): MyAPI {
            return Retrofit.Builder()
                    .baseUrl("http://161.35.71.36/")
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MyAPI::class.java)
        }

        val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
    }


}
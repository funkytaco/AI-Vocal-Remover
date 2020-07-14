package vocal.remover.karaoke.instrumental.app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface MyAPI {

    @Multipart
//    @POST("FileUploadServlet")
    @POST("FileTest")
    fun uploadImage(
            @Part image: MultipartBody.Part,
            @Part("desc") desc: RequestBody
    ): Call<UploadResponse>



    @GET("ProcessM")
    fun processMp3(@Query("file_name") fileName: String?): Call<ProcessMp3Response>



    companion object {
        operator fun invoke(): MyAPI {
            return Retrofit.Builder()
                    .baseUrl("http://161.35.71.36/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MyAPI::class.java)
        }
    }
}
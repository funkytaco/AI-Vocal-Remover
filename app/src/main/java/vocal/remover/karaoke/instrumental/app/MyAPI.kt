package vocal.remover.karaoke.instrumental.app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MyAPI {

    @Multipart
    @POST("FileUploadServlet")
    fun uploadImage(
            @Part image: MultipartBody.Part,
            @Part("desc") desc: RequestBody
    ): Call<UploadResponse>

    companion object {
        operator fun invoke(): MyAPI {
            return Retrofit.Builder()
                    .baseUrl("http://46.101.180.73/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(MyAPI::class.java)
        }
    }
}
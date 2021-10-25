package project.capstone6.acne_diagnosis.apis

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadAPis {
    @Multipart
    @POST("upload")
    fun uploadImage(
        @Part part: MultipartBody.Part?,
        @Part("imgdata") requestBody: RequestBody?
    ): Call<RequestBody?>?
}
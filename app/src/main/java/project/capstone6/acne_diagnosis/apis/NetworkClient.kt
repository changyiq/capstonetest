package project.capstone6.acne_diagnosis.apis

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkClient {
    var retrofit: Retrofit? = null
        get() {
            val okHttpClient = OkHttpClient.Builder().build()
            if (field == null) {
                field = Retrofit.Builder().baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build()
            }
            return field
        }
        private set
    private const val BASE_URL = "https://localhost:5001;http://localhost:5000"
}
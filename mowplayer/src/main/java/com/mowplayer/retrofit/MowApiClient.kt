package com.mowplayer.retrofit

import com.google.gson.Gson
import com.mowplayer.utils.MowConstants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MowApiClient {

    private var retrofit: Retrofit? = null
    private val builder = Retrofit.Builder()
            .baseUrl(MowConstants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(Gson()))

    fun <S> createService(serviceClass: Class<S>): S {
        try {
//            httpClient.sslSocketFactory(getSSLSocketFactory())
//            okHttpClient.hostnameVerifier { hostname, session -> true }

//            httpClient.addInterceptor(Interceptor { chain ->
//                val original = chain.request()
//                // Request customization: add request headers
//                val requestBuilder = original.newBuilder()
//                        .header("mow-referer", MowConstants.MOW_REFERER)
//                        .method(original.method, original.body)
//
//                if (requestBuilder != null) {
//                    val request = requestBuilder.build()
//                    return@Interceptor chain.proceed(request)
//                }
//                null
//            })
//            httpClient.addNetworkInterceptor { chain ->
//                val request = chain.request().newBuilder().addHeader("Connection", "close").build()
//                chain.proceed(request)
//            }

            retrofit = builder
                    .client(getUnsafeOkHttpClient()!!)
                    .build()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return retrofit!!.create(serviceClass)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient? {
        return try {
            // Create a trust manager that does not validate certificate chains
//            val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
//                override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) = Unit
//
//                override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) = Unit
//
//                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
//            })
//
//            // Install the all-trusting trust manager
//            val sslContext = SSLContext.getInstance("SSL")
//            sslContext.init(null, trustAllCerts, SecureRandom())
//
//            // Create an ssl socket factory with our all-trusting manager
//            val sslSocketFactory = sslContext.socketFactory

            val builder: OkHttpClient.Builder = OkHttpClient.Builder()

            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY

            builder.addInterceptor(interceptor)
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

//            builder.sslSocketFactory(sslSocketFactory, (trustAllCerts[0] as X509TrustManager))
            builder.hostnameVerifier { hostname, session -> true }

//            builder.addInterceptor(Interceptor.invoke {
//                val original = it.request()
//                // Request customization: add request headers
//                val requestBuilder = original.newBuilder()
//                        .header("mow-referer", MowConstants.MOW_REFERER)
//                        .method(original.method, original.body)
//
//                if (requestBuilder != null) {
//                    val request = requestBuilder.build()
//                    return@invoke it.proceed(request)
//                }
//            })

//            builder.addInterceptor(Interceptor { chain ->
//                val original = chain.request()
//                // Request customization: add request headers
//                val requestBuilder = original.newBuilder()
//                        .header("mow-referer", MowConstants.MOW_REFERER)
//                        .method(original.method, original.body)
//
//                if (requestBuilder != null) {
//                    val request = requestBuilder.build()
//                    return@Interceptor chain.proceed(request)
//                }
//                null
//            })

//            builder.addNetworkInterceptor { chain ->
//                val request = chain.request().newBuilder().addHeader("Connection", "close").build()
//                chain.proceed(request)
//            }

            builder.build()
        } catch (e: java.lang.Exception) {
            throw java.lang.RuntimeException(e)
        }
    }
}

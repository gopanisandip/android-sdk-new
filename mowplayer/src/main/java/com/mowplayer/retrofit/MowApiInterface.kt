package com.mowplayer.retrofit

import com.mowplayer.models.MediaModel
import com.mowplayer.models.RelatedVideoModel
import com.mowplayer.models.audio.MainEvent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface MowApiInterface {

    @FormUrlEncoded
    @POST("config")
    fun getMediaFiles(
            @Field("code") code: String
    ): Call<MediaModel>

    @GET("related_new")
    fun getRelatedVideo(@Header("Authorization") authorization: String,
                        @Header("x-video-data1") data2: String,
                        @Header("mow-referer") data1: String,
                        @Query("player_type") player_type: String): Call<List<RelatedVideoModel>>

    @Headers("Content-Type:application/json")
    @POST("statistics/update")
    fun trackAds(@Header("Authorization") authorization: String,
                 @Header("x-video-data1") data1: String,
                 @Header("x-video-data2") data2: String,
                 @Header("x-video-data3") data3: String, @Body trackingParameterModel: HashMap<String, String>
    ): Call<ResponseBody>

    // Audio API
    @GET("config/{code}")
    fun getVideoConfiguration(@Path("code") code: String): Call<ResponseBody>

    // Audio API
    @FormUrlEncoded
    @POST("config")
    fun getAudioConfiguration(
            @Field("code") code: String,
            @Field("client") client: String,
            @Field("type") type: String
    ): Call<ResponseBody>

    @Headers("Content-Type:application/json")
    @POST("statistics/update")
    fun tracking(@Header("Authorization") authorization: String,
                 @Header("mow-referer") mowReferer: String,
                 @Header("x-video-data1") data1: Int,
                 @Header("x-video-data2") data2: Long,
                 @Header("x-video-data3") data3: String, @Body trackingParameterModel: MainEvent
    ): Call<ResponseBody>

    @FormUrlEncoded
    @POST("text-to-speech")
    fun textToSpeech(@Header("mow-referer") mowReferer: String,
                     @Header("mow-data3") code: String,
                     @Field("id") id: String,
                     @Field("content") content: String
    ): Call<ResponseBody>
}
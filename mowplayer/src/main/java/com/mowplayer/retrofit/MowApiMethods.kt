package com.mowplayer.retrofit

import com.mowplayer.callbacks.*
import com.mowplayer.models.MediaModel
import com.mowplayer.models.RelatedVideoModel
import com.mowplayer.models.audio.MainEvent
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class MowApiMethods {

    companion object {
        fun getMediaFiles(
                mVideoFilesCallback: VideoFilesCallback<MediaModel>, mMowApiInterface: MowApiInterface, videoCode: String
        ) {
            mMowApiInterface.getMediaFiles(videoCode)
                    .enqueue(object : Callback<MediaModel> {
                        override fun onResponse(call: Call<MediaModel>, response: Response<MediaModel>) {
                            mVideoFilesCallback.onVideoFilesReceived(response)
                        }

                        override fun onFailure(call: Call<MediaModel>, t: Throwable) {
                            mVideoFilesCallback.onVideoFilesFailure()
                        }
                    })
        }

        fun getRelatedVideos(
                mRelatedVideosCallback: RelatedVideosCallback<List<RelatedVideoModel>>, mMowApiInterface: MowApiInterface, authToken: String, mediaId: Long, mowReferer: String
        ) {
            mMowApiInterface.getRelatedVideo(authToken, mediaId.toString(), mowReferer, "video")
                    .enqueue(object : Callback<List<RelatedVideoModel>> {
                        override fun onResponse(call: Call<List<RelatedVideoModel>>, response: Response<List<RelatedVideoModel>>) {
                            mRelatedVideosCallback.onRelatedVideosReceived(response)
                        }

                        override fun onFailure(call: Call<List<RelatedVideoModel>>, t: Throwable) {
                            mRelatedVideosCallback.onRelatedVideosFailure()
                        }
                    })
        }


        fun trackAds(
                mTrackAdsCallback: TrackAdsCallback<ResponseBody>, mMowApiInterface: MowApiInterface, authorization: String, mAdId: String, randomNumber: String,
                videoCode: String, trackingParameterModel: HashMap<String, String>
        ) {
            mMowApiInterface.trackAds(authorization, mAdId, randomNumber, videoCode, trackingParameterModel)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            mTrackAdsCallback.onTrackAdResponse(response)
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            mTrackAdsCallback.onTrackAdFailure()
                        }
                    })
        }

        // Video API call
        fun getVideoConfiguration(
                APICallback: APICallback<ResponseBody>, mMowApiInterface: MowApiInterface, videoCode: String
        ) {
            mMowApiInterface.getVideoConfiguration(videoCode)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            APICallback.onAPISuccess(response.body()!!)
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            APICallback.onAPIFailure(t)
                        }
                    })
        }

        // Audio API call
        fun getAudioConfiguration(
                APICallback: APICallback<ResponseBody>, mMowApiInterface: MowApiInterface, videoCode: String, client: String, type: String
        ) {
            mMowApiInterface.getAudioConfiguration(videoCode, client, type)
                    .enqueue(object : Callback<ResponseBody> {

                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            APICallback.onAPISuccess(response.body()!!)
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            APICallback.onAPIFailure(t)
                        }
                    })
        }

        // TrackerManager API call
        fun startTracking(
                mMowApiInterface: MowApiInterface, authorization: String, mowReferer: String,
                idNowPlaying: Int, randomNumber: Long, code: String, trackingParameterModel: MainEvent
        ) {
            mMowApiInterface.tracking(authorization, mowReferer, idNowPlaying, randomNumber, code, trackingParameterModel)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {

                        }
                    })
        }

        // TrackerManager API call
        fun textToSpeech(
                readerAPICallback: ReaderAPICallback<ResponseBody>, mMowApiInterface: MowApiInterface,
                mowReferer: String, code: String, id: String, content: String
        ) {
            mMowApiInterface.textToSpeech(mowReferer, code, id, content)
                    .enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                            readerAPICallback.onReaderAPISuccess(response.body()!!)
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            readerAPICallback.onReaderAPIFailure(t)
                        }
                    })
        }
    }
}
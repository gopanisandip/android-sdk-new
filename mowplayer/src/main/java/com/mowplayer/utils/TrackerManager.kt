package com.mowplayer.utils

import android.os.Handler
import android.widget.SeekBar
import com.mowplayer.models.audio.*
import com.mowplayer.retrofit.MowApiInterface
import com.mowplayer.retrofit.MowApiMethods
import java.util.*

class TrackerManager {

    init {

    }

    companion object {

        lateinit var mowConfig: MOWConfig
        lateinit var mowApiInterface: MowApiInterface
        lateinit var code: String
        lateinit var mowReferer: String
        lateinit var media: Media
        lateinit var mowAds: MOWAds
        private var randomNumber: Long = 0
        lateinit var runnable: Runnable
        val handler = Handler()

        fun configTracker(mowApiInterface: MowApiInterface, mowConfig: MOWConfig, code: String, mowReferer: String) {
            this.mowApiInterface = mowApiInterface
            this.mowConfig = mowConfig
            this.code = code
            this.mowReferer = mowReferer
            this.randomNumber = Random().nextLong()

            runnable = Runnable {

            }
        }

        fun updateCurrentMedai(media: Media) {
            this.media = media
        }

        fun updateCurrentAds(mowAds: MOWAds) {
            this.mowAds = mowAds
        }

        fun trackProcess(mowConfig: MOWConfig, seekBarAudio: SeekBar) {
            runnable = object : Runnable {
                override fun run() {
                    handler.postDelayed(this, mowConfig.tracking!!.throttle)
                    val mainEvent = MainEvent()
                    mainEvent.event.video_progress = (seekBarAudio.progress / 1000).toDouble()
                    MowApiMethods.startTracking(mowApiInterface, mowConfig.auth!!, mowReferer, media.id!!, randomNumber, code, mainEvent)
                }
            }

            handler.postDelayed(runnable, mowConfig.tracking!!.throttle)
        }

        fun trackFirstPlay(type: String, isLive: Boolean, isStream: Boolean) {
            val mainEvent = MainEvent()
            val firstPlay = FirstPlay()
            firstPlay.type = type
            firstPlay.isLive = isLive
            firstPlay.isStream = isStream

            mainEvent.event.first_play = firstPlay

            MowApiMethods.startTracking(mowApiInterface, mowConfig.auth!!, mowReferer, media.id!!, randomNumber, code, mainEvent)
        }

        fun trackAdRequest(id: Int, adFrom: String, adPriority: String) {
            val mainEvent = MainEvent()
            val adRequestArray: ArrayList<AdRequest> = arrayListOf()
            val adRequest = AdRequest()
            adRequest.id = id
            adRequest.data2 = randomNumber
            adRequest.ad_from = adFrom
            adRequest.ad_priority = adPriority
            adRequestArray.add(adRequest)
            mainEvent.event.ad_request = adRequestArray

            MowApiMethods.startTracking(mowApiInterface, mowConfig.auth!!, mowReferer, mowAds.id, randomNumber, code, mainEvent)
        }

        fun trackAdImpression(id: Int, adFrom: String, adPriority: String) {
            val mainEvent = MainEvent()
            val adImpressionArray: ArrayList<AdImpression> = arrayListOf()
            val adImpression = AdImpression()
            adImpression.id = id
            adImpression.data2 = randomNumber
            adImpression.ad_from = adFrom
            adImpression.ad_priority = adPriority
            adImpressionArray.add(adImpression)
            mainEvent.event.ad_impression = adImpressionArray

            MowApiMethods.startTracking(mowApiInterface, mowConfig.auth!!, mowReferer, mowAds.id, randomNumber, code, mainEvent)
        }

        fun resumeTracking(mowConfig: MOWConfig) {
            handler.postDelayed(runnable, mowConfig.tracking!!.throttle)
        }

        fun stopTracking() {
            handler.removeCallbacks(runnable)
        }
    }
}
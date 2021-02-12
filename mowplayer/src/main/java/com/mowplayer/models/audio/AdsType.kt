package com.mowplayer.models.audio

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

class AdsType(adsObject: JSONObject) {

    @SerializedName("pre_roll_check_priority")
    var preRollCheckPriority: Boolean? = false
    var offsetClock: OffsetClock? = null
    var marketplace: List<MOWAds> = emptyList()
    var publisher: List<MOWAds> = emptyList()

    init {
        if (adsObject.has("pre_roll_check_priority")) {
            preRollCheckPriority = adsObject.getBoolean("pre_roll_check_priority")
        }

        if (adsObject.has("offsetClock")) {
            offsetClock = OffsetClock(adsObject.getJSONObject("offsetClock"))
        }

        val marketplaceList: ArrayList<MOWAds> = ArrayList()
        if (adsObject.has("marketplace")) {
            for (i in 0 until adsObject.getJSONArray("marketplace").length()) {
                val data = MOWAds(adsObject.getJSONArray("marketplace").getJSONObject(i), "marketplace")
                marketplaceList.add(data)
            }
            marketplace = marketplaceList
        }

        val publisherList: ArrayList<MOWAds> = ArrayList()
        if (adsObject.has("publisher")) {
            for (i in 0 until adsObject.getJSONArray("publisher").length()) {
                val data = MOWAds(adsObject.getJSONArray("publisher").getJSONObject(i), "publisher")
                publisherList.add(data)
            }
            publisher = publisherList
        }
    }

    // Custom Functions
    fun processDuplicateAds(durationSeconds: Int) {

        converTimeFromDuration(durationSeconds)

//        var seen = Set<String>()
//        //var uniqueMAds = [MowAd]()
//        for ad in marketplace {
//            let dateFormatter = DateFormatter ()
//            //dateFormatter.timeZone = TimeZone(identifier: "UTC")!
//            dateFormatter.dateFormat = "dd/MM/yyy HH:mm:ss"
//            if !seen.contains(ad.time) {
//                //uniqueMAds.append(ad)
//                seen.insert(ad.time)
//            }
//            else if ad.startDateTime != nil,
//            !seen.contains(dateFormatter.string(from: ad. startDateTime !))
//            {
//                //uniqueMAds.append(ad)
//                seen.insert(ad.time)
//            }
//            else {
//            ad.isUsedOrExpired = true
//        }
//        }
//
//        seen = Set<String>()
//        //var uniquePAds = [MowAd]()
//        for ad in publisher {
//            let dateFormatter = DateFormatter ()
//            //dateFormatter.timeZone = TimeZone(identifier: "UTC")!
//            dateFormatter.dateFormat = "dd/MM/yyy HH:mm:ss"
//            if !seen.contains(ad.time) {
//                //uniquePAds.append(ad)
//                seen.insert(ad.time)
//            }
//            else if ad.startDateTime != nil,
//            !seen.contains(dateFormatter.string(from: ad. startDateTime !))
//            {
//                //uniquePAds.append(ad)
//                seen.insert(ad.time)
//            } else {
//            ad.isUsedOrExpired = true
//        }
    }

    fun converTimeFromDuration(durationSeconds: Int) {
        for (marketplace in marketplace) {
            if (marketplace.time.contains("%")) {
                val time = marketplace.time.replace("%", "").toFloat()
                val actualTime = (time * durationSeconds.toFloat()) / 100.0
                marketplace.time = actualTime.toInt().toString()
            }
        }

        for (publisher in publisher) {
            if (publisher.time.contains("%")) {
                val time = publisher.time.replace("%", "").toFloat()
                val actualTime = (time * durationSeconds.toFloat()) / 100.0
                publisher.time = actualTime.toInt().toString()
            }
        }
    }

    fun getAdsFor(time: String = "0", priority: String = "m"): MOWAds? {
        var adsToPlay: MOWAds? = null

        val filteredMarketPlaceAds = getUnusedAds(marketplace, time)
        val filteredPublisherAds = getUnusedAds(publisher, time)

        if (priority == "p" && filteredPublisherAds.isNotEmpty()) {
            adsToPlay = filteredMarketPlaceAds.first()
        } else if (filteredMarketPlaceAds.isNotEmpty()) {
            adsToPlay = filteredMarketPlaceAds.first()
        }

        return adsToPlay
    }

    fun getUnusedAds(list: List<MOWAds>?, time: String): List<MOWAds> {
        val filtedData = ArrayList<MOWAds>()

        if (list.isNullOrEmpty()) {
            return filtedData
        }

        for (ad in list) {
            if (!ad.isUsedOrExpired && ad.time == time) {
                filtedData.add(ad)
            }
        }
        return filtedData
    }

//    fun getAdsForTime(time: String = "0", priority: String = "m"): MOWAds {
//        val adsToPlay: MOWAds? = null
//        var loadedFrom: String = "marketplace"
//
//        val filteredPublisherAds = publisher.filter {
//
//        }
//
//        return adsToPlay
//    }
}

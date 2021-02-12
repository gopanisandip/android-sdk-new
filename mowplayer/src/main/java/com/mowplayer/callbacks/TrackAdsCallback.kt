package com.mowplayer.callbacks

import retrofit2.Response

interface TrackAdsCallback<T> {

    fun onTrackAdResponse(response: Response<T>)
    fun onTrackAdFailure()

}
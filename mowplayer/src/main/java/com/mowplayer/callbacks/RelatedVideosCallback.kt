package com.mowplayer.callbacks

import retrofit2.Response

interface RelatedVideosCallback<T> {

    fun onRelatedVideosReceived(response: Response<T>)
    fun onRelatedVideosFailure()

}
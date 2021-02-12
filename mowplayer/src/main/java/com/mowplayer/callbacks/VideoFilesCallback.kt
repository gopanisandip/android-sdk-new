package com.mowplayer.callbacks

import retrofit2.Response

interface VideoFilesCallback<T> {

    fun onVideoFilesReceived(response: Response<T>)
    fun onVideoFilesFailure()

}
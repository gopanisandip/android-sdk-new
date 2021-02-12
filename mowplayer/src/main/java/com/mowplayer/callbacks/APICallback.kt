package com.mowplayer.callbacks

import okhttp3.ResponseBody

interface APICallback<T> {

    fun onAPISuccess(responseBody: ResponseBody)
    fun onAPIFailure(throwable: Throwable)

}
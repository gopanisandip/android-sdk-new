package com.mowplayer.callbacks

import okhttp3.ResponseBody

interface ReaderAPICallback<T> {

    fun onReaderAPISuccess(responseBody: ResponseBody)
    fun onReaderAPIFailure(throwable: Throwable)

}
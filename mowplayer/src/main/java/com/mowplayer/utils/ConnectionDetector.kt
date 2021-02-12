package com.mowplayer.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class ConnectionDetector(val context: Context) {

    fun isConnectingToInternet():Boolean{
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (true) {
            val info = connectivity.allNetworkInfo
            if (info != null)
                for (i in info.indices)
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }

        }
        return false
    }
}

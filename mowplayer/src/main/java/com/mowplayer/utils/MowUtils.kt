package com.mowplayer.utils

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mowplayer.R
import retrofit2.Response
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class MowUtils {
    companion object {

        var dialog: Dialog? = null

        @SuppressLint("StaticFieldLeak")
        var hud: CustomProgressDialog? = null

        fun showSnackbar(view: View, message: String) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
        }

        fun showShortToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }

        fun showLog(tag: String, message: String) {
            val Log = Logger.getLogger(tag)
            Log.warning(message)
        }

        fun isConnectToInternet(context: Context): Boolean {
            val connectionDetector = ConnectionDetector(context)
            return connectionDetector.isConnectingToInternet()
        }


        fun showProgress(context: Context, isCancelable: Boolean) {

            if (hud != null)
                hud!!.dismiss()

            hud = CustomProgressDialog.create(context).setStyle(CustomProgressDialog.Style.SPIN_INDETERMINATE)
            hud!!.setCancellable(isCancelable)

            if (!hud!!.isShowing) {
                hud!!.show()
            }
        }

        fun hideProgress() {
            hud!!.dismiss()
        }

        fun <T> getObject(jsonElementResponse: Response<JsonElement>, t: Class<T>): T {
            return Gson().fromJson(jsonElementResponse.body()!!.asJsonObject.toString(), t)
        }

        /**
         * Function to convert seconds time to
         * Timer Format
         * Hours:Minutes:Seconds
         */
        fun milliSecondsToTimer(second: Long): String {
            var finalTimerString = ""
            // Convert total duration into time
            val hours = (second / (1000 * 60))
            // Add hours if there
            if (hours > 0) {
                finalTimerString = String.format("%02d", TimeUnit.MILLISECONDS.toHours(second)) + ":"
            }
            // Prepending 0 to seconds if it is one digit
            finalTimerString += String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(second) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(second)), TimeUnit.MILLISECONDS.toSeconds(second) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(second)))

            // return timer string
            return finalTimerString
        }

        /**
         * Function to change progress to timer
         * @param progress -
         * @param totalDuration
         * returns current duration in milliseconds
         */
        fun progressToTimer(progress: Int, totalDuration: Int): Int {
            var totalDuration = totalDuration
            val currentDuration: Int
            totalDuration /= 1000
            currentDuration = (progress.toDouble() / 100 * totalDuration).toInt()

            // return current duration in milliseconds
            return currentDuration * 1000
        }

        fun showLoading(context: Context) {
            dialog = Dialog(context)
            val inflate = LayoutInflater.from(context).inflate(R.layout.progress_bar, null)
            dialog!!.setContentView(inflate)
            dialog!!.setCancelable(false)
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            dialog!!.show()
        }

        fun hideLoading() {
            if (dialog!!.isShowing) {
                dialog!!.cancel()
            }
        }
    }
}
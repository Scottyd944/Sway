package com.swayapp.helpers

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Build
import android.util.Log

import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetAddress
import java.net.URL

/**
 * Created by javiermartinez on 8/10/17.
 */

class ConnectionDetector(private val _context: Context) : AsyncTask<Void, Void, Boolean>() {
    private val TAG = "CONNECTION"


    val isOnlinePing: Boolean
        get() {

            val runtime = Runtime.getRuntime()
            try {

                val ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
                val exitValue = ipProcess.waitFor()
                return exitValue == 0

            } catch (e: IOException) {
                Log.d(TAG, "Ping Failed:$e")
                e.printStackTrace()
            } catch (e: InterruptedException) {
                Log.d(TAG, "Ping Failed:$e")
                e.printStackTrace()
            }

            return false
        }

    internal val isOnlineInetAddress: Boolean
        get() {
            try {
                val inetAddress = InetAddress.getByName("www.google.com")

                return inetAddress != null //&& inetAddress != ""
            } catch (ex: Exception) {
                return false
            }

        }

    override fun doInBackground(vararg params: Void): Boolean? {
        val cm = _context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnected) {
            try {
                val url = URL("http://www.google.com/")
                val urlc = url.openConnection() as HttpURLConnection
                urlc.setRequestProperty("User-Agent", "test")
                urlc.setRequestProperty("Connection", "close")
                urlc.connectTimeout = 1000 // mTimeout is in seconds
                urlc.connect()
                return if (urlc.responseCode == 200) {
                    true
                } else {
                    false
                }
            } catch (e: IOException) {
                Log.i("warning", "Error checking internet connection", e)
                return false
            }

        }
        return false
    }

    /**
     * Se verifican todos los posibles proveedores de internet
     */

    internal fun isConnectingToInternet(context: Context): Boolean {

        return if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            isActiveNetworkConnected(context) && (isOnlinePing || isOnlineInetAddress)
        } else {
            isActiveNetworkConnected(context) && isOnlinePing
        }


    }

    fun statusGPS(): Boolean {
        val lm = _context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false
        var network_enabled = false

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
        }


        return if (!gps_enabled && !network_enabled) {
            false

        } else true
    }

    internal fun isActiveNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkInfo = connectivityManager.activeNetworkInfo

        return (networkInfo != null
                && networkInfo.isAvailable
                && networkInfo.isConnected)
    }

    override fun onPostExecute(aBoolean: Boolean?) {
        super.onPostExecute(aBoolean)
    }
}

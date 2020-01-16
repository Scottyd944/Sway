package com.swayapp.helpers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.swayapp.LandingPage
import com.swayapp.R
import org.json.JSONArray
import org.json.JSONException

/**
 * Created by Javier Martinez on 8/3/2017.
 */

class swayLocationService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    internal lateinit var swayApi: swayApiManager
    internal lateinit var prefsManager: SharedPreferencesManager
    internal lateinit var receiver: BroadcastReceiver
    internal var privacyAreas: JSONArray? = null
    internal var lastSavedLocationTime: Long = 0
    internal var locationStartTime: Long = 0
    internal var locationDisabledInDB:Boolean = false

    //Fused Location API
    private var mLocationRequest: LocationRequest? = null
    private var mLastLocation: Location? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private val CHANNEL_ID = "ForegroundService Kotlin"

    //GPS Location API
    private var mLocationManager: LocationManager? = null
    private val mLastLocationGPS: Location? = null

    private var mLocationListeners = arrayOf(LocationListener(LocationManager.GPS_PROVIDER), LocationListener(LocationManager.NETWORK_PROVIDER))

    private inner class LocationListener(provider: String) : android.location.LocationListener {
        internal var mLastLocationGPS: Location

        init {
            Log.d(TAG, "LocationListener $provider")
            mLastLocationGPS = Location(provider)
        }

        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "New Location Received: $location")
            mLastLocationGPS.set(location)
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.d(TAG, "onStatusChanged: $provider")
        }
    }

    @Synchronized
    protected fun buildGoogleApiClient() {
        Log.d(TAG, "Build google api client")
        mGoogleApiClient = GoogleApiClient.Builder(applicationContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        //Connection failed
        Log.d(TAG, "Fused location Google Client Connection Failed")
    }

    override fun onConnected(arg0: Bundle?) {
        // Once connected with google api, get the location
        Log.d(TAG, "Google api client connected successfully")
        startLocationUpdates()
    }

    override fun onConnectionSuspended(arg0: Int) {
        mGoogleApiClient!!.connect()
    }

    protected fun startLocationUpdates() {
        LOCATION_CURRENT_INTERVAL = LOCATION_DEFAULT_INTERVAL
        locationStartTime = System.currentTimeMillis()
        // Create the location request
        swayApi.GetAllPrivacyAreas()
        Log.d(TAG, "Fused location start updates")
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((30 * 1000).toLong())
                .setFastestInterval((5 * 1000).toLong())
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Just require the location if we have permissions
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "Got Location : $location")
        //we received an update
        // 1-Set flag to control the database update
        // 2-Check for privacy area and set to active inactive - Shared Prefs Field - pending to review Interface implementation if possible
        // 3-Update the location on the app - Shared Prefs Field - pending to review Interface implementation if possible
        // 4- Check the time period and update if the time between both updates is more than 5 min. - service config takes care of this
        Log.d(TAG, "checking time elapsed since last location update...")
        if (System.currentTimeMillis() - LOCATION_CURRENT_INTERVAL > lastSavedLocationTime) {
            if (checkPrivacyAreaIsActive(location) == false) {

              //  var local = Intent()
              //  local.setAction("com.privacynotactive.action")
              //  this.sendBroadcast(local)
                if (location != null) {
                    Log.d(TAG, "Trying to update location in database...")
                    swayApi.UpdateLocationInDB(location.latitude, location.longitude, location.altitude, Integer.toString(prefsManager.userID))
                    lastSavedLocationTime = System.currentTimeMillis()
                    var intent = Intent("CurrentLocationUpdate")
                    intent.putExtra("latitude", location.latitude)
                    intent.putExtra("longitude", location.longitude)
                    sendBroadcast(intent)
                }
                LOCATION_CURRENT_INTERVAL = LOCATION_DEFAULT_INTERVAL
                locationDisabledInDB = false
            }
            else
            {
                if (LOCATION_CURRENT_INTERVAL > LOCATION_PRIVACY_INTERVAL)
                {
                    var intent = Intent("LocationStopped")
                    sendBroadcast(intent)
                    this.stopLocationUpdates()
                }
                else {
                    LOCATION_CURRENT_INTERVAL = LOCATION_CURRENT_INTERVAL + 20000
                    if (!locationDisabledInDB) {
                        swayApi.turnOffLocationInDB(Integer.toString(prefsManager.userID))
                        locationDisabledInDB = true
                    }
                }
            }
        } else {
            Log.d(TAG, "Less than " + LOCATION_CURRENT_INTERVAL / 1000 / 60 + " minutes have passed, location will not be updated!")
            Log.d(TAG, "FINAL TIME: " + (System.currentTimeMillis() - locationStartTime).toString())
            if (System.currentTimeMillis() - locationStartTime > LOCATION_MAX_INTERVAL)
            {
                //turn off location after LOCATION_MAX hours
                var intent = Intent("LocationStopped")
                sendBroadcast(intent)
                this.stopLocationUpdates()
            }
        }

        mLastLocation = location
    }

    fun stopLocationUpdates() {
        swayApi.turnOffLocationInDB(Integer.toString(prefsManager.userID))
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        } else {
            //no need to stop updates - we are no longer connected to location service anyway
        }
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.d(TAG, "fail to remove location listners, ignore", ex)
                }

            }
        }
        Log.d(TAG, "Location updates have been STOPPED!")
    }

    override fun onCreate() {
        Log.d(TAG, "Location service has been started")
        swayApi = swayApiManager(applicationContext)
        prefsManager = SharedPreferencesManager(applicationContext)
        initializeLocationManager()

        try {
            mLocationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[1])
        } catch (ex: java.lang.SecurityException) {
            Log.d(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            mLocationManager!!.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                    mLocationListeners[0])
        } catch (ex: java.lang.SecurityException) {
            Log.d(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }

        val filter = IntentFilter()
        filter.addAction("PrivacyAreasUpdated")
        receiver = object: BroadcastReceiver(){
             override fun onReceive(context: Context?, intent: Intent?) {
                 var data = intent?.getStringExtra("data")
               Log.d(TAG, "New Privacy Areas Received: " + data)
                 prefsManager.setPrivacyAreas(data!!)
                 lastSavedLocationTime = System.currentTimeMillis()-130000
            }
        }
        registerReceiver(receiver, filter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)

        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, LandingPage::class.java)
        val pendingIntent = PendingIntent.getActivity(
                this,
                0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("sway location is active.")
                .setContentText(input)
                .setSmallIcon(R.drawable.gdncicon180)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1, notification)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Location service has been stopped")
        super.onDestroy()
        stopLocationUpdates()
        unregisterReceiver(receiver)

        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.d(TAG, "fail to remove location listners, ignore", ex)
                }

            }
        }

    }

    private fun initializeLocationManager() {
        Log.d(TAG, "initializeLocationManager")
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        if (mGoogleApiClient == null) {
            buildGoogleApiClient()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onUnbind(intent: Intent): Boolean {
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent) {
        super.onRebind(intent)
    }

    fun checkPrivacyAreaIsActive(location: Location): Boolean? {
        var isActive = false
        try {
            if (prefsManager.privacyAreas != null) {
                var privString = prefsManager.privacyAreas
                Log.d(TAG, "PRIVACY AREAS: " + privString.toString())
                privacyAreas = privString
                if (privacyAreas!!.length() > 0) {
                    for (p in 0 until privacyAreas!!.length()) {
                        val privacyArea = privacyAreas!!.getJSONObject(p)
                        val results = FloatArray(1)
                        Location.distanceBetween(location!!.latitude, location!!.longitude, privacyArea.getDouble("latitude"), privacyArea.getDouble("longitude"), results)
                        if (results[0] < privacyArea.getInt("radius")) {
                            isActive = true
                            Log.d(TAG, "Privacy Area is ACTIVE!")
                            var intent = Intent("SetPrivacyStatus")
                            intent.putExtra("status", true)
                            intent.putExtra("latitude", privacyArea.getDouble("latitude"))
                            intent.putExtra("longitude", privacyArea.getDouble("longitude"))
                            intent.putExtra("radius", privacyArea.getInt("radius"))
                            sendBroadcast(intent)
                            break
                        }
                    }
                    if (!isActive) {
                        Log.d(TAG, "Privacy are NOT Active.")
                        var intent = Intent("SetPrivacyStatus")
                        intent.putExtra("status", false)
                        sendBroadcast(intent)
                    }
                } else {
                    Log.d(TAG, "Privacy Areas array is empty.")
                    var intent = Intent("SetPrivacyStatus")
                    intent.putExtra("status", false)
                    sendBroadcast(intent)
                }
            }
        } catch (err: JSONException) {
            Log.d(TAG, "Error loading privacy areas from shared preferences$err")
        } catch (err: NullPointerException) {
            Log.d(TAG, "Error loading privacy areas from shared preferences$err")
        }


        return isActive
    }

    companion object {
        private val TAG = "LocationService"
        private var LOCATION_CURRENT_INTERVAL: Long = 120000//Restriction in miliseconds to update to DB
        private val LOCATION_DEFAULT_INTERVAL: Long = 120000//Restriction in miliseconds to update to DB
        private val LOCATION_PRIVACY_INTERVAL: Long = 1800000 //30 minutes - Stop Service
        private val LOCATION_MAX_INTERVAL: Long =  14400000 //Stop updates after 4 hours
        private val LOCATION_INTERVAL = 10000
        private val LOCATION_DISTANCE = 0f
    }
}

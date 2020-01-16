package com.swayapp.helpers

import android.content.Context
import android.util.Log
import com.swayapp.R
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by javiermartinez on 8/10/17.
 */

class swayApiManager(internal var context: Context) {
    internal var prefsManager: SharedPreferencesManager

    init {
        prefsManager = SharedPreferencesManager(context)
    }

    fun UpdateLocationInDB(Latitude: Double?, Longitude: Double?, Altitude: Double?, UserID: String) : Boolean {

            try {
                // Add parameters to call
                val json = JSONObject()
                json.put("UserID", UserID)
                json.put("Latitude", Latitude!!.toString())
                json.put("Longitude", Longitude!!.toString())
                json.put("Altitude", Altitude!!.toString())

                HttpTask( {
                    if (it == null) {
                        println("Connection error sending location to database.")
                        return@HttpTask
                    }
                    Log.d(TAG, "Location was updated successfully!")
                    Log.d(TAG, "UpdateLocationInDB response: $it")
                } ).execute("POST", context.resources.getString(R.string.sway_api_base) + "Locations/Locations", json.toString())
                return true

            } catch (e: Exception) {
                Log.d(TAG, "General error sending location to database.")
                e.printStackTrace()
                return false
            }

    }

    fun turnOffLocationInDB(UserID: String) {

        try {
            // Add parameters to call
            val json = JSONObject()
            json.put("UserID", UserID)

            HttpTask( {
                if (it == null) {
                    println("Connection error turning off location in database.")
                    return@HttpTask
                }
                Log.d(TAG, "Location was turned off in database!")
            } ).execute("POST", context.resources.getString(R.string.sway_api_base) + "Locations/LocationOff?UserID=" + UserID, json.toString())

        } catch (e: Exception) {
            Log.d(TAG, "General error turning off location in database.")
            e.printStackTrace()
        }

    }

    fun GetAllPrivacyAreas() : Boolean {
        var privacyAreasResponseArray: JSONArray

            try {
                // Add parameters to call
                val json = JSONObject()
                json.put("UserID", prefsManager.userID)

                HttpTask( {
                    if (it == null) {
                        println("Connection error getting privacy areas.")
                        return@HttpTask
                    }
                    Log.d(TAG, "Privacy areas retrieved!")
                    Log.d(TAG, "Privacy area response: $it")
                    //privacyAreasResponseArray = JSONArray(it)
                    prefsManager.setPrivacyAreas(it)
                } ).execute("POST", context.resources.getString(R.string.sway_api_base) + "Privacy/GetAllAreas?UserID=" + prefsManager.userID.toString(), json.toString())
                return true

            } catch (e: Exception) {
                Log.d(TAG, "General error on the call")
                e.printStackTrace()
                return false
            }
    }

    companion object {
        //Class for HTTP calls that do not require an interaction on finished action
        private val TAG = "swayAPI"
    }


}

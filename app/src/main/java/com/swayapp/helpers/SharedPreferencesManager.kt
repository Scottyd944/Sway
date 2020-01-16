package com.swayapp.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

import org.json.JSONArray
import org.json.JSONException

/**
 * Created by javiermartinez on 8/10/17.
 */

class SharedPreferencesManager(internal var sharedContext: Context?) {
    //Master object for settings file
    internal var sharedPrefs: SharedPreferences
    //Master object for settings editor
    internal var sharedPrefsEditor: SharedPreferences.Editor
    internal val FILE_NAME = "swayUserSettings"

    //Getters and Setters FOR PROFILE
    var birthday: String?
        get() = sharedPrefs.getString("Birthday", "")
        set(birthday) {
            sharedPrefsEditor.putString("Birthday", birthday)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userID: Int
        get() = sharedPrefs.getInt("UserID", -1)
        set(userId) {
            sharedPrefsEditor.putInt("UserID", userId)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var firstName: String?
        get() = sharedPrefs.getString("firstName", "")
        set(firstName) {
            sharedPrefsEditor.putString("firstName", firstName)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userGender: Int
        get() = sharedPrefs.getInt("Gender", -1)
        set(gender) {
            sharedPrefsEditor.putInt("Gender", gender)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var profileComplete: Boolean?
        get() = sharedPrefs.getBoolean("profileComplete", false)
        set(isComplete) {
            sharedPrefsEditor.putBoolean("profileComplete", isComplete!!)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userHeightFt: Int
        get() = sharedPrefs.getInt("HeightFt", -1)
        set(heightFt) {
            sharedPrefsEditor.putInt("HeightFt", heightFt)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userHeightIn: Int
        get() = sharedPrefs.getInt("HeightIn", -1)
        set(heightIn) {
            sharedPrefsEditor.putInt("HeightIn", heightIn)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userWeight: Int
        get() = sharedPrefs.getInt("Weight", -1)
        set(weight) {
            sharedPrefsEditor.putInt("Weight", weight)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userBodyType: Int
        get() = sharedPrefs.getInt("BodyType", -1)
        set(bodyType) {
            sharedPrefsEditor.putInt("BodyType", bodyType)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userEyeColor: Int
        get() = sharedPrefs.getInt("EyeColor", -1)
        set(eyeColor) {
            sharedPrefsEditor.putInt("EyeColor", eyeColor)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userHairColor: Int
        get() = sharedPrefs.getInt("HairColor", -1)
        set(hairColor) {
            sharedPrefsEditor.putInt("HairColor", hairColor)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userEthnicity: Int
        get() = sharedPrefs.getInt("Ethnicity", -1)
        set(ethnicity) {
            sharedPrefsEditor.putInt("Ethnicity", ethnicity)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userOrganization: Int
        get() = sharedPrefs.getInt("Organization", -1)
        set(organization) {
            sharedPrefsEditor.putInt("Organization", organization)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userPersonality1: Int
        get() = sharedPrefs.getInt("Personality1", -1)
        set(personality1) {
            sharedPrefsEditor.putInt("Personality1", personality1)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userPersonality2: Int
        get() = sharedPrefs.getInt("Personality2", -1)
        set(personality2) {
            sharedPrefsEditor.putInt("Personality2", personality2)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userPersonality3: Int
        get() = sharedPrefs.getInt("Personality3", -1)
        set(personality3) {
            sharedPrefsEditor.putInt("Personality3", personality3)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userEducation: Int
        get() = sharedPrefs.getInt("Education", -1)
        set(education) {
            sharedPrefsEditor.putInt("Education", education)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userRisk: Int
        get() = sharedPrefs.getInt("Risk", -1)
        set(risk) {
            sharedPrefsEditor.putInt("Risk", risk)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userPolitics: Int
        get() = sharedPrefs.getInt("Politics", -1)
        set(politics) {
            sharedPrefsEditor.putInt("Politics", politics)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var userKids: Int
        get() = sharedPrefs.getInt("Kids", -1)
        set(kids) {
            sharedPrefsEditor.putInt("Kids", kids)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    //Getters and Setters For MATCH
    var matchComplete: Boolean
        get() = sharedPrefs.getBoolean("matchComplete", false)
        set(isMatchComplete) {
            sharedPrefsEditor.putBoolean("matchComplete", isMatchComplete)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    val matchGenderArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("GenderArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the gender array: $err")
                return null
            }

        }

    var matchAgeFrom: String?
        get() = sharedPrefs.getString("AgeFrom", "")
        set(ageFrom) {
            sharedPrefsEditor.putString("AgeFrom", ageFrom)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var matchAgeTo: String?
        get() = sharedPrefs.getString("AgeTo", "")
        set(ageTo) {
            sharedPrefsEditor.putString("AgeTo", ageTo)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var matchFromHeightFt: Int
        get() = sharedPrefs.getInt("HeightFtFrom", -1)
        set(fromHeightFt) {
            sharedPrefsEditor.putInt("HeightFtFrom", fromHeightFt)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var matchFromHeightIn: Int
        get() = sharedPrefs.getInt("HeightInFrom", -1)
        set(fromHeightIn) {
            sharedPrefsEditor.putInt("HeightInFrom", fromHeightIn)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var matchToHeightFt: Int
        get() = sharedPrefs.getInt("HeightFtTo", -1)
        set(toHeightFt) {
            sharedPrefsEditor.putInt("HeightFtTo", toHeightFt)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var matchToHeightIn: Int
        get() = sharedPrefs.getInt("HeightInTo", -1)
        set(toHeightIn) {
            sharedPrefsEditor.putInt("HeightInTo", toHeightIn)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    val matchBodyTypeArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("BodyTypeArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the body type array: $err")
                return null
            }

        }

    val matchEyeColorArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("EyeColorArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the eye color array: $err")
                return null
            }

        }

    val matchHairColorArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("HairColorArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the hair color array: $err")
                return null
            }

        }

    val matchEthnicityArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("EthnicityArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the ethnicity array: $err")
                return null
            }

        }

    val matchOrganizationArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("OrganizationArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the organization array: $err")
                return null
            }

        }

    val matchPersonalityArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("PersonalityArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the personality array: $err")
                return null
            }

        }

    val matchEducationArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("EducationArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the education array: $err")
                return null
            }

        }

    val matchRiskArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("RiskArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the risk array: $err")
                return null
            }

        }

    val matchKidsArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("KidsArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the kids array: $err")
                return null
            }

        }

    val matchPoliticsArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("PoliticsArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the politics array: $err")
                return null
            }

        }

    val matchForArray: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("ForArray", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the -for- array: $err")
                return null
            }

        }

    //General
    var deviceToken: String?
        get() = sharedPrefs.getString("DeviceToken", "")
        set(token) {
            sharedPrefsEditor.putString("DeviceToken", token)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    var newestDeviceToken: String?
        get() = sharedPrefs.getString("NewestDeviceToken", "")
        set(token) {
            sharedPrefsEditor.putString("NewestDeviceToken", token)
            sharedPrefsEditor.apply()
            sharedPrefsEditor.commit()
        }

    val privacyAreas: JSONArray?
        get() {
            try {
                return JSONArray(sharedPrefs.getString("PrivacyAreas", ""))
            } catch (err: JSONException) {
                Log.d(TAG, "Error getting the privacy areas array: $err")
                return null
            }

        }

    init {
        //if the file does not exist, it will create the file
        //if the file exist it will just get the file for access
        sharedPrefs = sharedContext!!.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        sharedPrefsEditor = sharedPrefs.edit()

        if (sharedPrefs.getBoolean("isSetUp", false) == false) {
            //if the file has not being created then proceed to setUp
            setUpUserSettingsFile()
        }//else dont overwrite the data
    }

    fun setUpUserSettingsFile() {
        //this method will store all the value pair keys with default values to be accessed later in the app
        //All these values are default values that will be overwritten by the actual values

        sharedPrefsEditor.putString("DeviceToken", "") //Firebase device token
        sharedPrefsEditor.putString("NewestDeviceToken", "") //The newest token received by firebase instance service
        sharedPrefsEditor.putString("PrivacyAreas", "") //A JSON array for privacy areas store


        sharedPrefsEditor.putBoolean("profileComplete", false)//flag to check for a completed profile
        sharedPrefsEditor.putString("Birthday", "") //birthday of the logged user
        sharedPrefsEditor.putInt("UserID", -1)//user id from backend table
        sharedPrefsEditor.putInt("Gender", -1)//user gender
        sharedPrefsEditor.putInt("HeightFt", -1) // user height in Feet
        sharedPrefsEditor.putInt("HeightIn", -1) //user height in Inches
        sharedPrefsEditor.putInt("BodyType", -1) //user bodyType
        sharedPrefsEditor.putInt("EyeColor", -1)//user eye color id
        sharedPrefsEditor.putInt("HairColor", -1)// user hair color id
        sharedPrefsEditor.putInt("Ethnicity", -1)//user ethnicity id
        sharedPrefsEditor.putInt("Organization", -1)//user organization id
        sharedPrefsEditor.putInt("Personality1", -1)//user personality 1 id
        sharedPrefsEditor.putInt("Personality2", -1)//personality 2 id
        sharedPrefsEditor.putInt("Personality3", -1)//personality 3 id
        sharedPrefsEditor.putInt("Education", -1)//education level id
        sharedPrefsEditor.putInt("Risk", -1)// risk level
        sharedPrefsEditor.putInt("Politics", -1)//politics id
        sharedPrefsEditor.putInt("Kids", -1)//kids selection id

        sharedPrefsEditor.putBoolean("matchComplete", false)//flag to check for a completed profile
        sharedPrefsEditor.putString("GenderArray", "")//string representing an array of ints for gender
        sharedPrefsEditor.putString("AgeFrom", "")//minimum age range to look for matches
        sharedPrefsEditor.putString("AgeTo", "")//maximum age range to look for matches
        sharedPrefsEditor.putInt("HeightFtFrom", -1)//minimum height on ft to look for matches
        sharedPrefsEditor.putInt("HeightInFrom", -1)//minimum height on ft to look for matches
        sharedPrefsEditor.putInt("HeightFtTo", -1) //maximum height on ft to look for matches
        sharedPrefsEditor.putInt("HeightInTo", -1) //maximum height on ft to look for matches
        sharedPrefsEditor.putString("BodyTypeArray", "")//string representing an array of ints for eye color
        sharedPrefsEditor.putString("EyeColorArray", "")//string representing an array of ints for eye color
        sharedPrefsEditor.putString("HairColorArray", "")//string representing an array of ints for hair color
        sharedPrefsEditor.putString("EthnicityArray", "")//string representing an array of ints for ethnicity
        sharedPrefsEditor.putString("OrganizationArray", "")//string representing an array of ints for organization
        sharedPrefsEditor.putString("PersonalityArray", "")//string representing an array of ints for personality
        sharedPrefsEditor.putString("EducationArray", "")//string representing an array of ints for education
        sharedPrefsEditor.putString("RiskArray", "")//string representing an array of ints for risk
        sharedPrefsEditor.putString("KidsArray", "")//string representing an array of ints for kids
        sharedPrefsEditor.putString("PoliticsArray", "")//string representing an array of ints for politics
        sharedPrefsEditor.putString("ForArray", "")//string representing an array of ints


        sharedPrefsEditor.putBoolean("isSetUp", true)//string representing an array of ints
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchGenderArray(genderArray: String) {
        sharedPrefsEditor.putString("GenderArray", genderArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchBodyTypeArray(bodyTypeArray: String) {
        sharedPrefsEditor.putString("BodyTypeArray", bodyTypeArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchEyeColorArray(eyeColorArray: String) {
        sharedPrefsEditor.putString("EyeColorArray", eyeColorArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchHairColorArray(hairColorArray: String) {
        sharedPrefsEditor.putString("HairColorArray", hairColorArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchEthnicityArray(ethnicityArray: String) {
        sharedPrefsEditor.putString("EthnicityArray", ethnicityArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchOrganizationArray(organizationArray: String) {
        sharedPrefsEditor.putString("OrganizationArray", organizationArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchPersonalityArray(personalityArray: String) {
        sharedPrefsEditor.putString("PersonalityArray", personalityArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchEducationArray(educationArray: String) {
        sharedPrefsEditor.putString("EducationArray", educationArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchRiskArray(riskArray: String) {
        sharedPrefsEditor.putString("RiskArray", riskArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchKidsArray(kidsArray: String) {
        sharedPrefsEditor.putString("KidsArray", kidsArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchPoliticsArray(politicsArray: String) {
        sharedPrefsEditor.putString("PoliticsArray", politicsArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setMatchForArray(forArray: String) {
        sharedPrefsEditor.putString("ForArray", forArray)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }

    fun setPrivacyAreas(privacyAreas: String) {
        sharedPrefsEditor.putString("PrivacyAreas", privacyAreas)
        sharedPrefsEditor.apply()
        sharedPrefsEditor.commit()
    }


    //OTHER METHODS
    fun printAllValues() {
        val keys = sharedPrefs.all
        for ((key, value) in keys) {
            Log.i("FEETZ", key + ": " +
                    value.toString())
        }
    }

    fun wipeCustomerData() {
        setUpUserSettingsFile()
    }

    companion object {
        //class to manage all inputs and outputs to sharedPreferences (NSUserDefaults on IOS)
        val TAG = "Shared Prefs"
    }


}

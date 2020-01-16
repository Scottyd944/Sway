package com.swayapp

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.swayapp.helpers.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class LandingPage : AppCompatActivity() {
    internal lateinit var featuresFlipper: ViewFlipper
    internal lateinit var bulletIndicator: RatingBar
    internal lateinit var loginIndicator: ProgressBar
    private var initialX: Float = 0.toFloat()
    private val featuresSlides = 4
    internal lateinit var firstName: String
    internal lateinit var lastName: String
    internal lateinit var facebookId: String
    internal lateinit var profilePicture: String
    internal lateinit var email: String
    internal var profileComplete: Boolean? = false
    internal var matchComplete: Boolean? = false

    //MANAGERS
    internal lateinit var prefsManager: SharedPreferencesManager
    internal lateinit var connectionDetector: ConnectionDetector
    internal lateinit var swayAPI: swayApiManager

    //FACEBOOK SIGN IN
    internal lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsManager = SharedPreferencesManager(this)

        connectionDetector = ConnectionDetector(this)
        swayAPI = swayApiManager(this)
        //Facebook initializer
        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()

        setContentView(R.layout.landing_page_layout)
        featuresFlipper = findViewById(R.id.featuresFlipper) as ViewFlipper
        bulletIndicator = findViewById(R.id.bulletIndicator) as RatingBar

        prefsManager.printAllValues()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
            if (!connectionDetector.statusGPS()) {
                buildAlertMessageNoGps(getString(R.string.no_location_services_message), getString(R.string.no_location_services_title))
            }
        }

      //  Log.d(TAG, "Send to start service!")
      //  val i = Intent(this@LandingPage, swayLocationService::class.java)
      //  startService(i)

    }

    fun signInFacebook(v: View) {
        Log.d(TAG, "FacbeookButtonPressed")
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_birthday"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "Facebook Login Results$loginResult")
                val request = GraphRequest.newMeRequest(
                        loginResult.accessToken
                ) { `object`, response ->
                    Log.d(TAG, "Profile Info: $response")
                    Log.d(TAG, "Response Object: $`object`")
                    try {

                        try {
                            //get and save birthday date
                            var birthday: String = `object`.getString("birthday");
                            //val birthday = "03/07/1980"
                            prefsManager.birthday = birthday
                        }
                        catch(e: Exception)
                        {
                            prefsManager.birthday = ""
                        }

                        firstName = `object`.getString("first_name")
                        lastName = `object`.getString("last_name")
                        email = `object`.getString("email")
                        profilePicture = `object`.getJSONObject("picture").getJSONObject("data").getString("url")
                        facebookId = `object`.getString("id")

                        val json = JSONObject()
                        json.put("FirstName", firstName)
                        json.put("LastName", lastName)
                        json.put("Email", email)
                        json.put("FacebookID", facebookId)
                        json.put("ProfilePic", profilePicture)

                        HttpTask( {
                            if (it == null) {
                                println("connection error")
                                Toast.makeText(this@LandingPage, "Something Went Wrong", Toast.LENGTH_LONG).show()
                                return@HttpTask
                            }
                            println(it)
                            loginResult(it)
                        } ).execute("POST", getString(R.string.sway_api_base) + "/Users/PostUser", json.toString())


                    } catch (ex: JSONException) {
                        Log.d(TAG, "Facebook Info FAILED: $ex")
                        showFacebookAlertDialog()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id, name, first_name, last_name, picture.type(large), email, birthday")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {
                Log.d(TAG, "User cancelled the Facebook Login")
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "There was an error login with facebook $error")
                showFacebookAlertDialog()
            }
        })
    }

    fun loginResult(data: String)
    {
        val loginArr = JSONArray(data)
        val loginData:JSONObject = loginArr[0] as JSONObject

            try {
                if (loginData.length() > 1) {
                    Log.d(TAG, "length is more than 1, save to preferences")
                    saveUserToPreferences(loginData)
                } else {
                    val userID = Integer.parseInt(loginData.getString("userID"))
                    prefsManager.userID = userID
                }

                prefsManager.firstName = firstName


            } catch (e: JSONException) {
                Log.d(TAG, "JSON Parse Error : $e")
            } catch (e: NumberFormatException) {
                Log.d(TAG, "ID was not a number")
            }

    }

    private fun isLocationServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun toggleLocationService(v: View) {
        val i = Intent(this, swayLocationService::class.java)
        if (isLocationServiceRunning(swayLocationService::class.java)) {
            stopService(i)
            Toast.makeText(this, "Service STOPPED", Toast.LENGTH_LONG).show()
        } else {
        //    startService(i)
            Toast.makeText(this, "Service STARTED", Toast.LENGTH_SHORT).show()
        }
    }

    fun showFacebookAlertDialog() {
        //Create alert dialog to display error message
        val facebookError = AlertDialog.Builder(this)
        facebookError.setTitle(getString(R.string.login_error))
        facebookError.setMessage(getString(R.string.facebook_sign_in_failure))
        facebookError.setCancelable(true)
        facebookError.setIcon(android.R.drawable.ic_dialog_alert)
        facebookError.setPositiveButton(
                "OK"
        ) { dialog, id -> dialog.cancel() }
        val facebookErrorDialog = facebookError.create()
        facebookErrorDialog.show()
    }

    fun saveUserToPreferences(userData: JSONObject) {

        try {
            if (!userData.isNull("userID")) {
                prefsManager.userID = userData.getInt("userID")
            }


            if (!userData.isNull("shortBirthday")) {
                //val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                val format = SimpleDateFormat("MM/dd/yyyy")
                val birthdayFormatted = format.format(Date.parse(userData.getString("shortBirthday")))
                //prefsManager.birthday = userData.getString("Birthday")
                prefsManager.birthday = birthdayFormatted
                Log.d(TAG, "Formatted BirthDay: $birthdayFormatted")
            }

            //Check for user setting gender, if is set, profile is complete
            if (!userData.isNull("gender")) {
                profileComplete = true
                prefsManager.profileComplete = profileComplete
                prefsManager.userGender = userData.getInt("gender")
            }

            //User Profile
            if (profileComplete!!) {
                if (!userData.isNull("height")) {
                    val height = userData.getInt("height").toDouble()
                    val heightFt = Math.floor(height / 12)
                    val heightIn = height - heightFt * 12
                    prefsManager.userHeightFt = heightFt.toInt() - 4 //minus 4 calculation to get the INDEX of the value
                    prefsManager.userHeightIn = heightIn.toInt()
                }


                if (!userData.isNull("bodyType")) {
                    prefsManager.userBodyType = userData.getInt("bodyType")
                }

                if (!userData.isNull("eyeColor")) {
                    prefsManager.userEyeColor = userData.getInt("eyeColor")
                }

                if (!userData.isNull("hairColor")) {
                    prefsManager.userHairColor = userData.getInt("hairColor")
                }

                if (!userData.isNull("ethnicity")) {
                    prefsManager.userEthnicity = userData.getInt("ethnicity")
                }

                if (!userData.isNull("organization")) {
                    prefsManager.userOrganization = userData.getInt("organization")
                }

                if (!userData.isNull("personality1")) {
                    prefsManager.userPersonality1 = userData.getInt("personality1")
                }

                if (!userData.isNull("personality2")) {
                    prefsManager.userPersonality2 = userData.getInt("personality2")
                }

                if (!userData.isNull("personality3")) {
                    prefsManager.userPersonality3 = userData.getInt("personality3")
                }

                if (!userData.isNull("education")) {
                    prefsManager.userEducation = userData.getInt("education")
                }

                if (!userData.isNull("risk")) {
                    prefsManager.userRisk = userData.getInt("risk")
                }

                if (!userData.isNull("politics")) {
                    prefsManager.userPolitics = userData.getInt("politics")
                }

                if (!userData.isNull("kids")) {
                    prefsManager.userKids = userData.getInt("kids")
                }

            }

            //Check for the gender array, if more than 0 then match is complete
            if (!userData.isNull("mGender")) {
                val mGenderArray = JSONArray(userData.getString("mGender"))
                if (mGenderArray.length() > 0) {
                    matchComplete = true
                    prefsManager.matchComplete = true
                    prefsManager.setMatchGenderArray(userData.getString("mGender"))
                }
            }

            if (matchComplete!!) {
                if (!userData.isNull("mAgeFrom")) {
                    prefsManager.matchAgeFrom = userData.getString("mAgeFrom")
                }

                if (!userData.isNull("mAgeTo")) {
                    prefsManager.matchAgeTo = userData.getString("mAgeTo")
                }

                if (!userData.isNull("mHeightFrom")) {
                    val fromHeight = userData.getInt("mHeightFrom").toDouble()
                    val fromHeightFt = Math.floor(fromHeight / 12)
                    val fromHeightIn = fromHeight - fromHeightFt * 12
                    prefsManager.matchFromHeightFt = fromHeightFt.toInt() - 4 //minus 4 calculation to get the INDEX of the value
                    prefsManager.matchFromHeightIn = fromHeightIn.toInt()
                }

                if (!userData.isNull("mHeightTo")) {
                    val toHeight = userData.getInt("mHeightTo").toDouble()
                    val tpHeightFt = Math.floor(toHeight / 12)
                    val toHeightIn = toHeight - tpHeightFt * 12
                    prefsManager.matchToHeightFt = tpHeightFt.toInt() - 4 //minus 4 calculation to get the INDEX of the value
                    prefsManager.matchToHeightIn = toHeightIn.toInt()
                }


                if (!userData.isNull("mBodyType")) {
                    val mBodyTypeArray = JSONArray(userData.getString("mBodyType"))
                    if (mBodyTypeArray.length() > 0) {
                        prefsManager.setMatchBodyTypeArray(userData.getString("mBodyType"))
                    }
                }

                if (!userData.isNull("mEyeColor")) {
                    val mEyeColorArray = JSONArray(userData.getString("mEyeColor"))
                    if (mEyeColorArray.length() > 0) {
                        prefsManager.setMatchEyeColorArray(userData.getString("mEyeColor"))
                    }
                }

                if (!userData.isNull("mHairColor")) {
                    val mHairColorArray = JSONArray(userData.getString("mHairColor"))
                    if (mHairColorArray.length() > 0) {
                        prefsManager.setMatchHairColorArray(userData.getString("mHairColor"))
                    }
                }

                if (!userData.isNull("mEthnicity")) {
                    val mEthnicityArray = JSONArray(userData.getString("mEthnicity"))
                    if (mEthnicityArray.length() > 0) {
                        prefsManager.setMatchEthnicityArray(userData.getString("mEthnicity"))
                    }
                }

                if (!userData.isNull("mOrganization")) {
                    val mOrganizationArray = JSONArray(userData.getString("mOrganization"))
                    if (mOrganizationArray.length() > 0) {
                        prefsManager.setMatchOrganizationArray(userData.getString("mOrganization"))
                    }
                }

                if (!userData.isNull("mPersonality")) {
                    val mPersonalityArray = JSONArray(userData.getString("mPersonality"))
                    if (mPersonalityArray.length() > 0) {
                        prefsManager.setMatchPersonalityArray(userData.getString("mPersonality"))
                    }
                }

                if (!userData.isNull("mEducation")) {
                    val mEducationArray = JSONArray(userData.getString("mEducation"))
                    if (mEducationArray.length() > 0) {
                        prefsManager.setMatchEducationArray(userData.getString("mEducation"))
                    }
                }

                if (!userData.isNull("mRisk")) {
                    val mRiskArray = JSONArray(userData.getString("mRisk"))
                    if (mRiskArray.length() > 0) {
                        prefsManager.setMatchRiskArray(userData.getString("mRisk"))
                    }
                }

                if (!userData.isNull("mKids")) {
                    val mKidsArray = JSONArray(userData.getString("mKids"))
                    if (mKidsArray.length() > 0) {
                        prefsManager.setMatchKidsArray(userData.getString("mKids"))
                    }
                }

                if (!userData.isNull("mPolitics")) {
                    val mPoliticsArray = JSONArray(userData.getString("mPolitics"))
                    if (mPoliticsArray.length() > 0) {
                        prefsManager.setMatchPoliticsArray(userData.getString("mPolitics"))
                    }
                }

                if (!userData.isNull("mFor")) {
                    val mForArray = JSONArray(userData.getString("mFor"))
                    if (mForArray.length() > 0) {
                        prefsManager.setMatchForArray(userData.getString("mFor"))
                    }
                }
            }
        } catch (err: JSONException) {
            Log.d(TAG, err.toString())
        } finally {
            prefsManager.printAllValues()
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == LOCATION_REQUEST_CODE) {
                //checked for location
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this@LandingPage, "Permission not granted, sway will note be able to determine your location.", Toast.LENGTH_LONG).show()
                } else {
                    if (!connectionDetector.statusGPS()) {
                        buildAlertMessageNoGps(getString(R.string.no_location_services_message), getString(R.string.no_location_services_title))
                    }
                }
            }
        }
    }

    override fun onTouchEvent(touchevent: MotionEvent): Boolean {
        when (touchevent.action) {
            MotionEvent.ACTION_DOWN -> initialX = touchevent.x
            MotionEvent.ACTION_UP -> {
                val finalX = touchevent.x
                if (initialX > finalX) {
                    if (featuresFlipper.displayedChild + 1 == featuresSlides)
                    {}
                    else {
                        // Next screen comes in from right.
                        featuresFlipper.setInAnimation(this, R.anim.slide_in_from_right)
                        // Current screen goes out from left.
                        featuresFlipper.setOutAnimation(this, R.anim.slide_out_to_left)
                        featuresFlipper.showNext()
                        bulletIndicator.rating = (featuresFlipper.displayedChild + 1).toFloat()
                    }
                } else {
                    if (featuresFlipper.displayedChild + 1 == 1)
                    {}
                    else {

                        // Next screen comes in from left.
                        featuresFlipper.setInAnimation(this, R.anim.slide_in_from_left)
                        // Current screen goes out from right.
                        featuresFlipper.setOutAnimation(this, R.anim.slide_out_to_right)
                        featuresFlipper.showPrevious()
                        bulletIndicator.rating = (featuresFlipper.displayedChild + 1).toFloat()
                    }
                }
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d("PERMISSIONS", "RequestCode: $requestCode  PERMISSIONS$permissions  GRANT RESULTS: $grantResults")
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                val perms = HashMap<String, Int>()
                perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED
                //fill with results
                for (i in permissions.indices)
                    perms[permissions[i]] = grantResults[i]

                if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PERMISSION CODE 1", "GRANTED")
                    // permissions was granted

                } else {
                    Log.d("PERMISSIONS", "Some permission was revoked")
                    if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_DENIED) {
                        buildCustomDialog("Permission Denied", "sway App requires your location to find the best matches for you, please go to settings and update permissions if you change your mind.", "OK")
                    }

                   // break
                    // permission denied
                }
            }
        }
    }

    fun buildCustomDialog(message: String, title: String, buttonTitle: String) {
        val messageTV: TextView
        val titleTV: TextView
        val acceptButton: Button
        val factory = LayoutInflater.from(this)
        val dialogoView = factory.inflate(
                R.layout.simple_dialog_layout, null)
        val dialogo = AlertDialog.Builder(this).create()
        dialogo.setView(dialogoView)
        messageTV = dialogoView.findViewById(R.id.dialogMessage) as TextView
        titleTV = dialogoView.findViewById(R.id.dialogTitle) as TextView
        acceptButton = dialogoView.findViewById(R.id.acceptButton) as Button
        messageTV.text = message
        titleTV.text = title
        acceptButton.text = buttonTitle

        acceptButton.setOnClickListener { dialogo.dismiss() }

        dialogo.show()
    }

    private fun buildAlertMessageNoGps(Message: String, Title: String) {
        val a: Button
        val b: Button
        val builder = AlertDialog.Builder(this)
        builder.setMessage(Message)
                .setTitle(Title)
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.settings)) { dialog, id -> startActivityForResult(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), LOCATION_REQUEST_CODE) }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()

        val params = LinearLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(20, 10, 20, 2)

        a = alert.getButton(DialogInterface.BUTTON_POSITIVE)
        a.setBackgroundColor(applicationContext.resources.getColor(R.color.colorPrimary))
        a.setTextColor(applicationContext.resources.getColor(R.color.alphaWhite))
        b = alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        b.setBackgroundColor(applicationContext.resources.getColor(R.color.colorPrimary))
        b.setTextColor(applicationContext.resources.getColor(R.color.alphaWhite))

        a.layoutParams = params
        b.layoutParams = params
        a.setPadding(40, 4, 40, 4)
        b.setPadding(40, 4, 40, 4)

    }

    companion object {
        private val TAG = "LoginPage"
        private val LOCATION_REQUEST_CODE = 101
    }


}

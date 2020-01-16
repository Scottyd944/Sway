package com.swayapp.fragments

//import androidx.core.app.Fragment
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.Location
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.Transition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.swayapp.R
import com.swayapp.helpers.swayLocationService
import com.swayapp.helpers.HttpTask
import com.swayapp.helpers.SharedPreferencesManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.home_layout.*
import org.json.JSONObject
import java.lang.Math.abs
import kotlin.math.roundToInt

/**
 * Created by javiermartinez on 9/8/17.
 */

class HomeFragment : Fragment(), OnMapReadyCallback {
    internal var mainContext: Context? = null
    internal lateinit var onlineLabel: TextView
    internal lateinit var offlineLabel: TextView
    internal lateinit var welcomeLabel: TextView
    internal lateinit var lblCredits: TextView
    internal lateinit var lblHours: TextView
    internal lateinit var lblPhotoStatus: TextView
    internal lateinit var switchOnline: Switch
    internal lateinit var imgPhoto: ImageView
    internal lateinit var btnPhoto: Button
    internal lateinit var matchMapButton: Button
    internal lateinit var homeLayout: View
    internal lateinit var vwOnline: ConstraintLayout
    internal lateinit var vwStatus: ConstraintLayout
    internal lateinit var vwMain: ConstraintLayout
    internal lateinit var lblCurrentLocation: TextView
    internal lateinit var vwMap: ConstraintLayout
    lateinit var googleMap:GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private lateinit var privacyArea: LatLng
    private var privacyRadius: Int = 0
    private lateinit var mapFragment: SupportMapFragment


    internal lateinit var prefsManager: SharedPreferencesManager
    internal lateinit var privacyReceiver: BroadcastReceiver
    internal lateinit var locationReceiver: BroadcastReceiver
    internal lateinit var locationStoppedReceiver: BroadcastReceiver
    private val BACKGROUND_REQUEST_CODE = 101
    private val LOCATION_REQUEST_CODE = 101
    private var myPhoto:String = ""
    private var isPhotoHidden:Boolean = false
    val constraintSetOffline = ConstraintSet()
    val constraintSetOnline = ConstraintSet()
    private var privacyIsActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate Called")
        val fragments = fragmentManager?.fragments
        //Log.d(TAG, "Number of fragments:" + fragments.size)
        for (i in fragments?.indices!!) {
            Log.d(TAG, fragments?.get(i).tag)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.activity!!)

        val privacyFilter = IntentFilter()
        privacyFilter.addAction("SetPrivacyStatus")
        privacyReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var isActive = intent?.getBooleanExtra("status", false)!!
                if (isActive != privacyIsActive)
                {
                    privacyIsActive = isActive
                    if (privacyIsActive) {
                        Log.d(TAG, "Privacy Area Active")
                        privacyIsActive = true
                        var latitude = intent?.getDoubleExtra("latitude", 0.0)
                        var longitude = intent?.getDoubleExtra("longitude", 0.0)
                        privacyRadius = intent?.getIntExtra("radius", 0)!!
                        privacyArea = LatLng(latitude!!, longitude!!)
                        refreshMap()
                    }
                    else
                    {
                        Log.d(TAG, "No Privacy Areas Active")
                        privacyIsActive = false
                        refreshMap()
                    }
                }

            }
        }
        context?.registerReceiver(privacyReceiver, privacyFilter)

        val locationFilter = IntentFilter()
        locationFilter.addAction("CurrentLocationUpdate")
        locationReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "Location Update Received!")
                lastLocation.latitude = intent?.getDoubleExtra("latitude", 0.0)!!
                lastLocation.longitude = intent?.getDoubleExtra("longitude", 0.0)!!
                refreshMap()
            }
        }
        context?.registerReceiver(locationReceiver, locationFilter)

        val locationStoppedFilter = IntentFilter()
        locationStoppedFilter.addAction("LocationStopped")
        locationStoppedReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.d(TAG, "Location Updates Stopped!")
                switchOnline.isChecked = false

                TransitionManager.beginDelayedTransition(vwStatus)
                constraintSetOffline.applyTo(vwMain)
            }
        }
        context?.registerReceiver(locationStoppedReceiver, locationStoppedFilter)
    }

    fun refreshMap()
    {
        googleMap.clear()

        val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        val markerOptions = MarkerOptions().position(currentLatLng)
        //change color or marker
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))

        googleMap.addMarker(markerOptions)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))

        if (privacyIsActive)
        {
            drawPrivacyArea(privacyArea, privacyRadius)
            lblStatus.text = "Privacy Area Active!"
        }
        else
        {
            lblStatus.text = "You are currently online."
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeLayout = inflater!!.inflate(R.layout.home_layout, container, false)
        onlineLabel = homeLayout.findViewById(R.id.onlineLabel) as TextView
        offlineLabel = homeLayout.findViewById(R.id.offlineLabel) as TextView
        welcomeLabel = homeLayout.findViewById(R.id.welcomeLabel) as TextView
        lblCredits = homeLayout.findViewById(R.id.lblCredits) as TextView
        lblHours = homeLayout.findViewById(R.id.lblHours) as TextView
        lblPhotoStatus = homeLayout.findViewById(R.id.lblPhotoStatus) as TextView
        lblCurrentLocation = homeLayout.findViewById(R.id.lblCurrentLocation) as TextView
        switchOnline = homeLayout.findViewById(R.id.switch_online) as Switch
        imgPhoto = homeLayout.findViewById(R.id.imgPhoto) as ImageView
        vwMap = homeLayout.findViewById(R.id.vwMap) as ConstraintLayout
        vwOnline = homeLayout.findViewById(R.id.vwOnline) as ConstraintLayout
        vwStatus = homeLayout.findViewById(R.id.vwStatus) as ConstraintLayout
        vwMain = homeLayout.findViewById(R.id.vwMain) as ConstraintLayout
        btnPhoto = homeLayout.findViewById(R.id.btnPhoto) as Button
        matchMapButton = homeLayout.findViewById(R.id.matchMapButton) as Button

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        vwOnline.visibility = View.INVISIBLE
        return homeLayout
    }

    override fun onResume() {
        super.onResume()
        checkPhotoVisibility()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        constraintSetOnline.clone(vwMain)
        constraintSetOffline.clone(vwMain)
        constraintSetOffline.centerVertically(vwStatus.id, vwMain.id)

        prefsManager = SharedPreferencesManager(context)
        //checkPhotoVisibility()
        var typeface = ResourcesCompat.getFont(context!!, R.font.montserrat);
        // Set an checked change listener for switch button
        switchOnline.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {


                val transition = AutoTransition()

                transition.duration = 500
                transition.addListener(object: Transition.TransitionListener{
                    override fun onTransitionEnd(transition: Transition?) {
                        Log.d(TAG, "Transition Ended")
                        vwOnline.apply {
                            alpha = 0f
                            visibility = View.VISIBLE
                            animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .setListener(object : AnimatorListenerAdapter() {
                                        override fun onAnimationEnd(animation: Animator) {

                                        }
                                    })
                        }
                    }

                    override fun onTransitionResume(transition: Transition?) {

                    }

                    override fun onTransitionPause(transition: Transition?) {

                    }

                    override fun onTransitionCancel(transition: Transition?) {

                    }

                    override fun onTransitionStart(transition: Transition?) {

                    }

                })


                TransitionManager.beginDelayedTransition(
                        vwStatus, transition)
                constraintSetOnline.applyTo(vwMain)

                toggleLocationService(true)
                lblStatus.setText("You are currently online.")
                onlineLabel.setTypeface(typeface, Typeface.BOLD);
                offlineLabel.setTypeface(typeface, Typeface.NORMAL);

           /*  //Add when API 29 is available
                val permissionAccessCoarseLocationApproved = ActivityCompat
                        .checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED

                if (permissionAccessCoarseLocationApproved) {
                    val backgroundLocationPermissionApproved = ActivityCompat
                            .checkSelfPermission(context!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED

                    if (backgroundLocationPermissionApproved) {
                        // App can access location both in the foreground and in the background.
                        toggleLocationService(true)
                        lblStatus.setText("You are currently online.")
                        onlineLabel.setTypeface(null, Typeface.BOLD);
                        offlineLabel.setTypeface(null, Typeface.NORMAL);
                    } else {
                        // App can only access location in the foreground. Display a dialog
                        // warning the user that your app must have all-the-time access to
                        // location in order to function properly. Then, request background
                        // location.
                        val blockUserBuilder = AlertDialog.Builder(context!!)
                        blockUserBuilder.setTitle("Location Required!")
                        blockUserBuilder.setMessage("sway requires your location in order to function properly. Remember, you can stay completely anonymous while searching for your match!")
                        blockUserBuilder.setCancelable(true)
                        blockUserBuilder.setIcon(R.drawable.warning)
                        blockUserBuilder.setPositiveButton(
                                getString(R.string.ok)
                        ) { dialog, id ->
                            ActivityCompat.requestPermissions(this.activity!!,
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    BACKGROUND_REQUEST_CODE)
                        }

                        blockUserBuilder.setNegativeButton(
                                getString(R.string.cancel)
                        ) { dialog, id -> dialog.cancel()
                            switchOnline.isEnabled = false
                            matchMapButton.isEnabled = false
                        }

                        val blockUserAlert = blockUserBuilder.create()
                        blockUserAlert.show()
                        val posButton = blockUserAlert.getButton(AlertDialog.BUTTON_POSITIVE)
                        posButton.setTextColor(Color.DKGRAY)
                        val negButton = blockUserAlert.getButton(AlertDialog.BUTTON_NEGATIVE)
                        negButton.setTextColor(Color.DKGRAY)

                    }
                } else {
                    // App doesn't have access to the device's location at all. Make full request
                    // for permission.
                    ActivityCompat.requestPermissions(this.activity!!,
                            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION),LOCATION_REQUEST_CODE)


                }
                */



            } else {
                // The switch is disabled

                vwOnline.apply {
                    alpha = 1f
                    visibility = View.VISIBLE
                    animate()
                            .alpha(0f)
                            .setDuration(500)
                            .setListener(object : AnimatorListenerAdapter() {
                                override fun onAnimationEnd(animation: Animator) {
                                    val transition = AutoTransition()

                                    transition.duration = 500
                                    TransitionManager.beginDelayedTransition(
                                            vwStatus, transition)
                                    constraintSetOffline.applyTo(vwMain)

                                }
                            })
                }


                toggleLocationService(false)
                lblStatus.setText("You are currently offline.")
                onlineLabel.setTypeface(typeface, Typeface.NORMAL);
                offlineLabel.setTypeface(typeface, Typeface.BOLD);
            }
        }

        btnPhoto.setOnClickListener {
            togglePhoto()
        }

        if (!prefsManager.firstName.isNullOrEmpty())
        {
            welcomeLabel.text = "Welcome " + prefsManager.firstName + "!"
        }

        switchOnline.isChecked = isLocationServiceRunning(swayLocationService::class.java)

        if (isLocationServiceRunning(swayLocationService::class.java))
        {
            TransitionManager.beginDelayedTransition(vwStatus)
            constraintSetOnline.applyTo(vwMain)
        }
        else
        {
            TransitionManager.beginDelayedTransition(vwStatus)
            constraintSetOffline.applyTo(vwMain)
        }

    }

    fun drawPrivacyArea(position: LatLng, size: Int)
    {
        var circle = CircleOptions()
        circle.center(position)
        circle.radius(size.toDouble())
        circle.strokeColor(Color.BLUE)
        //var fillColor = Color.rgb(0.0f,0.0f,0.35f)
        circle.fillColor(0x404e33c4)
        circle.strokeWidth(2.0F)
        //circle.title = "Search Area"
        googleMap.addCircle(circle)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(this.activity!!,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.activity!!,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        this.googleMap = googleMap

        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false

        googleMap.isMyLocationEnabled = true


        fusedLocationClient.lastLocation.addOnSuccessListener() { location ->
            // Got last known location. In some rare situations this can be null.

            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                val markerOptions = MarkerOptions().position(currentLatLng)

                //change color or marker
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))

                //Use a custom marker image
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                //        BitmapFactory.decodeResource(resources, R.mipmap.ic_user_location)))

                googleMap.addMarker(markerOptions)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f))
            }


        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            BACKGROUND_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    switchOnline.isEnabled = false
                    matchMapButton.isSaveEnabled = false
                    Log.i(TAG, "Background location permission has been denied by user")
                } else {
                    switchOnline.isEnabled = true
                    matchMapButton.isSaveEnabled = true
                    Log.i(TAG, "Background location permission has been granted by user")
                }
            }
            LOCATION_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    switchOnline.isEnabled = false
                    matchMapButton.isSaveEnabled = false
                    Log.i(TAG, "Location permission has been denied by user")
                } else {
                    switchOnline.isEnabled = true
                    matchMapButton.isSaveEnabled = true
                    Log.i(TAG, "Location permission has been granted by user")
                }
            }
        }
    }

    fun togglePhoto()
    {
        val json = JSONObject()
        json.put("UserID", prefsManager.userID.toString())
        json.put("PhotoCredits", "")
        json.put("HideDate", "")
        json.put("HidePhotos", !isPhotoHidden)
        json.put("HideDateDiff", "")

        HttpTask {
            if (it == null) {
                println("connection error")
                buildCustomDialog(getString(R.string.search_error), getString(R.string.there_was_a_problem), getString(R.string.ok))
                return@HttpTask
            }
            println(it)
            var gson = Gson()
            var dict: HashMap<String, Any?> = gson.fromJson(it, object : TypeToken<HashMap<String,Any?>>() {}.type)

            checkPhotoVisibility()

        }.execute("POST", getString(R.string.sway_api_base) + "Photo/TogglePhotos", json.toString())

    }

    fun checkPhotoVisibility()
    {
        val json = JSONObject()
        json.put("UserID", prefsManager.userID.toString())

        HttpTask {
            if (it == null) {
                println("connection error")
                buildCustomDialog(getString(R.string.search_error), getString(R.string.there_was_a_problem), getString(R.string.ok))
                return@HttpTask
            }
            println(it)
            var gson = Gson()
            var dict: HashMap<String, Any?> = gson.fromJson(it, object : TypeToken<HashMap<String,Any?>>() {}.type)

            if (!dict.isNullOrEmpty())
            {
                var strTime: String = ""
                var strCredits: String = ""
                val hideDateDiff = dict["hideDateDiff"].toString().toDouble().roundToInt()
                val photoCredits = dict["photoCredits"].toString().toDouble().roundToInt()
                myPhoto = dict["photoURL"].toString()
                val hidePhotos = dict["hidePhotos"].toString().toBoolean()

                if (hideDateDiff < 0)
                {
                    if (hidePhotos)
                    {
                        isPhotoHidden = true;
                        btnPhoto.setText("Unhide Your Photo")
                        lblPhotoStatus.text = "Your photo is hidden."
                        lblCredits.text = ""
                        lblHours.text = ""
                        myPhoto = ""
                        imgPhoto.setImageResource(R.drawable.stock_photo)
                    }
                    else
                    {
                        isPhotoHidden = false;
                        lblPhotoStatus.text = "Your photo is visible."
                        btnPhoto.setText("Hide Your Photo")
                        Glide.with(this).load(myPhoto).placeholder(R.drawable.stock_photo).transform(CircleCrop()).into(imgPhoto)

                        if (abs(hideDateDiff) == 1)
                        {
                            strTime = (abs(hideDateDiff)).toString()  + " hour remaining."
                        }
                        else
                        {
                            strTime = (abs(hideDateDiff)).toString()  + " hours remaining."
                        }
                    }
                    if (photoCredits == 1)
                    {
                        strCredits = photoCredits.toString() + " credit left."
                    }
                    else
                    {
                        strCredits = photoCredits.toString() + " credits left."
                    }
                }
                else
                {
                    isPhotoHidden = true
                    myPhoto = ""
                    imgPhoto.setImageResource(R.drawable.stock_photo)
                    if (photoCredits == 1)
                    {
                        strCredits = photoCredits.toString() + " credit left."
                    }
                    else
                    {
                        strCredits = photoCredits.toString() + " credits left."
                    }
                }
                //self.refreshMyLocationMap()

                lblCredits.text = strCredits
                lblHours.text = strTime
            }

        }.execute("POST", getString(R.string.sway_api_base) + "Photo/CheckPhotoHidden?UserID=" + prefsManager.userID.toString(), json.toString())

    }

    fun buildCustomDialog(message: String, title: String, buttonTitle: String) {
        val messageTV: TextView
        val titleTV: TextView
        val acceptButton: Button
        val factory = LayoutInflater.from(this.activity)
        val dialogoView = factory.inflate(
                R.layout.simple_dialog_layout, null)
        val dialogo = AlertDialog.Builder(context!!).create()
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

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context


    }

    override fun onDetach() {
        super.onDetach()
    }

    fun toggleLocationService(online: Boolean) {
        val i = Intent(mainContext, swayLocationService::class.java)
        if (!online) {
            activity!!.stopService(i)

            Toast.makeText(mainContext, "Location Updates Stopped.", Toast.LENGTH_LONG).show()
        } else {
            activity!!.startForegroundService(i)
            Toast.makeText(mainContext, "Location Updates Started.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = activity!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = "HomeScreen"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}

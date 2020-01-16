package com.swayapp

//import android.widget.Toolbar
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.widget.ShareDialog
import com.swayapp.fragments.HomeFragment
import com.swayapp.helpers.swayLocationService
import com.swayapp.helpers.SharedPreferencesManager
import com.google.android.material.navigation.NavigationView

/**
 * Created by javiermartinez on 8/17/17.
 */

class CoreActivity : AppCompatActivity() {
    internal lateinit var fragmentManager: FragmentManager
    internal lateinit var fragmentTransaction: FragmentTransaction
    internal lateinit var fragmentContainer: FrameLayout
    private var prefsManager: SharedPreferencesManager? = null
    //Navigation drawer
    private var mDrawer: DrawerLayout? = null
    private var toolbar: Toolbar? = null
    private var nvDrawer: NavigationView? = null
    private var drawerToggle: ActionBarDrawerToggle? = null
    private val mRegistrationBroadcastReceiver: BroadcastReceiver? = null
    private var onBackPressedListener: OnBackPressedListener? = null
    var userStatus: Int = 0
    //FACEBOOK SIGN IN
    internal lateinit var callbackManager: CallbackManager

    interface OnBackPressedListener {
        fun doBack()
    }

    fun setOnBackPressedListener(onBackPressedListener: OnBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // The action bar home/up action should open or close the drawer.
        when (item.itemId) {
            android.R.id.home -> {
                mDrawer!!.openDrawer(GravityCompat.START)
                return true
            }
        }

        return if (drawerToggle!!.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Facebook initializer
        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()
        userStatus = intent.extras!!.getInt("userStatus", -1)
        Log.d(TAG, "User status is: $userStatus")
        prefsManager = SharedPreferencesManager(this)
        setContentView(R.layout.core_activity_layout)
        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Find our drawer view
        mDrawer = findViewById(R.id.drawer_layout) as DrawerLayout

        // Find our drawer view
        nvDrawer = findViewById(R.id.nvView) as NavigationView
        // Setup drawer view
        setupDrawerContent(nvDrawer!!)

        drawerToggle = setupDrawerToggle()


        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer!!.addDrawerListener(drawerToggle!!)
        nvDrawer!!.itemIconTintList = null

        fragmentContainer = findViewById(R.id.fragmentContainer) as FrameLayout
        //First fragment setup

        val homeFragment = HomeFragment()
        fragmentManager = supportFragmentManager
        fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, homeFragment, "HOMEFRAGMENT")
        fragmentTransaction.commit()


        /*        Log.d(TAG,"Setting broadcast receiver");
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG,"Received something but dont know what!");
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    //Received a token code
                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                    Log.d(TAG,"Message received: "+message);
                }
            }
        };*/
        prefsManager!!.printAllValues()
    }

    override fun onResume() {
        super.onResume()
        /*        // register GCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());*/
    }

    private fun displayFirebaseRegId() {
        // Log.e(TAG, "Firebase reg id: " + prefsManager.getUserFirebaseId());
    }

    private fun setupDrawerToggle(): ActionBarDrawerToggle {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_closed)
    }

    private fun setupDrawerContent(navigationView: NavigationView) {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    fun selectDrawerItem(menuItem: MenuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        var isLogout: Boolean? = false
        var fragment: Fragment? = null
        val fragmentClass: Class<*>? = null
        var duplicateFragmentInstance: Boolean? = false
        var fragmentTAG = ""
        when (menuItem.itemId) {
            R.id.home ->
            {
                fragmentTAG = "HOME_FRAGMENT"
                mDrawer!!.closeDrawers()
            }

            R.id.share ->
            {
                fragmentTAG = "SHARE"
                val shareDialog: ShareDialog
                //FacebookSdk.sdkInitialize(this@Activity)
                shareDialog = ShareDialog(this)
                val linkContent = ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.rovio.baba&hl=en_US")).build()

                callbackManager = CallbackManager.Factory.create()
                shareDialog.registerCallback(callbackManager, object : FacebookCallback<Sharer.Result?> {
                    override fun onSuccess(result: Sharer.Result?) {
                        Toast.makeText(applicationContext, "Thanks for sharing!", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Fb onSuccess")
                    }

                    override fun onCancel() {
                        Toast.makeText(applicationContext, "Oh no!", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Fb onCancel")
                    }

                    override fun onError(error: FacebookException) {
                        Toast.makeText(applicationContext, "Error Sharing!", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Fb onError")
                    }
                }, 90)

                shareDialog.show(linkContent)
            }
            R.id.signOut -> isLogout = true
        }

        try {
            if ((isLogout == false)!!) {
                fragment = fragmentClass!!.newInstance() as Fragment
                // Insert the fragment by replacing any existing fragment
                val fragmentManager = supportFragmentManager
                //get rid of all stacked fragments before moving from a drawer selection
                val fragments = supportFragmentManager.fragments
                for (i in fragments.indices) {
                    if (fragments[i].tag !== fragmentTAG) {
                        fragmentManager.beginTransaction().remove(supportFragmentManager.findFragmentByTag(fragments[i].tag)!!).commit()
                    } else {
                        //if we found the tag, it means there is an instance of this fragment already
                        duplicateFragmentInstance = true
                    }
                }

                if ((duplicateFragmentInstance == false)!!) {
                    fragmentManager.beginTransaction().add(R.id.fragmentContainer, fragment, fragmentTAG).commit()
                }
                // Highlight the selected item has been done by NavigationView
                menuItem.isChecked = true
                // Set action bar title
                title = menuItem.title
            } else {
                logout()
            }
            // Close the navigation drawer
            mDrawer!!.closeDrawers()

        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    override fun onPostResume() {
        super.onPostResume()
    }

    override fun onBackPressed() {
        if (onBackPressedListener != null)
            onBackPressedListener!!.doBack()
        else {
            Log.d("CoreAct", "Back from core activity")
            //logout()
            mDrawer!!.closeDrawers()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle!!.syncState()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Pass any configuration change to the drawer toggles
        drawerToggle!!.onConfigurationChanged(newConfig)
    }

    override fun onDestroy() {
        onBackPressedListener = null
        super.onDestroy()
    }

    override fun onPause() {
        if (mRegistrationBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver)
        }
        super.onPause()
    }

    fun logout() {
        val logOutUserBuilder = AlertDialog.Builder(this)
        logOutUserBuilder.setTitle(getString(R.string.logout_title))
        logOutUserBuilder.setMessage(getString(R.string.logout_message))
        logOutUserBuilder.setCancelable(true)
        logOutUserBuilder.setIcon(R.drawable.warning)
        logOutUserBuilder.setPositiveButton(
                getString(R.string.accept)
        ) { dialog, id ->
            //wipe all customer data, clear facebook login
            prefsManager!!.wipeCustomerData()
            LoginManager.getInstance().logOut()
            val s = Intent(this, swayLocationService::class.java)
            this!!.stopService(s)
            //send to loginScreen
            val i = Intent(this@CoreActivity, LandingPage::class.java)
            startActivity(i)
            prefsManager!!.printAllValues()
            finish()
        }

        logOutUserBuilder.setNegativeButton(
                getString(R.string.cancel)
        ) { dialog, id -> dialog.cancel() }

        val logOutUserAlert = logOutUserBuilder.create()
        logOutUserAlert.show()
        val posButton = logOutUserAlert.getButton(AlertDialog.BUTTON_POSITIVE)
        posButton.setTextColor(Color.DKGRAY)
        val negButton = logOutUserAlert.getButton(AlertDialog.BUTTON_NEGATIVE)
        negButton.setTextColor(Color.DKGRAY)
    }

    companion object {
        val TAG = "CoreActivity"
    }


}

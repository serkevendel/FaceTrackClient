package com.vsoft.trackify.activity

import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.GraphRequest
import com.vsoft.trackify.R
import com.vsoft.trackify.adapter.FriendsAdapter
import com.vsoft.trackify.model.FriendsContainer
import com.vsoft.trackify.model.User
import com.vsoft.trackify.service.LocationSharingService
import com.vsoft.trackify.util.PermissionUtils
import com.vsoft.trackify.util.ResolvableApiExceptionHolder


class FriendListActivity : AppCompatActivity() {
    private var userTextView: TextView? = null
    private lateinit var currentUser: User
    private var accessTokenTracker: AccessTokenTracker? = null
    private lateinit var searchView: SearchView

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: FriendsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val REQUEST_CHECK_SETTINGS = 12

    private val broadcastReceiver: BroadcastReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val exception = ResolvableApiExceptionHolder.resolvableApiException
            exception.startResolutionForResult(this@FriendListActivity,REQUEST_CHECK_SETTINGS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        userTextView = findViewById(R.id.textView_user)
        requestUserAndSetGreeting(AccessToken.getCurrentAccessToken())
        registerTokenChangedHandler()

        viewManager = LinearLayoutManager(this)
        viewAdapter = FriendsAdapter(FriendsContainer.friends, object : FriendsAdapter.FriendsAdapterListener {
            override fun onFriendSelected(user: User) {
                Toast.makeText(applicationContext, "Selected friend with name: " + user.name, Toast.LENGTH_SHORT).show()
            }
        })

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView_friends).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        //addDummyFriends() -for testing
        getFriends()
    }

    private fun tryToStartLocationService() {
        if (PermissionUtils.checkForLocationPermissions(this)) {
            enableSharing()
        } else {
            //Clear shared preferences so that a previous entry doesnt make sharing enable when the user did not give permission
            disableSharing()
            PermissionUtils.requestPermissions(this)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                object: IntentFilter("com.vsoft.trackify.service.RESOLVABLE_API_EXCEPTION"){})
    }

    override fun onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(broadcastReceiver)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == REQUEST_CHECK_SETTINGS){
            //If user approved the settings, send back a broadcast to the service so it can continue
            val intent = Intent("com.vsoft.trackify.activity.RESOLVABLE_API_EXCEPTION_RESOLVED")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        } else {
            disableSharing()
            Toast.makeText(this,"Location sharing is disabled because you have not approved the settings!",Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PermissionUtils.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableSharing()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView = menu?.findItem(R.id.menu_search)?.actionView as SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.maxWidth = Integer.MAX_VALUE

        // listening to search query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // filter recycler view when query submitted
                viewAdapter.filter.filter(query)
                return false
            }

            override fun onQueryTextChange(query: String): Boolean {
                // filter recycler view when text is changed
                viewAdapter.filter.filter(query)
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.menu_settings -> {
                showSettingsFragment()
                true
            }
            R.id.menu_search -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // close search view on back button pressed
        if (!searchView.isIconified) {
            searchView.isIconified = true
            return
        }
        super.onBackPressed()
    }

    private fun switchToFacebookLoginActivity() {
        val intent = Intent(this, FacebookLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    private fun requestUserAndSetGreeting(accessToken: AccessToken?) {
        val request = GraphRequest.newMeRequest(
                accessToken
        ) { jsonObject, _ ->
            val userName = jsonObject.getString("first_name")
            val fullName = jsonObject.getString("name")
            val id = jsonObject.getInt("id")
            currentUser = User(id.toString(), fullName)
            userTextView?.text = "$userName!"
            tryToStartLocationService()
        }
        val parameters = Bundle()
        parameters.putString("fields", "id,name,first_name")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun getFriends() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val friendRequest = GraphRequest.newMyFriendsRequest(
                accessToken
        ) { jsonArray, _ ->
            // Application code for users friends
            FriendsContainer.clear()
            for (i in 0 until jsonArray.length()) {
                val friendJsonObject = jsonArray.getJSONObject(i)
                val friend = User(friendJsonObject.getInt("id").toString(), friendJsonObject.getString("name"))
                FriendsContainer.addFriend(friend)
            }
            viewAdapter.notifyDataSetChanged()
        }
        friendRequest.executeAsync()
    }

    private fun sharingIsEnabled(): Boolean {
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        return sharedPref.getBoolean(getString(R.string.toggle_sharing), false)
    }

    private fun disableSharing() {
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(getString(com.vsoft.trackify.R.string.toggle_sharing), false)
            apply()
        }
        //stopService(Intent(this,LocationSharingService::class.java))
    }

    private fun enableSharing() {
        val sharedPref = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean(getString(com.vsoft.trackify.R.string.toggle_sharing), true)
            apply()
        }
        startService(Intent(this,LocationSharingService::class.java).apply { putExtra("user",currentUser) })
    }

    private fun registerTokenChangedHandler() {
        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?,
                                                     currentAccessToken: AccessToken?) {
                if (currentAccessToken == null) {
                    switchToFacebookLoginActivity()
                }
            }
        }
        (accessTokenTracker as AccessTokenTracker).startTracking()
    }

    private fun showSettingsFragment() {
        val intent = Intent(this, SettingsActivity::class.java)
        intent.putExtra("user", currentUser)
        startActivity(intent)
    }
}



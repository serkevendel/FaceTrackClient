package com.vsoft.trackify.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.GraphRequest
import com.vsoft.trackify.R
import com.vsoft.trackify.adapter.FriendsAdapter
import com.vsoft.trackify.friends.FriendsContainer
import com.vsoft.trackify.model.User


class FriendListActivity : Activity() {
    var userTextView: TextView? = null
    var userName: String = ""
    var accessTokenTracker: AccessTokenTracker? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: FriendsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        userTextView = findViewById(R.id.textView_user)
        requestUserNameAndSetGreeting(AccessToken.getCurrentAccessToken())

        registerTokenChangedHandler()

        viewManager = LinearLayoutManager(this)
        viewAdapter = FriendsAdapter(FriendsContainer.friends)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView_friends).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        //addDummyFriends() -for testing
        getFriends()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.options_menu, menu)
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
                Toast.makeText(this, "Search selected!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (userName == "") {
            requestUserNameAndSetGreeting(AccessToken.getCurrentAccessToken())
            userTextView?.text = userName
        }
    }

    private fun switchToFacebookLoginActivity() {
        val intent = Intent(this, FacebookLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun addDummyFriends() {
        val user1 = User(1234, "Dummy Name1")
        val user2 = User(2345, "Dummy Name2")
        FriendsContainer.addFriend(user1)
        FriendsContainer.addFriend(user2)
        viewAdapter.notifyDataSetChanged()
    }

    private fun requestUserNameAndSetGreeting(accessToken: AccessToken?) {
        val request = GraphRequest.newMeRequest(
                accessToken
        ) { jsonObject, response ->
            userName = jsonObject.getString("first_name")
            userTextView?.text = "$userName!"
        }
        val parameters = Bundle()
        parameters.putString("fields", "first_name")
        request.parameters = parameters
        request.executeAsync()
    }

    private fun getFriends() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val friendRequest = GraphRequest.newMyFriendsRequest(
                accessToken
        ) { jsonArray, response ->
            // Application code for users friends
            FriendsContainer.clear()
            for (i in 0 until jsonArray.length()) {
                val friendJsonObject = jsonArray.getJSONObject(i)
                val friend = User(friendJsonObject.getInt("id"), friendJsonObject.getString("name"))
                FriendsContainer.addFriend(friend)
            }
            viewAdapter.notifyDataSetChanged()
        }
        friendRequest.executeAsync()
    }

    private fun isTokenValid(): Boolean {
        return AccessToken.getCurrentAccessToken() != null
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

    }
}



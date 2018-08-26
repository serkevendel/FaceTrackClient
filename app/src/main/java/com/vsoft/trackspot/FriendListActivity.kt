package com.vsoft.trackspot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.GraphRequest
import com.vsoft.trackspot.adapter.FriendsAdapter
import com.vsoft.trackspot.friends.FriendsContainer


class FriendListActivity : Activity() {
    var userTextView: TextView? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: FriendsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        val accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(oldAccessToken: AccessToken?,
                                                     currentAccessToken: AccessToken?) {
                if (currentAccessToken == null) {
                    switchToFacebookLoginActivity()
                }
            }
        }
        accessTokenTracker.startTracking()

        val userId = intent.getStringExtra(USER_ID)

        userTextView = findViewById<TextView>(R.id.textView_user).apply {
            text = userId
        }

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
}



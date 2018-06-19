package com.vsoft.trackspot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker
import com.facebook.GraphRequest
import com.vsoft.trackspot.dummy.DummyContent


class FriendListActivity : Activity(), UserListFragment.OnListFragmentInteractionListener {
    override fun onListFragmentInteraction(item: DummyContent.DummyItem?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var userTextView: TextView? = null
    var friends: MutableList<User> = mutableListOf<User>()

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

        requestFriends(userId)


    }

    private fun switchToFacebookLoginActivity() {
        val intent = Intent(this, FacebookLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun requestFriends(userId: String) {
        val accessToken = AccessToken.getCurrentAccessToken()
        val request = GraphRequest.newGraphPathRequest(
                accessToken,
                "/$userId/friends"
        ) {
            val friendDataArray = it.jsonObject.getJSONArray("data")
            for (i in 0..(friendDataArray.length() - 1)) {
                val friendDataObject = friendDataArray.getJSONObject(i)
                val friend = User()
                friend.id = friendDataObject.getInt("id")
                friend.name = friendDataObject.getString("name")
                friends.add(friend)
                // Your code here
            }
            Toast.makeText(this, "friends are updated!", Toast.LENGTH_SHORT).show()
        }

        request.executeAsync()
    }
}

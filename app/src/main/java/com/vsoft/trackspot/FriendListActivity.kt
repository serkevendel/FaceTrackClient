package com.vsoft.trackspot

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.facebook.AccessToken
import com.facebook.AccessTokenTracker

class FriendListActivity : Activity() {

    var userTextView: TextView? = null

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
    }

    private fun switchToFacebookLoginActivity() {
        val intent = Intent(this, FacebookLoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

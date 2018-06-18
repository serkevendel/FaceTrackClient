package com.vsoft.trackspot

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class FriendListActivity : Activity() {

    var userTextView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_list)

        val userId = intent.getStringExtra(USER_ID)

        userTextView = findViewById<TextView>(R.id.textView_user).apply {
            text = userId
        }
    }
}

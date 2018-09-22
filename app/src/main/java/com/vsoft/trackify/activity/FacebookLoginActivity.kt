package com.vsoft.trackify.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.vsoft.trackify.R

class FacebookLoginActivity : Activity() {

    private val callbackManager = CallbackManager.Factory.create()
    private var loginButton: LoginButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facebook_login)

        loginButton = findViewById(R.id.login_button)
        loginButton?.setReadPermissions("user_photos")

        //Register login callback
        loginButton?.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                switchToFriendListActivity()
            }

            override fun onCancel() {
                Toast.makeText(applicationContext, "Login cancelled!", Toast.LENGTH_SHORT).show()
            }

            override fun onError(error: FacebookException?) {
                Toast.makeText(applicationContext, error?.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        })

        if (isLoggedIn()) {
            switchToFriendListActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun switchToFriendListActivity() {
        val intent = Intent(this, FriendListActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        return accessToken != null
    }
}

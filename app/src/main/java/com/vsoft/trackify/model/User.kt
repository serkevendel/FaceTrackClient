package com.vsoft.trackify.model

import java.io.Serializable

data class User(var id: String, var name: String): Serializable {
    lateinit var profilePictureUrl: String
}

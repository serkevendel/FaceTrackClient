package com.vsoft.trackspot.friends

import com.vsoft.trackspot.User

object FriendsContainer {

    /**
     * An array of sample (dummy) items.
     */
    val friends: MutableList<User> = ArrayList()

    public fun addFriend(user: User){
        friends.add(user)
    }

    public fun removeFriend(user: User){
        friends.remove(user)
    }

    public fun clear() {
        friends.clear()
    }
}
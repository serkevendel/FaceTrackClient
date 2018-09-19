package com.vsoft.trackify.model

object FriendsContainer {

    /**
     * An array of users representing the friends of the logged in user
     */
    var friends: MutableList<User> = ArrayList()

    fun addFriend(user: User) {
        friends.add(user)
    }

    fun removeFriend(user: User) {
        friends.remove(user)
    }

    fun clear() {
        friends.clear()
    }
}
package com.vsoft.trackspot.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.vsoft.trackspot.R
import com.vsoft.trackspot.User

class FriendsAdapter(var friendsDataSet: MutableList<User>) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    fun getDataSet(): MutableList<User> {
        return friendsDataSet
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView? = view.findViewById(R.id.textView_friend_name)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): FriendsAdapter.ViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.friend_list_item, parent, false)
        // set the view's size, margins, paddings and layout parameters
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView?.text = friendsDataSet[position].name
    }

    override fun getItemCount() = friendsDataSet.size
}
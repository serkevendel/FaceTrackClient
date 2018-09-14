package com.vsoft.trackify.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.vsoft.trackify.R
import com.vsoft.trackify.model.User


class FriendsAdapter(var friendsDataSet: MutableList<User>, var friendsAdapterListener: FriendsAdapterListener) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>(), Filterable {

    var filteredDataSet: MutableList<User> = friendsDataSet

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                if (charSequence != null) {
                    filteredDataSet = if (charSequence.isEmpty()) {
                        friendsDataSet
                    } else {
                        friendsDataSet.filter { it.name.contains(charSequence, true) }.toMutableList()
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = filteredDataSet
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filteredDataSet = filterResults.values as MutableList<User>
                notifyDataSetChanged()
            }

        }
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        var textView: TextView? = view.findViewById(R.id.textView_friend_name)

        init {
            view.setOnClickListener {
                // send selected contact in callback
                friendsAdapterListener.onFriendSelected(filteredDataSet[adapterPosition])
            }
        }
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
        holder.textView?.text = filteredDataSet[position].name
    }

    override fun getItemCount() = filteredDataSet.size

    interface FriendsAdapterListener {
        fun onFriendSelected(user: User)
    }
}
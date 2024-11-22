package com.reisiegel.volleyballhelper.ui.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.R

class PlayerItem(private var jersey: Int = 0, private var name: String = "") {
    fun getJersey(): String {
        return jersey.toString()
    }

    fun setJersey(jersey: Int) {
        this.jersey = jersey
    }

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }
}

class PlayerAdapter(private var items: MutableList<PlayerItem>): RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_layout, parent, false) //TODO: Create layout
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val item = items[position]
        holder.neme.text = item.getName()
        holder.jerseNumber.text = item.getJersey()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addPlayer(player: PlayerItem) {
        items.add(player)
        notifyItemInserted(items.size - 1)
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //TODO: Implement ViewHolder
    }
}
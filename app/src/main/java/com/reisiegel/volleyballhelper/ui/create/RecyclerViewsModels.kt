package com.reisiegel.volleyballhelper.ui.create

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.R

class PlayerItem(private var jersey: String = "", private var name: String = "") {
    fun getJersey(): Int {
        return jersey.toInt()
    }
    fun setJersey(jersey: Int) {
        try {
            this.jersey = jersey.toString()
        } catch (e: NumberFormatException) {
            this.jersey = "0"
        }
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_item_layout, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.getName()
        holder.jerseyNumber.text = item.getJersey().toString()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addPlayer(player: PlayerItem) {
        items.add(player)
        notifyItemInserted(items.size - 1)
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.playerName)
        val jerseyNumber: TextView = itemView.findViewById(R.id.jerseyNumber)
    }

    fun updateItems(newItems: MutableList<PlayerItem>){
        items = newItems
        notifyItemInserted(items.size - 1)
    }
}

class MatchItem(private var opponent: String = "", private var startTime: String = "") {
    fun getOpponent(): String {
        return opponent
    }

    fun setOpponent(opponent: String) {
        this.opponent = opponent
    }

    fun getStartTime(): String {
        return startTime
    }

    fun getStartDate():  String{
        val startDateParts = startTime.split(" ")
        return startDateParts.take(3).joinToString(" ",)
    }

    fun setStartTime(startTime: String) {
        this.startTime = startTime
    }
}

class MatchAdapter(private var items: MutableList<MatchItem>/*, private val onDeleteClick: (MatchItem) -> Unit*/): RecyclerView.Adapter<MatchAdapter.MatchViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.match_item_layout, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = items[position]
        holder.opponent.text = item.getOpponent()
        holder.startTime.text = item.getStartTime()

        /*holder.deleteButton.setOnClickListener{
            onDeleteClick(item)
        }*/
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addMatch(match: MatchItem) {
        items.add(match)
        notifyItemInserted(items.size - 1)
    }

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val opponent: TextView = itemView.findViewById(R.id.opponent)
        val startTime: TextView = itemView.findViewById(R.id.startTime)
        //val deleteButton: TextView = itemView.findViewById(R.id.matchDeleteButton)
    }

    fun updateItems(newItems: MutableList<MatchItem>){
        items = newItems
        notifyItemInserted(items.size - 1)
    }

    fun updateAllList(newItems: MutableList<MatchItem>){
        //val olItems = items
        items.clear()
        items.addAll(newItems)
        notifyItemInserted(items.size - 1)
    }
}
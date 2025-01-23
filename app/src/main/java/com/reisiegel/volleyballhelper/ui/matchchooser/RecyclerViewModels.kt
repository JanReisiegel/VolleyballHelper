package com.reisiegel.volleyballhelper.ui.matchchooser

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.models.SelectedTournament

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

class MatchAdapter(private var items: MutableList<MatchItem>, private val context: Context?, private val view: View/*, private val onDeleteClick: (MatchItem) -> Unit*/): RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.match_item_layout, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = items[position]
        holder.opponent.text = item.getOpponent()
        holder.startTime.text = item.getStartTime()
        if (context != null){
            holder.matchLine.setOnLongClickListener {
                val dialog = AlertDialog.Builder(context?:return@setOnLongClickListener false)
                    .setTitle("Long click " + item.getOpponent())
                    .setMessage(item.getStartTime())
                    .setPositiveButton("OK"){
                            dialog, _ -> dialog.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnLongClickListener false
            }
            holder.matchLine.setOnClickListener {
                SelectedTournament.selectedMatchIndex = position
                val matchListLayout = view.findViewById<FrameLayout>(R.id.match_list) ?: return@setOnClickListener
                val statisticsLayout = view.findViewById<FrameLayout>(R.id.match_statistics) ?: return@setOnClickListener
                matchListLayout.visibility = View.INVISIBLE
                statisticsLayout.visibility = View.VISIBLE
                view.requestLayout()
            }
        }

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
        val matchLine: LinearLayout = itemView.findViewById(R.id.match_line)
        //val deleteButton: TextView = itemView.findViewById(R.id.matchDeleteButton)
    }

    fun updateItems(newItems: MutableList<MatchItem>) {
        items = newItems
        notifyItemInserted(items.size - 1)
    }

    fun updateAllList(newItems: MutableList<MatchItem>) {
        //val olItems = items
        items.clear()
        items.addAll(newItems)
        notifyItemInserted(items.size - 1)
    }
}
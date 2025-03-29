package com.reisiegel.volleyballhelper.ui.export

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.reisiegel.volleyballhelper.R
import androidx.recyclerview.widget.RecyclerView

class TournamentItem(private var name: String, private val filePath: String){
    fun getPath():  String {
        return filePath
    }
    fun getName(): String {
        return name
    }
}

class TournamentAdapter(
    private var items: MutableList<TournamentItem>?,
    private val context: Context?,
    private val view: View,/*, private val onDeleteClick: (MatchItem) -> Unit*/
    private val onClickDelete: (Int) -> Unit,
    private val onClickExport: (Int) -> Unit
): RecyclerView.Adapter<TournamentAdapter.TournamentViewHolder>(){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TournamentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.tournament_export_layout, parent, false)
        return TournamentViewHolder(view)
    }

    override fun onBindViewHolder(holder: TournamentViewHolder, position: Int) {
        val item = items?.get(position)
        holder.tournamentName.text = item?.getName()
//        holder.deleteButton.setOnClickListener {
//            onClickDelete(position)
//        }
//        holder.exportButton.setOnClickListener {
//            onClickExport(position)
//        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class TournamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tournamentName: TextView = itemView.findViewById(R.id.tournament_name)
        //val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        //val exportButton: Button = itemView.findViewById(R.id.export_button)
    }
}
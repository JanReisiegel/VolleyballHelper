package com.reisiegel.volleyballhelper.ui.export

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.reisiegel.volleyballhelper.R
import androidx.recyclerview.widget.RecyclerView

class TournamentItem(private var name: String, private val filePath: String){
    fun getPath(): String {
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
    private val onClickExport: (String) -> Unit
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
        holder.tournamentLine.setOnClickListener {
            Log.d("ExportStatistics", "Clicked tournament: ${item?.getName()}")
            onClickExport(item?.getPath() ?: return@setOnClickListener)
        }
//        holder.deleteButton.setOnClickListener {
//            onClickDelete(position)
//        }
//        holder.exportButton.setOnClickListener {
//            Log.d("ExportStatistics", "Exporting tournament: ${item?.getName()}")
//            onClickExport(position)
//        }
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class TournamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tournamentName: TextView = itemView.findViewById(R.id.tournament_name)
        val tournamentLine: LinearLayout = itemView.findViewById(R.id.tournament_line)
//        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
//        val exportButton: Button = itemView.findViewById(R.id.export_button)
    }
}
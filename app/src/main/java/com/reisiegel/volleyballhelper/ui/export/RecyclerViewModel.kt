package com.reisiegel.volleyballhelper.ui.export

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.reisiegel.volleyballhelper.R
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.models.Tournament
import com.reisiegel.volleyballhelper.ui.export.TournamentAdapter.TournamentViewHolder
import java.io.File

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
    private val view: View,
    private val onDeleteClick: (String) -> Unit,
    private val onClickExport: (String) -> Unit,
    private val onClickEditPlayers: (String, View) -> Unit
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
//        holder.tournamentLine.setOnClickListener {
//            Log.d("ExportStatistics", "Clicked tournament: ${item?.getName()}")
//            onClickExport(item?.getPath() ?: return@setOnClickListener)
//        }
        holder.deleteButton.setOnClickListener {
            Log.d("ExportStatistics", "Delete tournament: ${item?.getName()}")
            onDeleteClick(item?.getPath() ?: return@setOnClickListener)
        }
        holder.exportButton.setOnClickListener {
            Log.d("ExportStatistics", "Exporting tournament: ${item?.getName()}")
            onClickExport(item?.getPath() ?: return@setOnClickListener)
        }
        holder.editPlayersButton.setOnClickListener {
            onClickEditPlayers(item?.getPath() ?: return@setOnClickListener, view)
        }

    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class TournamentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tournamentName: TextView = itemView.findViewById(R.id.tournament_name)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
        val exportButton: Button = itemView.findViewById(R.id.export_button)
        val editPlayersButton: Button = itemView.findViewById(R.id.edit_players_button)
    }
}

class PlayerItem(
    private var jerseyNumber: Int,
    private var name: String,
    private var oldJerseyNumber: Int
) {
    fun getName(): String {
        return name
    }
    fun getJerseyNumber(): Int {
        return jerseyNumber
    }
    fun setName(name: String) {
        this.name = name
    }
    fun setJerseyNumber(jerseyNumber: Int) {
        this.jerseyNumber = jerseyNumber
    }

    fun getOldJerseyNumber(): Int {
        return oldJerseyNumber
    }
}

class PlayerAdapter(
    private var items: MutableList<PlayerItem>?,
    private val context: Context?,
    private val view: View
): RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlayerViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.edit_player_line, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int){
        val playerItem = items?.get(position)
        holder.editName.setText(playerItem?.getName())
        holder.editNumber.setText(playerItem?.getJerseyNumber().toString())
        holder.editName.setOnClickListener {
            holder.editName.requestFocus()
        }
        holder.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().isNotEmpty())
                    playerItem?.setName(s.toString())
                else
                    holder.editName.error = context?.getString(R.string.player_name_input_error)
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    holder.editName.error = context?.getString(R.string.player_name_input_error)
                }
            }
        })
        holder.editNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val number = s.toString().toIntOrNull()
                if (number != null) {
                    playerItem?.setJerseyNumber(number)
                } else{
                    holder.editNumber.error = context?.getString(R.string.player_jersey_input_error)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    holder.editNumber.error = context?.getString(R.string.player_jersey_input_error)
                }
            }
        })
    }

    fun getUpdatedPlayers(): MutableList<PlayerItem>? {
        return items
    }

    override fun getItemCount(): Int {
        return items?.size ?: 0
    }

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val editNumber: EditText = itemView.findViewById(R.id.edit_player_jersey)
        val editName: EditText = itemView.findViewById(R.id.edit_player_name)
    }
}
package com.reisiegel.volleyballhelper.ui.matchchooser

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.models.SelectedTournament
import java.io.File
import java.util.Calendar

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

    fun getStartDate(): String {
        val startDateParts = startTime.split(" ")
        return startDateParts.take(3).joinToString(" ")
    }

    fun setStartTime(startTime: String) {
        this.startTime = startTime
    }
}

class MatchAdapter(
    private var items: MutableList<MatchItem>,
    private val context: Context?,
    private val view: View,/*, private val onDeleteClick: (MatchItem) -> Unit*/
    private val onClickSet: (Int) -> Unit
) : RecyclerView.Adapter<MatchAdapter.MatchViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MatchViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.match_item_layout, parent, false)
        return MatchViewHolder(view)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        val item = items[position]
        holder.opponent.text = item.getOpponent()
        holder.startTime.text = item.getStartTime()
        if (context != null) {
            holder.matchLine.setOnLongClickListener {
                val dialogView =
                    LayoutInflater.from(context).inflate(R.layout.change_match_dialog_layout, null)

                val opponentName = dialogView.findViewById<EditText>(R.id.opponent_name)
                val startDate = dialogView.findViewById<TextView>(R.id.date_time)
                val startDateButton = dialogView.findViewById<Button>(R.id.match_time)

                opponentName.setText(item.getOpponent())

                startDate.text = item.getStartTime()

                startDateButton.setOnClickListener {
                    showDatePicker { newDateTime ->
                        startDate.text = newDateTime
                    }
                }

                val dialog = AlertDialog.Builder(context ?: return@setOnLongClickListener false)
                    .setView(dialogView)
                    .setPositiveButton("Save") { dialog, _ ->
                        SelectedTournament.selectedTournament?.getMatch(position)?.updateMatchInfo(opponentName.text.toString(), startDate.text.toString())
                        val file = File(context.filesDir, "Statistics/${SelectedTournament.filePath}")
                        SelectedTournament.selectedTournament?.saveJson(file)
                        items[position].setOpponent(opponentName.text.toString())
                        items[position].setStartTime(startDate.text.toString())
                        holder.opponent.text = opponentName.text.toString()
                        holder.startTime.text = startDate.text.toString()
                        view.requestLayout()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnLongClickListener false
            }
            holder.matchLine.setOnClickListener {
                SelectedTournament.selectedMatchIndex = position
                val matchListLayout =
                    view.findViewById<FrameLayout>(R.id.match_list) ?: return@setOnClickListener
                val statisticsLayout = view.findViewById<FrameLayout>(R.id.match_statistics)
                    ?: return@setOnClickListener
                matchListLayout.visibility = View.INVISIBLE
                statisticsLayout.visibility = View.VISIBLE
                view.requestLayout()
                onClickSet(position)
            }
        }

        /*holder.deleteButton.setOnClickListener{
            onDeleteClick(item)
        }*/
    }

    @SuppressLint("DefaultLocale")
    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context ?: return,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%02d. %02d. %d", selectedDay, selectedMonth + 1, selectedYear)
                showTimePicker(formattedDate){
                    fullDateTime -> onDateSelected(fullDateTime)
                }
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showTimePicker(date: String, onTimeSelected: (String) -> Unit) {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        var result = ""
        val timePickerDialog = TimePickerDialog(
            context,
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)

                onTimeSelected("$date $formattedTime")
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
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
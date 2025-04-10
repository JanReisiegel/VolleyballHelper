package com.reisiegel.volleyballhelper.ui.export

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.models.Tournament
import java.io.File

class ExportStatisticsViewModel() : ViewModel() {
    private val  TAG = "GoogleActivity"
    private val _tournaments = MutableLiveData<MutableList<Tournament>>()
    private val _tournamentsItem = MutableLiveData<MutableList<TournamentItem>>()
    private val _exportTournament = MutableLiveData<Tournament?>()


    val tournaments: LiveData<MutableList<Tournament>> = _tournaments
    val tournamentsItem: LiveData<MutableList<TournamentItem>> = _tournamentsItem
    val exportTournament: LiveData<Tournament?> = _exportTournament

    init{
        //TODO: load tournaments storage
    }

    fun exportTournament(path: String){
        val tournamentFile = File(path)
        val tournament = Tournament.loadFromJson(tournamentFile)
        if (tournament == null) {
            Log.e(TAG, "Tournament from $path not found")
            return
        }
        _exportTournament.value = tournament
    }

    fun deleteTournament(path: String, context: Context){
        Log.d("ExportStatistics", "Deleting tournament: $path")
        val tournamentFile = File(path)
        if (tournamentFile.exists() && tournamentFile.isDirectory.not()){
            tournamentFile.delete()
        }
        loadTournaments(context)
    }

    fun loadTournaments(context: Context){
        val statisticsDirectory = File(context.filesDir, "Statistics")
        if (!statisticsDirectory.isDirectory)
            statisticsDirectory.mkdir()
        val statisticFiles = statisticsDirectory.listFiles()
        val tournamentsTemp = mutableListOf<TournamentItem>()
        statisticFiles?.forEach {file ->
            val fileName = file.name.subSequence(0, file.name.length-5)
            val filePath = file.path
            val tournamentItem = TournamentItem(fileName.toString(), filePath)
            tournamentsTemp.add(tournamentItem)
        }
        _tournamentsItem.value = tournamentsTemp
    }

    fun setTournament(tournament: Tournament?){
        _exportTournament.value = tournament
    }

    @SuppressLint("InflateParams")
    fun editTournamentPlayers(path: String, view: View, context: Context){
        val tournamentFile = File(path)
        val tournament = Tournament.loadFromJson(tournamentFile)
        if (tournament == null) {
            Log.e(TAG, "Tournament from $path not found")
            return
        }

        val playersMap = tournament.getPlayers()
        val players = mutableListOf<PlayerItem>()

        playersMap.keys.forEach { number ->
            val playerItem = PlayerItem(number, playersMap[number] ?: "", number)
            players.add(playerItem)
        }

        val dialogView = LayoutInflater.from(context).inflate(R.layout.edit_players_dialog, null)

        val dialog = Dialog(context)
        dialog.setContentView(dialogView)

        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val desiredWidth = (screenWidth * 0.60).toInt() // 85% of screen width

        dialog.window?.apply {
            setLayout(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            setGravity(Gravity.CENTER)
        }

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        dialogTitle.text = tournament.name

        val playerRecycleView = dialogView.findViewById<RecyclerView>(R.id.player_recycler_view)
        playerRecycleView.layoutManager = LinearLayoutManager(context)

        val playerAdapter = PlayerAdapter(players, context, view)
        playerRecycleView.adapter = playerAdapter
        dialogView.requestLayout()

        val saveButton = dialogView.findViewById<View>(R.id.save_button)
        saveButton.setOnClickListener {
            players.forEachIndexed { index, item ->
                tournament.updatePlayer(item.getOldJerseyNumber(), item.getJerseyNumber(), item.getName())
            }
            tournament.saveJson(tournamentFile)
            dialog.dismiss()
        }
        val cancelButton = dialogView.findViewById<View>(R.id.cancel_button)
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
//        val dialog = AlertDialog.Builder(context)
//            .setView(dialogView)
//            .setTitle(tournament.name)
//            .setPositiveButton(context.getString(R.string.save)) { dialog, _ ->
//                dialog.dismiss()
//            }
//            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
//                dialog.dismiss()
//            }
//            .create()
//
//        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
//        dialog.show()
    }
}
package com.reisiegel.volleyballhelper.ui.export

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
}
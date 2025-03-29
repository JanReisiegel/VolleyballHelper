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
import com.reisiegel.volleyballhelper.services.AuthService
import java.io.File

class ExportStatisticsViewModel() : ViewModel() {

    private val _tournaments = MutableLiveData<MutableList<Tournament>>()
    private val _tournamentsItem = MutableLiveData<MutableList<TournamentItem>>()
    private val _exportTournament = MutableLiveData<Tournament>()


    val tournaments: LiveData<MutableList<Tournament>> = _tournaments
    val tournamentsItem: LiveData<MutableList<TournamentItem>> = _tournamentsItem
    val exportTournament: LiveData<Tournament> = _exportTournament

    init{
        //TODO: load tournaments storage
    }

    fun exportTournament(index: Int){
        val tournament = _tournaments.value?.get(index)
        _exportTournament.value = tournament ?: return
    }

    fun deleteTournament(index: Int){
        //TODO: delete tournament from storage
    }

    fun loadTournaments(context: Context){
        val statisticsDirectory = File(context.filesDir, "Statistics")
        if (!statisticsDirectory.isDirectory)
            statisticsDirectory.mkdir()
        val statisticFiles = statisticsDirectory.listFiles()
        val tournamentsTemp = mutableListOf<TournamentItem>()
        statisticFiles?.forEach {file ->
            val fileName = file.name
            val filePath = file.path
            val tournamentItem = TournamentItem(fileName, filePath)
            tournamentsTemp.add(tournamentItem)
        }
        _tournamentsItem.value = tournamentsTemp
    }
}
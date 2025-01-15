package com.reisiegel.volleyballhelper.ui.create

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reisiegel.volleyballhelper.models.Match
import com.reisiegel.volleyballhelper.models.Tournament
import java.io.File
import java.io.FileOutputStream
import java.text.Normalizer

class CreateViewModel() : ViewModel() {
    // TODO: Implement the ViewModel
    private val _tournamentName = MutableLiveData<String>()
    private val _startMatchTime = MutableLiveData<String>()
    private val _players = MutableLiveData<MutableList<PlayerItem>>(mutableListOf())
    private val _matches = MutableLiveData<MutableList<MatchItem>>(mutableListOf())

    val tournamentName: LiveData<String> = _tournamentName
    val players: LiveData<MutableList<PlayerItem>> = _players
    val matches: LiveData<MutableList<MatchItem>> = _matches
    val startMatchTime: LiveData<String> = _startMatchTime


    fun updateActionName(newActionName: String){
        _tournamentName.value = newActionName
    }
    fun updateStartMatchTime(newStartMatchTime: String){
        _startMatchTime.value = newStartMatchTime
    }

    fun addPlayer(player: PlayerItem){
        val updatedListPlayers = _players.value ?: mutableListOf()
        updatedListPlayers.add(player)
        _players.value = updatedListPlayers
    }

    fun addMatch(match: MatchItem){
        val updatedListMatches = _matches.value ?: mutableListOf()
        updatedListMatches.add(match)
        _matches.value = updatedListMatches
    }

    fun sortMatches(){
        val updatedListMatches = _matches.value ?: mutableListOf()
        updatedListMatches.sortBy { it.getStartTime() }
        _matches.value = updatedListMatches
    }

    fun sortPlayers(){
        val updatedListPlayers = _players.value ?: mutableListOf()
        updatedListPlayers.sortBy { it.getJersey() }
        _players.value = updatedListPlayers
    }

    private fun fileName(): String {
        val normalizedName = Normalizer.normalize(_tournamentName.value.toString(), Normalizer.Form.NFD)
        val normalizedName2 = Normalizer.normalize(tournamentName.value.toString(), Normalizer.Form.NFD)
        val withoutDiacritics = normalizedName.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return withoutDiacritics.replace(" ", "").replace("[^a-zA-Z0-9-]".toRegex(), "") // vyčištění názvu turnaje od diakritiky a mezer
    }

    fun createTournament(context: Context){
        val stringName = fileName()
        val file = File(context.filesDir, "Statistics/${fileName()}.json")
        val players : HashMap<Int, String> = HashMap()
        val matches : ArrayList<Match> = ArrayList()
        _players.value?.forEach {
            players[it.getJersey()] = it.getName()
        }
        _matches.value?.forEach {
            matches.add(Match(it.getOpponent(), ArrayList(), it.getStartTime()))
        }
        val startDate = _matches.value?.get(0)?.getStartTime() ?: ""
        val endDate = _matches.value?.get(_matches.value!!.size - 1)?.getStartTime() ?: ""
        val tournament = Tournament.createTournament(_tournamentName.value!!, matches, players, startDate, endDate)
        tournament.saveJson(file)
    }

    /*fun removeMatch(item: MatchItem){
        val updatedListMatches = _matches.value ?: return
        updatedListMatches.remove(item)
        _matches.value = updatedListMatches
    }*/
}
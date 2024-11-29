package com.reisiegel.volleyballhelper.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateViewModel : ViewModel() {
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

    fun createSheetForTournament(){
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

    /*fun removeMatch(item: MatchItem){
        val updatedListMatches = _matches.value ?: return
        updatedListMatches.remove(item)
        _matches.value = updatedListMatches
    }*/
}
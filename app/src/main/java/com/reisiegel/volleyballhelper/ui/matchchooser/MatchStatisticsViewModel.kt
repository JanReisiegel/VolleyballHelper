package com.reisiegel.volleyballhelper.ui.matchchooser

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reisiegel.volleyballhelper.models.Player
import com.reisiegel.volleyballhelper.models.SelectedTournament
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchItem

class MatchStatisticsViewModel() : ViewModel() {
    private val _matchList = MutableLiveData<MutableList<MatchItem>>()
    private val _playersSquad = MutableLiveData<MutableList<Player?>>()
    private val _playersBench = MutableLiveData<MutableList<Player>>()

    val matchList: LiveData<MutableList<MatchItem>> = _matchList
    val playersSquad: LiveData<MutableList<Player?>> = _playersSquad
    val playersBench: LiveData<MutableList<Player>> = _playersBench

    init {
        val allPlayers = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.players
        if (allPlayers != null){
            val activeSquad: MutableList<Player?> = MutableList(6) { null }
            val bench: MutableList<Player> = mutableListOf()
            allPlayers.forEach(){
                if (SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActiveSquad()
                        ?.contains(it.jerseyNumber) == true){
                    val position = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActiveSquad()
                        ?.indexOf(it.jerseyNumber)
                    activeSquad[position!!] = it
                }
                else{
                    bench.add(it)
                }
            }
            _playersSquad.value = activeSquad
            _playersBench.value = bench
        }
    }

    fun getBanchedPlayers(): MutableList<Player>? {
        return _playersBench.value
    }

    fun addMatchItem(match: MatchItem){
        val updatedMatchList = _matchList.value ?: mutableListOf()
        updatedMatchList.add(match)
        _matchList.value = updatedMatchList
    }

    fun addPlayerToSquad(jerseyNumber: Int, position: Int, change: Boolean = false){
        val player = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(jerseyNumber)
        if (player == null) return
        if (!change) {
            val updatedPlayersSquad = _playersSquad.value ?: mutableListOf()
            updatedPlayersSquad[position] = player
            _playersSquad.value = updatedPlayersSquad

            val updatedPlayersBench = _playersBench.value ?: mutableListOf()
            updatedPlayersBench.remove(player)
            _playersBench.value = updatedPlayersBench
        }
        else{
            val updatedPlayersSquad = _playersSquad.value ?: mutableListOf()
            val playerToBench = updatedPlayersSquad[position]
            updatedPlayersSquad[position] = player
            _playersSquad.value = updatedPlayersSquad

            val updatedPlayersBench = _playersBench.value ?: mutableListOf()
            updatedPlayersBench.add(playerToBench!!)
            updatedPlayersBench.remove(player)
            _playersBench.value = updatedPlayersBench
        }
    }

    fun matchSelected(){
        val allPlayers = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.players
        if (allPlayers != null){
            val activeSquad: MutableList<Player?> = MutableList(6) { null }
            val bench: MutableList<Player> = mutableListOf()
            allPlayers.forEach(){
                if (SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActiveSquad()
                        ?.contains(it.jerseyNumber) == true){
                    val position = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActiveSquad()
                        ?.indexOf(it.jerseyNumber)
                    activeSquad[position!!] = it
                }
                else{
                    bench.add(it)
                }
            }
            _playersSquad.value = activeSquad
            _playersBench.value = bench
        }
    }

    fun containsPlayer(index: Int): Boolean {
        return _playersSquad.value?.get(index) != null
    }

}
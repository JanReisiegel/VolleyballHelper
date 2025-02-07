package com.reisiegel.volleyballhelper.ui.matchchooser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reisiegel.volleyballhelper.models.AttackEnum
import com.reisiegel.volleyballhelper.models.BlockEnum
import com.reisiegel.volleyballhelper.models.Player
import com.reisiegel.volleyballhelper.models.ReceiveServeEnum
import com.reisiegel.volleyballhelper.models.SelectedTournament
import com.reisiegel.volleyballhelper.models.ServeEnum

class MatchStatisticsViewModel() : ViewModel() {
    private val _matchList = MutableLiveData<MutableList<MatchItem>>()
    private val _playersSquad = MutableLiveData<MutableList<Player?>>()
    private val _playersBench = MutableLiveData<MutableList<Player>>()
    private val _serve = MutableLiveData<Boolean>()

    val matchList: LiveData<MutableList<MatchItem>> = _matchList
    val playersSquad: LiveData<MutableList<Player?>> = _playersSquad
    val playersBench: LiveData<MutableList<Player>> = _playersBench
    val serve: LiveData<Boolean> = _serve

    init {
        _serve.value = true
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

    fun matchSelected(index: Int?){
        if(index == null){

            SelectedTournament.selectedMatchIndex = null
            return
        }
        SelectedTournament.selectedMatchIndex = index
        val allPlayers = SelectedTournament.selectedTournament?.getMatch(index)?.players
        if (allPlayers != null){
            val activeSquad: MutableList<Player?> = MutableList(6) { null }
            val bench: MutableList<Player> = mutableListOf()
            allPlayers.forEach(){
                if (SelectedTournament.selectedTournament?.getMatch(index)?.getActiveSquad()
                        ?.contains(it.jerseyNumber) == true){
                    val position = SelectedTournament.selectedTournament?.getMatch(index)?.getActiveSquad()
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

    fun substitution(jerseyNumber: Int, position: Int){
        val player = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(jerseyNumber)
        if (player == null) return

        val updatedPlayersSquad = _playersSquad.value ?: mutableListOf()
        val playerToBench = updatedPlayersSquad[position]
        updatedPlayersSquad[position] = player
        _playersSquad.value = updatedPlayersSquad

        val updatedPlayersBench = _playersBench.value ?: mutableListOf()
        updatedPlayersBench.add(playerToBench!!)
        updatedPlayersBench.remove(player)
        _playersBench.value = updatedPlayersBench

        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)
            ?.addSubstitution(player.jerseyNumber, playerToBench.jerseyNumber)
    }

    fun serveButtonsAction(serveType: ServeEnum, playerNumber: Int): Int {
        if (serveType == ServeEnum.ERROR){
            changeServe()
        }
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerServe(playerNumber, serveType)
        return SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getServeStats(serveType) ?: 0
    }

    fun attackButtonAction(attackType: AttackEnum, playerNumber: Int): Int {
        if ((attackType == AttackEnum.ERROR || attackType == AttackEnum.BLOCK) && serve.value == true){
            changeServe()
        } else if(attackType == AttackEnum.HIT && serve.value == false){
            changeServe()
        }
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerAttack(playerNumber, attackType)
        return SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getAttackStats(attackType) ?: 0
    }

    fun blockButtonAction(blockType: BlockEnum, playerNumber: Int): Int {
        if (blockType == BlockEnum.ERROR && serve.value == true){
            changeServe()
        } else if(blockType == BlockEnum.POINT && serve.value == false){
            changeServe()
        }
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerBlock(playerNumber, blockType)
        return SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getBlockStats(blockType) ?: 0
    }

    fun receiveButtonAction(receiveType: ReceiveServeEnum, playerNumber: Int): Int {
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerReceivedServe(playerNumber, receiveType)
        return SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getReceiveStats(receiveType) ?: 0
    }

    fun changeServe(){
        _serve.value = !_serve.value!!
    }
}
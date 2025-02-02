package com.reisiegel.volleyballhelper.ui.matchchooser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reisiegel.volleyballhelper.models.Player
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchItem

class MatchStatisticsViewModel : ViewModel() {
    private val _matchList = MutableLiveData<MutableList<MatchItem>>()
    private val _playersSquad = MutableLiveData<MutableList<Player>>()

    val matchList: LiveData<MutableList<MatchItem>> = _matchList
    val playersSquad: LiveData<MutableList<Player>> = _playersSquad


    fun addMatchItem(match: MatchItem){
        val updatedMatchList = _matchList.value ?: mutableListOf()
        updatedMatchList.add(match)
        _matchList.value = updatedMatchList
    }


}
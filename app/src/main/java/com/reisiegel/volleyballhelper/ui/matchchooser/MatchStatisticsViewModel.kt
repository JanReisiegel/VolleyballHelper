package com.reisiegel.volleyballhelper.ui.matchchooser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchItem

class MatchStatisticsViewModel : ViewModel() {
    private val _matchList = MutableLiveData<MutableList<MatchItem>>()

    val matchList: LiveData<MutableList<MatchItem>> = _matchList

    fun addMatchItem(match: MatchItem){
        val updatedMatchList = _matchList.value ?: mutableListOf()
        updatedMatchList.add(match)
        _matchList.value = updatedMatchList
    }


}
package com.reisiegel.volleyballhelper.ui.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _actionName = MutableLiveData<String>()
    val actionName: LiveData<String> = _actionName

    private val _numberOfMatches = MutableLiveData<Int>()
    val numberOfMatches: LiveData<Int> = _numberOfMatches

    fun updateActionName(newActionName: String){
        _actionName.value = newActionName
    }
    fun updateNumberOfMatches(newNumberOfMatches: Int){
        _numberOfMatches.value = newNumberOfMatches
    }

    fun createSheetForTournament(){
    }
}
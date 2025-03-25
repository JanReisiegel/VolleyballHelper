package com.reisiegel.volleyballhelper.ui.export

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.models.Tournament
import com.reisiegel.volleyballhelper.services.AuthService

class ExportStatisticsViewModel() : ViewModel() {

    private val _tournament = MutableLiveData<Tournament>()
    val tournament: LiveData<Tournament> = _tournament

    fun setTournament(tournament: Tournament){
        _tournament.value = tournament
    }

}
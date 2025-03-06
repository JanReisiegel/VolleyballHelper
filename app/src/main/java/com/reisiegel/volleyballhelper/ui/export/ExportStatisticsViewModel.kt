package com.reisiegel.volleyballhelper.ui.export

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.services.AuthService

class ExportStatisticsViewModel() : ViewModel() {

    private lateinit var authService: AuthService

    private lateinit var credential: CustomCredential

    fun exportStatisticsToGoogleDrive(context: Context, activity: Activity){
       //todo
    }


}
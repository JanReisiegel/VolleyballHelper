package com.reisiegel.volleyballhelper.services

import android.accounts.Account
import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.Sheets.Spreadsheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange

class GoogleDriveService(private val context: Context) {
    fun createGoogleSheet(account: Account) {
        val HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport()
        val JSON_FACTORY = JacksonFactory.getDefaultInstance()

        val credential = GoogleAccountCredential.usingOAuth2(context, listOf(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE_FILE))
        credential.selectedAccount = account

        val sheetsService = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
            .setApplicationName("Volleyball Helper")
            .build()

        val spreadsheet = Spreadsheet().setProperties(SpreadsheetProperties().setTitle("DataUživatelů"))

        val newSpreadsheet = sheetsService.spreadsheets().create(spreadsheet).execute()
        val range = "List1!A1:C2"
        val values = listOf(
            listOf("A1", "B1", "C1"),
            listOf("A2", "B2", "C2")
        )
        val body = ValueRange().setValues(values)
        sheetsService.spreadsheets().values().update(newSpreadsheet.spreadsheetId, range, body).setValueInputOption("RAW").execute()

        val driveService = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Volleyball Helper").build()

        val fileMetadata = File().setName("DataUživatelů.xlsx").setMimeType("application/vnd.google-apps.spreadsheet")
        val file = driveService.files().create(fileMetadata).execute()
    }
}
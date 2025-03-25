package com.reisiegel.volleyballhelper.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.Sheet
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.auth.FirebaseAuth
import com.reisiegel.volleyballhelper.models.Match
import com.reisiegel.volleyballhelper.models.Tournament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoogleDriveService(private val context: Context, private val activity: Activity) {
    private val TAG = "GoogleDriveService"
    private lateinit var sheetService: Sheets
    suspend fun createGoogleSheet(auth: FirebaseAuth, authorizationLauncher: ActivityResultLauncher<Intent>, tournament: Tournament?) {
        if (tournament == null){
            Log.e(TAG, "Tournament is null")
            Toast.makeText(context, "Tournament is null", Toast.LENGTH_SHORT).show()
            return
        }
        withContext(Dispatchers.IO) {
            try {

                val account = auth.currentUser?.email
                if (account == null) {
                    Log.e(TAG, "Account email is null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Account error", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_FILE, SheetsScopes.SPREADSHEETS)
                )
                    .setSelectedAccountName(account)

                sheetService =
                    Sheets.Builder(NetHttpTransport(), JacksonFactory(), credential)
                        .setApplicationName("VolleyballHelper")
                        .build()

                val spreadsheet =
                    Spreadsheet().setProperties(SpreadsheetProperties().setTitle(tournament.name))

                val createdSpreadsheet =
                    sheetService.spreadsheets().create(spreadsheet).execute()

                val spreadsheetID = createdSpreadsheet.spreadsheetId ?: null

                if (spreadsheetID == null) {
                    Log.e(TAG, "Spreadsheet ID is null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error creating sheet", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                tournament.getMatchesArrayList().forEachIndexed { index, match ->
                    createMatchSheet(spreadsheetID, match, index)
                }


                //TODO: Add data to the summary sheet

//                val values = listOf(
//                    listOf("Header1", "Header2", "Header3"),
//                    listOf("Data1", "Data2", "Data3"),
//                    listOf("MoreData1", "MoreData2", "MoreData3")
//                )
//                val body = ValueRange().setValues(values)
//                sheetService.spreadsheets().values()
//                    .update(createdSpreadsheet.spreadsheetId, "A1", body)
//                    .setValueInputOption("RAW")
//                    .execute()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Sheet created and saved!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: UserRecoverableAuthIOException){
                Log.e(TAG, "createGoogleSheet: UserRecoverableAuthIOException: ${e.message}", e)
                Log.d(TAG, "createGoogleSheet: Launching authorization activity")
                withContext(Dispatchers.Main){
                    authorizationLauncher.launch(e.intent)
                }
            }
            catch (e: GoogleJsonResponseException) {
                Log.e(TAG, "GoogleJsonResponseException: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                Log.e(TAG, "Exception: ${e.localizedMessage}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createMatchSheet(spreadsheetID: String, match: Match, index: Int){
        val addSheetRequest = Request()
            .setAddSheet(AddSheetRequest().setProperties(SheetProperties().setTitle(match.opponentName).setIndex(index)))
        val batchUpdateRequest = BatchUpdateSpreadsheetRequest()
            .setRequests(listOf(addSheetRequest))

        sheetService.spreadsheets().batchUpdate(spreadsheetID, batchUpdateRequest).execute()

        val matchData = mutableListOf<List<Any>>().apply{
            match.getTableData().forEach{
                item -> add(item)
            }

        }

        val valueRange = ValueRange().setValues(matchData)
        val updateRequest = sheetService.spreadsheets().values()
            .update(spreadsheetID, match.opponentName, valueRange)
            .setValueInputOption("RAW")
            .execute()
    }
}
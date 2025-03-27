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
import com.google.api.services.drive.DriveScopes
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.AutoResizeDimensionsRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse
import com.google.api.services.sheets.v4.model.Border
import com.google.api.services.sheets.v4.model.Borders
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.DimensionProperties
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.MergeCellsRequest
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.RowData
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.UpdateCellsRequest
import com.google.api.services.sheets.v4.model.UpdateDimensionPropertiesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.auth.FirebaseAuth
import com.reisiegel.volleyballhelper.models.Match
import com.reisiegel.volleyballhelper.models.Tournament
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                val requests = mutableListOf<Request>()
                tournament.getMatchesArrayList().forEachIndexed { index, match ->
                    createMatchSheet(spreadsheetID, match, index+1)
                    val sheetId = getSheetId(spreadsheetID, match.opponentName)
                    requests.add(addBottomBorder(spreadsheetID, sheetId, match.players.size + 1,match.players.size + 3,0,27)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 1, 2)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 1, 2)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 6, 7)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 13, 14)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 18, 19)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 6, 7)!!)
                    requests.add(addRightBorder(spreadsheetID, sheetId, 0, match.players.size + 3, 23, 27)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 1, 0, 2)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 1, 2, 7)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 1, 7, 14)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 1, 14, 19)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 1, 19, 24)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, match.players.size + 2, match.players.size + 3, 0, 2)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 2, 24, 25)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 2, 25, 26)!!)
                    requests.add(mergeCells(spreadsheetID, sheetId, 0, 2, 26, 27)!!)
                    requests.add(alignColumns(spreadsheetID, sheetId, 0, match.players.size + 3, 0, 27)!!)
                    requests.add(resizeColumns(spreadsheetID, sheetId)!!)

                }
                Log.d(TAG, "Number of requests: ${requests.size}")
                updateGoogleSheet(spreadsheetID, requests)



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

    private fun mergeCells(spreadsheetId: String, sheetId: Int, startRow: Int, endRow: Int, startCol: Int, endCol: Int, mergeType: String = "MERGE_ALL") : Request?{

        val mergeRequest =
            Request().setMergeCells(
                MergeCellsRequest()
                    .setRange(
                        GridRange()
                            .setSheetId(sheetId)
                            .setStartRowIndex(startRow)
                            .setEndRowIndex(endRow)
                            .setStartColumnIndex(startCol)
                            .setEndColumnIndex(endCol)
                    )
                    .setMergeType(mergeType)
            )


        return mergeRequest

//        val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(mergeRequests)
//        sheetService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
    }
    private fun addBottomBorder(
        spreadsheetId: String,
        sheetId: Int,
        startRow: Int,
        endRow: Int,
        startCol: Int,
        endCol: Int,
        borderColor: Color? = null,
        borderStyle: String = "SOLID_MEDIUM") : Request?{
        val borderColorToUse = borderColor ?: Color().apply{
            red = 0.0f
            green = 0.0f
            blue = 0.0f
        }

        val request =
            Request().setRepeatCell(
                RepeatCellRequest()
                    .setRange(
                        GridRange()
                            .setSheetId(sheetId)
                            .setStartRowIndex(startRow)
                            .setEndRowIndex(endRow)
                            .setStartColumnIndex(startCol)
                            .setEndColumnIndex(endCol)
                    )
                    .setCell(
                        CellData().setUserEnteredFormat(
                            CellFormat()
                                .setBorders(
                                Borders()
                                    .setBottom(Border().setStyle(borderStyle).setColor(borderColorToUse))
                            )
                        )
                    )
                    .setFields("userEnteredFormat.borders.bottom")
            )


        return request

//        val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(requests)
//
//        try{
//            sheetService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
//        } catch (e: Exception){
//            Log.e(TAG, "addBorder: ${e.message}")
//
//        }
    }

    private fun addRightBorder(
        spreadsheetId: String,
        sheetId: Int,
        startRow: Int,
        endRow: Int,
        startCol: Int,
        endCol: Int,
        borderColor: Color? = null,
        borderStyle: String = "SOLID_MEDIUM") :Request? {
        val borderColorToUse = borderColor ?: Color().apply {
            red = 0.0f
            green = 0.0f
            blue = 0.0f
        }

        val request =
            Request().setRepeatCell(
                RepeatCellRequest()
                    .setRange(
                        GridRange()
                            .setSheetId(sheetId)
                            .setStartRowIndex(startRow)
                            .setEndRowIndex(endRow)
                            .setStartColumnIndex(startCol)
                            .setEndColumnIndex(endCol)
                    )
                    .setCell(
                        CellData().setUserEnteredFormat(
                            CellFormat().setBorders(
                                Borders()
                                    .setRight(
                                        Border().setStyle(borderStyle).setColor(borderColorToUse)
                                    )
                            )
                        )
                    )
                    .setFields("userEnteredFormat.borders.right")
            )


        return request
//        val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(requests)
//
//        try {
//            sheetService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
//        } catch (e: Exception) {
//            Log.e(TAG, "addBorder: ${e.message}")
//
//        }
    }

    private fun alignColumns(
        spreadsheetId: String,
        sheetId: Int,
        startRow: Int,
        endRow: Int,
        startCol: Int,
        endCol: Int,
        horizontalAlignment: String = "CENTER",
        verticalAlignment: String = "MIDDLE",
    ) : Request?{

        val request =
            Request().setUpdateCells(
                UpdateCellsRequest()
                    .setRange(
                        GridRange()
                            .setSheetId(sheetId)
                            .setStartColumnIndex(startCol)
                            .setEndColumnIndex(endCol)
                            .setStartRowIndex(startRow)
                            .setEndRowIndex(endRow)
                    )
                    .setFields("userEnteredFormat.horizontalAlignment,userEnteredFormat.verticalAlignment")
                    .setRows(
                        listOf(
                            RowData().setValues(
                                (startCol until endCol).map {
                                    CellData().setUserEnteredFormat(
                                        CellFormat()
                                            .apply {
                                                setHorizontalAlignment(horizontalAlignment)
                                                setVerticalAlignment(verticalAlignment)
                                            }
                                    )
                                }
                            )
                        )
                    )
            )

        return request
    }

    private fun resizeColumns(
        spreadsheetId: String,
        sheetId: Int,
        endCol: Int = 27,
    ) : Request?{

        val autoResizeRequest = Request().setAutoResizeDimensions(
            AutoResizeDimensionsRequest()
                .setDimensions(
                    DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension("COLUMNS")
                        .setStartIndex(0)
                        .setEndIndex(endCol)
                )
        )

        return autoResizeRequest
    }

    private suspend fun updateGoogleSheet(spreadsheetId: String, requests: List<Request>){

            try {
                // Execute request
                val batchUpdateRequest = BatchUpdateSpreadsheetRequest().setRequests(requests)
                sheetService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
                //delay(200) // Small delay between requests
            } catch (e: Exception) {
                Log.e(TAG, "Request failed: ${e.message}")
                // Optionally requeue or handle failure
            }

//        try {
//            executeWithRetry {
//                sheetService.spreadsheets().batchUpdate(spreadsheetId, batchUpdateRequest).execute()
//            }
//        } catch (e: Exception) {
//            // Handle final failure
//            Log.e(TAG, "Failed after all retries: ${e.message}")
//        }
    }

    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000L,
        block: suspend () -> T,
    ): T {
        var currentDelay = initialDelay
        var retryCount = 0

        while (retryCount < maxRetries) {
            try {
                return block()
            } catch (e: Exception) {
                // Check if it's a rate limit error
                if (e.message?.contains("429") == true ||
                    e.message?.contains("rateLimitExceeded") == true) {

                    // Log the retry attempt
                    Log.w(TAG, "Rate limit hit. Retry attempt ${retryCount + 1}")

                    // Wait before retrying with exponential backoff
                    delay(currentDelay)

                    // Increase delay exponentially
                    currentDelay *= 2
                    retryCount++
                } else {
                    // For non-rate limit errors, rethrow immediately
                    throw e
                }
            }
        }

        // If all retries fail
        throw Exception("Max retries reached. Unable to complete request.")
    }

    fun getSheetId(spreadsheetId: String, sheetName: String): Int {
        val spreadsheet = sheetService.spreadsheets().get(spreadsheetId)
            .setIncludeGridData(false)
            .execute()
        val sheets = spreadsheet.sheets
        val resultId = sheets
            .find { it.properties.title == sheetName }
            ?.properties?.sheetId
            ?: throw IllegalArgumentException("Sheet $sheetName not found in spreadsheet")
        return resultId
    }

//    fun formatSheet(spreadsheetId: String, endCol: Int, endRow: Int){
//        val sheet = sheetService.spreadsheets().get(spreadsheetId).execute()
//
//        // If you want to format a specific range
//        var range =
//    }
}
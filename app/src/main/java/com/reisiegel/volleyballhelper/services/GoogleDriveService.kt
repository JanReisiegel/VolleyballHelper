package com.reisiegel.volleyballhelper.services

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
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
import com.google.api.services.sheets.v4.model.Border
import com.google.api.services.sheets.v4.model.Borders
import com.google.api.services.sheets.v4.model.CellData
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.DimensionRange
import com.google.api.services.sheets.v4.model.ExtendedValue
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.MergeCellsRequest
import com.google.api.services.sheets.v4.model.RepeatCellRequest
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.RowData
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.Spreadsheet
import com.google.api.services.sheets.v4.model.SpreadsheetProperties
import com.google.api.services.sheets.v4.model.UpdateCellsRequest
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import com.google.firebase.auth.FirebaseAuth
import com.reisiegel.volleyballhelper.R
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
            Toast.makeText(context, context.getString(R.string.tournament_null), Toast.LENGTH_SHORT).show()
            return
        }
        withContext(Dispatchers.IO) {
            try {

                val firebaseUser = auth.currentUser
                if (firebaseUser == null) {
                    Log.e(TAG, "Firebase user is null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.firebase_user_not_available), Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }


                val credential = GoogleAccountCredential.usingOAuth2(
                    context,
                    listOf(DriveScopes.DRIVE_FILE, SheetsScopes.SPREADSHEETS)
                )
                    //.setSelectedAccountName(account.email)

                //získání emailu z firebase usera
                val userEmail = firebaseUser.email
                if (userEmail == null) {
                    Log.e(TAG, "User email is null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.user_mail_not_available), Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
                try {
                    // nastavení účtu pomocí emailu do credential
                    credential.selectedAccount = Account(userEmail, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE)
                    GoogleAuthUtil.getToken(
                        context,
                        userEmail,
                        "oauth2:" + DriveScopes.DRIVE_FILE + " " + SheetsScopes.SPREADSHEETS
                    )

                } catch (e: UserRecoverableAuthException) {
                    // User needs to grant permission
                    Log.d(TAG, "Need user consent, launching intent")
                    withContext(Dispatchers.Main) {
                        authorizationLauncher.launch(e.intent)
                    }
                    return@withContext
                } catch (e: GoogleAuthException) {
                    Log.e(TAG, "GoogleAuthException: ${e.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "${context.getString(R.string.google_auth_error)} ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }



                val token = credential.token

                sheetService =
                    Sheets.Builder(NetHttpTransport(), JacksonFactory(), credential)
                        .setApplicationName(context.getString(R.string.app_name))
                        .build()

                val spreadsheet =
                    Spreadsheet().setProperties(SpreadsheetProperties().setTitle(tournament.name))

                val createdSpreadsheet =
                    sheetService.spreadsheets().create(spreadsheet).execute()

                val spreadsheetID = createdSpreadsheet.spreadsheetId ?: null

                if (spreadsheetID == null) {
                    Log.e(TAG, "Spreadsheet ID is null")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, context.getString(R.string.create_sheet_error), Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
                val requests = mutableListOf<Request>()
                tournament.getMatchesArrayList().forEachIndexed { index, match ->
                    createMatchSheet(spreadsheetID, match, index+1)
                    val sheetId = getSheetId(spreadsheetID, match.opponentName)
                    formateTable(spreadsheetID, sheetId, match.players.size).forEach {
                        requests.add(it)
                    }
                }
                val summarySheetId = createdSpreadsheet.sheets[0].properties.sheetId

                //TADY to nejspíš spadne??

                val values = tournament.getSummaryTable()
                // TADY to padá
                val body = ValueRange().setValues(values)
                sheetService.spreadsheets().values()
                    .update(createdSpreadsheet.spreadsheetId, "A1", body)
                    .setValueInputOption("RAW")
                    .execute()

                val renameRequest = Request().setUpdateSheetProperties(
                    UpdateSheetPropertiesRequest().setProperties(
                        SheetProperties()
                            .setSheetId(summarySheetId)
                            .setTitle(tournament.name)
                    ).setFields("title")
                )

                requests.add(renameRequest)


                val playersNumber = tournament.getNumberOfPlayers()
                formateTable(spreadsheetID, summarySheetId, playersNumber).forEach {
                    requests.add(it)
                }


                Log.d(TAG, "Number of requests: ${requests.size}")
                updateGoogleSheet(spreadsheetID, requests)

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, context.getString(R.string.sheet_created), Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "${context.getString(R.string.error_header)}: ${e.message}", Toast.LENGTH_SHORT).show()
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

        var mergeRequest =
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
    }

    private fun mergeCellsAndKeepContent(
        spreadsheetId: String,
        sheetId: Int,
        startRow: Int,
        endRow: Int,
        startCol: Int,
        endCol: Int,
        mergeType: String = "MERGE_ALL"
    ): List<Request> {
        val requests = mutableListOf<Request>()

        try {
            // 1. Get the sheet properties to check grid limits
            val spreadsheet = sheetService.spreadsheets().get(spreadsheetId)
                .setFields("sheets(properties(sheetId,title,gridProperties))")
                .execute()

            val sheet = spreadsheet.sheets.find { it.properties.sheetId == sheetId }
            if (sheet == null) {
                Log.e("MergeCells", "Sheet with ID $sheetId not found")
                requests.add(mergeCells(spreadsheetId,sheetId,startRow,endRow,startCol,endCol,"MERGE_ALL")!!)
                return requests

            }

            val maxColumns = sheet.properties.gridProperties.columnCount
            val maxRows = sheet.properties.gridProperties.rowCount

            // Check if the range exceeds grid limits
            if (startCol < 0 || endCol > maxColumns || startRow < 0 || endRow > maxRows) {
                Log.e(
                    "MergeCells",
                    "Range exceeds grid limits: maxRows=$maxRows, maxColumns=$maxColumns"
                )
                requests.add(mergeCells(spreadsheetId,sheetId,startRow,endRow,startCol,endCol,"MERGE_ALL")!!)
                return requests
            }

            // 2. Get values within the merge range
            val sheetName = sheet.properties.title
            val range =
                "$sheetName!${columnToLetter(startCol)}${startRow + 1}:${columnToLetter(endCol - 1)}${endRow}"

            // Handle potential errors when getting values
            val response = try {
                sheetService.spreadsheets().values().get(spreadsheetId, range).execute()
            } catch (e: Exception) {
                Log.e("MergeCells", "Failed to get values: ${e.message}")
                null
            }

            val secondCellValue = if (response?.getValues() != null &&
                response.getValues().isNotEmpty() &&
                response.getValues()[0].size > 1 &&
                response.getValues()[0][1] != null
            ) {
                response.getValues()[0][1].toString()
            } else {
                "" // Use empty string if second cell value can't be retrieved
            }

            // If there's content in the second cell
            if (secondCellValue.isNotEmpty()) {
                // 3. Update the first cell with the value from the second cell
                val updateRequest = Request().setUpdateCells(
                    UpdateCellsRequest()
                        .setRange(
                            GridRange()
                                .setSheetId(sheetId)
                                .setStartRowIndex(startRow)
                                .setEndRowIndex(startRow + 1)
                                .setStartColumnIndex(startCol)
                                .setEndColumnIndex(startCol + 1)
                        )
                        .setFields("userEnteredValue")
                        .setRows(
                            listOf(
                                RowData().setValues(
                                    listOf(
                                        CellData().setUserEnteredValue(
                                            ExtendedValue().setStringValue(secondCellValue)
                                        )
                                    )
                                )
                            )
                        )
                )
                requests.add(updateRequest)

                // 4. Clear the second cell to avoid duplicate text after merge
                val clearRequest = Request().setUpdateCells(
                    UpdateCellsRequest()
                        .setRange(
                            GridRange()
                                .setSheetId(sheetId)
                                .setStartRowIndex(startRow)
                                .setEndRowIndex(startRow + 1)
                                .setStartColumnIndex(startCol + 1)
                                .setEndColumnIndex(startCol + 2)
                        )
                        .setFields("userEnteredValue")
                        .setRows(
                            listOf(
                                RowData().setValues(
                                    listOf(
                                        CellData().setUserEnteredValue(
                                            ExtendedValue().setStringValue("")
                                        )
                                    )
                                )
                            )
                        )
                )
                requests.add(clearRequest)
            }

            // 5. Merge the cells
            val mergeRequest =
                mergeCells(spreadsheetId, sheetId, startRow, endRow, startCol, endCol, "MERGE_ALL")
            requests.add(mergeRequest!!)

        } catch (e: Exception) {
            Log.e("MergeCells", "Error in mergeCellsKeepSecondValue: ${e.message}")
            // Fallback to basic merge
            val fallbackMergeRequest =
                mergeCells(spreadsheetId, sheetId, startRow, endRow, startCol, endCol, "MERGE_ALL")
            requests.add(fallbackMergeRequest!!)
        }

        return requests
    }

    private fun columnToLetter(column: Int): String {
        var temp = column
        var result = ""

        do {
            val remainder = temp % 26
            result = (remainder + 'A'.code).toChar() + result
            temp = temp / 26 - (if (remainder == 0) 1 else 0)
        } while (temp >= 0)

        return result
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
            ?: throw IllegalArgumentException("${context.getString(R.string.sheet)} $sheetName ${context.getString(R.string.sheet_error_not_found)}")
        return resultId
    }
    fun formateTable(spreadsheetId: String, sheetId: Int, numberOfRows: Int): List<Request> {
        val requests = mutableListOf<Request>()
        requests.add(addBottomBorder(spreadsheetId, sheetId, numberOfRows + 1,numberOfRows + 3,0,27)!!)
        requests.add(addBottomBorder(spreadsheetId,sheetId, 1, 2, 0, 27)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 1, 2)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 1, 2)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 6, 7)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 13, 14)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 18, 19)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 6, 7)!!)
        requests.add(addRightBorder(spreadsheetId, sheetId, 0, numberOfRows + 3, 23, 27)!!)
        requests.add(resizeColumns(spreadsheetId, sheetId)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 1, 0, 2)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 1, 2, 7)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 1, 7, 14)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 1, 14, 19)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 1, 19, 24)!!)
        //requests.add(mergeCells(spreadsheetId, sheetId, numberOfRows + 2, numberOfRows + 3, 0, 2)!!)
        mergeCellsAndKeepContent(spreadsheetId, sheetId, numberOfRows + 2, numberOfRows + 3, 0, 2).forEach {
            requests.add(it)
        }
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 2, 24, 25)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 2, 25, 26)!!)
        requests.add(mergeCells(spreadsheetId, sheetId, 0, 2, 26, 27)!!)
        requests.add(alignColumns(spreadsheetId, sheetId, 0, numberOfRows + 3, 0, 27)!!)


        return requests
    }

    private fun getColumnLetter(columnIndex: Int): String {
        var temp = columnIndex
        var result = ""
        while (temp >= 0) {
            result = ('A' + temp % 26) + result
            temp = temp / 26 - 1
        }
        return result
    }
}
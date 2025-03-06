package com.reisiegel.volleyballhelper.services

//class GoogleDriveService(private val context: Context, private val credential: GoogleAccountCredential) {
//    private val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
//    private val driveService: Drive by lazy {
//        Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
//            .setApplicationName("Volleyball Helper")
//            .build()
//    }
//    private val sheetsService: Sheets by lazy {
//        Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
//            .setApplicationName("Volleyball Helper")
//            .build()
//    }
//
//    fun uploadToGoogleDrive(scope: CoroutineScope, fileName: String, content: String, mimeType: String, callback: (String?) -> Unit){
//        scope.launch(Dispatchers.IO){
//            try {
//                val fileMetadata = File().apply {
//                    name = fileName
//                    setMimeType(mimeType)
//                }
//                val byteArrayOutputStream = ByteArrayOutputStream()
//                byteArrayOutputStream.write(content.toByteArray(StandardCharsets.UTF_8))
//                val inputStream = byteArrayOutputStream.toByteArray().inputStream()
//                val mediaContent =
//                    com.google.api.client.http.InputStreamContent(mimeType, inputStream)
//                val file = driveService.files().create(fileMetadata, mediaContent).setFields("id")
//                    .execute()
//                callback(file.id)
//            } catch (e: Exception){
//                e.printStackTrace()
//                callback(null)
//            }
//        }
//    }
//}
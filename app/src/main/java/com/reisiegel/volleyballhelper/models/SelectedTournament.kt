package com.reisiegel.volleyballhelper.models

import java.io.File
import kotlin.properties.Delegates

class SelectedTournament {
    companion object{
        var filePath: String = ""
        var selectedTournament: Tournament? = null
        var selectedMatchIndex: Int? = null
        fun loadTournament(file: File){
            filePath = file.path
            selectedTournament = Tournament.loadFromJson(file)
        }

    }
}
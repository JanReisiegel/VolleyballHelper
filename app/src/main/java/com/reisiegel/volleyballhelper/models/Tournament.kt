package com.reisiegel.volleyballhelper.models

import android.annotation.SuppressLint
import android.content.Context
import com.google.gson.Gson
import java.io.File


class Tournament private constructor(var name: String, private var matches: ArrayList<Match> = ArrayList<Match>(), private var players: HashMap<Int, String> = HashMap<Int, String>(), var startDate: String, var endDate: String) {

    init {
        for(item: Match in matches)
            players.forEach { (number, name) ->
                item.addPlayer(Player(name, number))
            }
    }

    companion object{
        /**
         * Create Tournament without any predefined properties
         *
         */
        fun createTournament(): Tournament{
            return Tournament("", ArrayList<Match>(), HashMap<Int, String>(), "", "")
        }

        /**
         * Create Tournament from existing JSON file
         *
         * @param context is the context of the application
         * @param fileName is the name of the file to be loaded
         *
         * @return new instance of [Tournament]
         */
        fun createTournament(context: Context, fileName: String): Tournament{
            val file = File(context.filesDir, fileName)
            val jsonString = file.readText()
            val gson = Gson()
            return gson.fromJson(jsonString, Tournament::class.java)
        }

        /**
         * Create Tournament with known properties
         *
         * @param name is name of Tournament
         * @param matches is list of matches of this Tournament, can be unknown
         * @param players is the roster participating in the Tournament, can be unknown
         * @param startDate is the start date of the Tournament
         * @param endDate is the end date of the Tournament
         *
         * @return new instance of [Tournament]
         */
        fun createTournament(name: String, matches: ArrayList<Match> = ArrayList<Match>(), players: HashMap<Int, String> = HashMap<Int, String>(), startDate: String, endDate: String): Tournament {
            return Tournament(name, matches, players, startDate, endDate)
        }

        fun loadFromJson(file: File): Tournament?{
            return try {
                val gson = Gson()
                val fileJsonString = file.readText()
                gson.fromJson(fileJsonString, Tournament::class.java)
            }
            catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun getPlayers(): HashMap<Int, String>{
        return players
    }
    fun updatePlayer(oldNumber: Int, number: Int, name: String){
        players[number] = name
        for (match: Match in matches){
            match.updatePlayer(oldNumber, number, name)
        }
    }

    fun getNumberOfPlayers(): Int{
        return players.size
    }
    /**
     * Function to add player in nomination and add the player to all match statistics
     *
     * @param name is for player name
     * @param jersey is for jersey number of this player
     */
    fun addPlayer(name: String, jersey: Int){
        val player = Player(name, jersey)
        players[jersey] = name
        for(item: Match in matches)
            item.addPlayer(player)
    }

    /**
     * Function for adding match to tournament array
     *
     * @param opponent is for opponent name like "Trentino Volley"
     */
    fun addMatch(opponent: String, startTime: String){
        val playersList = ArrayList<Player>()
        for(number: Int in players.keys){
            var player = Player(players[number]!!, number)
        }
        val match = Match(opponent, playersList, startTime)
        matches.add(match)
    }

    /**
     * This function is for get the specific match for match editor
     *
     * @param matchIndex is index of selected match
     */
    fun getMatch(matchIndex: Int): Match{
        //Log.d("Tournament", "Počet Zápasu (Tournament class): ${matches.size}")
        lateinit var result: Match
        try {
            result = matches[matchIndex]
        } catch (e: Exception){
            //Log.e("Tournament", "Chyba při načítání zápasu")
            e.printStackTrace()
        }
        //Log.d("Tournament", "Počet hráčů (Tournament class): ${result.players.size}")
        return result
    }

    /**
     * This function is for get all matches in this tournament
     */
    fun getMatchesArrayList() : ArrayList<Match>{
        return matches
    }

    /**
     * To save unshared match into local device
     */
    fun saveJson(file: File){
        val gson = Gson()
        val jsonString = gson.toJson(this)
        file.writeText(jsonString)
    }

    @SuppressLint("DefaultLocale")
    fun getSummaryTable(): List<List<String>>{
        val summaryTable = mutableListOf<List<String>>()
        val indexes = listOf(2, 4, 7, 9, 11, 16, 21)
        val header = listOf(
            listOf("Hráč", "",
                "Podání", "", "", "", "",
                "Útok", "", "", "", "", "", "",
                "Blok", "", "", "", "",
                "Příjem", "", "", "", "",
                "Chyby celkem", "Body celkem", "+-"),
            listOf("Číslo","Jméno",
                "Pokusy", "Zkažené", "%", "Esa", "% bodů",
                "Pokusy", "Zkažené", "%", "Bodové", "%", "Zablokované", "%",
                "Pokusy", "Úspěšné", "Neúspěšné", "Chyby", "%",
                "Pokusy", "Chyby", "Ideální", "Příjmuté", "%",
                "", "", "" )
        )
        header.forEach { item -> summaryTable.add(item) }
        val playerNumbers = players.keys.sorted()
        var playersSummaryStats = MutableList<Double>(25) {0.0}
        playerNumbers.forEach { number ->
            var stats: MutableList<Double> = MutableList<Double>(25) { 0.0 }
            matches.forEach { match ->
                val player = match.getPlayer(number)
                stats = player!!.updateSummary(stats)
            }
            playersSummaryStats.forEachIndexed { index, stat ->
                playersSummaryStats[index] += stats[index]
            }

            val playerName = players[number]
            val playerLine = mutableListOf<String>(number.toString(), playerName.toString())
            stats.forEachIndexed { index, item ->
                if (index in indexes){
                    playerLine.add(String.format("%.2f", item))
                } else{
                    playerLine.add(item.toInt().toString())
                }
            }
            summaryTable.add(playerLine.toList())
        }
        val footer = mutableListOf<String>("", "Celkem/Průměr")

        playersSummaryStats.forEachIndexed { index, item ->
            when (index) {
                2 -> footer.add(String.format("%.0f", if(playersSummaryStats[0] == 0.0) 0.0 else (playersSummaryStats[1] / playersSummaryStats[0]) * 100))
                4 -> footer.add(String.format("%.0f", if(playersSummaryStats[0] == 0.0) 0.0 else (playersSummaryStats[3] / playersSummaryStats[0]) * 100))
                7 -> footer.add(String.format("%.0f", if(playersSummaryStats[5] == 0.0) 0.0 else (playersSummaryStats[6] / playersSummaryStats[5]) * 100))
                9 -> footer.add(String.format("%.0f", if(playersSummaryStats[5] == 0.0) 0.0 else (playersSummaryStats[8] / playersSummaryStats[5]) * 100))
                11 -> footer.add(String.format("%.0f", if(playersSummaryStats[5] == 0.0) 0.0 else (playersSummaryStats[10] / playersSummaryStats[5]) * 100))
                16 -> footer.add(String.format("%.0f", if(playersSummaryStats[12] == 0.0) 0.0 else (playersSummaryStats[13] / playersSummaryStats[12]) * 100))
                21 -> footer.add(String.format("%.0f", if(playersSummaryStats[17] == 0.0) 0.0 else ((playersSummaryStats[19] + playersSummaryStats[20]) / playersSummaryStats[17]) * 100))
                else -> footer.add(item.toInt().toString())
            }
        }
        summaryTable.add(footer.toList())
        return summaryTable
    }
}
package com.reisiegel.volleyballhelper.models

import android.content.Context
import android.util.Log
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
    fun getmatchesArrayList() : ArrayList<Match>{
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

    fun shareGoogleSheetToGoogleDrive(){
        TODO()
    }
}
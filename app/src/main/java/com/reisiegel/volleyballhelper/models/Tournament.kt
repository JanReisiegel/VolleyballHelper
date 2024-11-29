package com.reisiegel.volleyballhelper.models

import android.content.Context
import com.google.gson.Gson
import com.reisiegel.volleyballhelper.models.Player
import java.io.File
import java.util.Date


class Tournament private constructor(var name: String, private var matches: ArrayList<Match> = ArrayList<Match>(), private var players: HashMap<Int, String> = HashMap<Int, String>(), var startDate: String, var endDate: String) {

    companion object{
        /**
         * Create Tournament from existing Google Sheet
         *
         */
        fun createTournament(): Tournament{
            TODO()
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
        return matches[matchIndex]
    }

    /**
     * To save unshared match into local device
     */
    fun saveJson(context: Context){
        val gson = Gson()
        val jsonString = gson.toJson(this)

        val file = File(context.filesDir, "${name.replace(" ", "_")}.json")
        file.writeText(jsonString)
    }

    fun shareGoogleSheetToGoogleDrive(){
        TODO()
    }
}
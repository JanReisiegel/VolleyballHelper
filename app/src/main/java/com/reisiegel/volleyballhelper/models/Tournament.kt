package com.reisiegel.volleyballhelper.models

class Tournament(var name: String, var matches: ArrayList<Match> = ArrayList<Match>(), var players: HashMap<Int, String> = HashMap<Int, String>()) {

    /**
     * Function to add player in nomination and add the player to all match statistics
     *
     * @param name is for player name
     * @param jersey is for jersey number of this player
     */
    fun addPlayer(name: String, jersey: Int){
        var player = Player(name, jersey)
        players[jersey] = name
        for(item: Match in matches)
            item.addPlayer(player)
    }

    /**
     * Function for adding match to tournament array
     *
     * @param opponent is for opponent name like "Trentino Volley"
     */
    fun addMatch(opponent: String){
        var playersList = ArrayList<Player>()
        for(number: Int in players.keys){
            var player = Player(players.get(number)!!, number)
        }
        var match = Match(opponent, playersList)
        matches.add(match)
    }

    /**
     * This function is for get the specific match for match editor
     *
     * @param matchIndex is index of selected match
     */
    fun getMatch(matchIndex: Int): Match{
        return matches.get(matchIndex)
    }

    fun saveToGoogleSheet(){
        TODO()
    }
}
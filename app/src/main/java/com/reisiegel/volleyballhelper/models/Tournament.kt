package com.reisiegel.volleyballhelper.models

class Tournament(var name: String, var matches: ArrayList<Match> = ArrayList<Match>(), var players: HashMap<Int, String> = HashMap<Int, String>()) {


    fun addPlayer(name: String, jersey: Int){
        var player = Player(name, jersey)
        players[jersey] = name
        for(item: Match in matches)
            item.addPlayer(player)
    }

    fun addMatch(opponent: String){
        var playersList = ArrayList<Player>()
        for(number: Int in players.keys){
            var player = Player(players.get(number)!!, number)
        }
        var match = Match(opponent, playersList)
        matches.add(match)
    }
}
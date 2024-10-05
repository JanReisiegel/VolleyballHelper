package com.reisiegel.volleyballhelper.models

class Match(var opponentName: String, var players: ArrayList<Player>) {
    private var substitutions: ArrayList<Substitution>
    private var startSquads: ArrayList<ArrayList<Int>>
    private var score: MatchScore

    init {
        score = MatchScore(0,0)
        substitutions = ArrayList<Substitution>()
        startSquads = ArrayList<ArrayList<Int>>()
    }

    fun addPlayer(player: Player){
        players.add(player)
    }

    fun opponentError(){
        score.teamPoint()
    }

    fun playerAttack(jerseyNumber: Int, attackType: AttackEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addAttackStat(attackType)
                break
            }
        }
    }
    fun playerServe(jerseyNumber: Int, serveType: ServeEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addServeStat(serveType)
                break
            }
        }
    }
    fun playerBlock(jerseyNumber: Int, blockType: BlockEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addBlockStat(blockType)
                break
            }
        }
    }
    fun playerReceivedServe(jerseyNumber: Int, receiveServeType: ReceiveServeEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addReceiveStat(receiveServeType)
                break
            }
        }
    }
}
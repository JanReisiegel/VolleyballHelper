package com.reisiegel.volleyballhelper.models

import android.util.Log

class Match(var opponentName: String, var players: ArrayList<Player>, var startTime: String) {
    private var substitutions: ArrayList<Substitution>
    private var squads: ArrayList<ArrayList<Int>> = ArrayList<ArrayList<Int>>() // list of players in each squad
    private var rotations: Int = 0
    private var score: MatchScore
    private var opponentsError: Int = 0
    private var isFinished: Boolean = false
    private var timeouts = ArrayList<Int>()
    var haveService: Boolean = true
    var serveStart: Boolean = true

    init {
        score = MatchScore(0,0)
        substitutions = ArrayList<Substitution>()
        squads = ArrayList<ArrayList<Int>>()
    }

    fun getActiveSquad(): ArrayList<Int> {
        //Log.d("Match", "getActiveSquad: ${squads.size}")
        //val matchSquads = squads ?: listOf<ArrayList<Int>>()
        if(squads.size == 0) {
            squads.add(ArrayList<Int>())
        }
        return squads[squads.size - 1]
    }

    fun setSquad(players: List<Player?>){
        if (squads.size == 0){
            squads.add(ArrayList<Int>())
        }
        for (player in players){
            if (player != null){
                squads[squads.size - 1].add(player.jerseyNumber)
            }
        }
    }

    fun changeSquad(players: List<Player?>){
        if (squads.size == 0){
            setSquad(players)
        }
        squads[squads.size - 1].clear()
        players.forEachIndexed { index, player ->
            if (player != null) {
                squads[squads.size - 1].add(player.jerseyNumber)
            }
        }
    }

    /**
     * Function for add a substitution to match
     */
    fun finishMatch(){
        isFinished = true
    }

    /**
     * function for add a player to match
     *
     * @param player instance of Player class. This will be added to match
     */
    fun addPlayer(player: Player){
        players.add(player)
    }

    fun changeStartServe(value: Boolean){
        val setNumber = score.getNumberOfSet()
        if (setNumber != 3 && setNumber != 5){
            serveStart = value
        }
    }

    /**
     *
     */
    fun isFinished(): Boolean {
        return isFinished
    }

    fun getPlayer(jerseyNumber: Int): Player? {
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                return player
            }
        }
        return null
    }

    /**
     * function for update match start time and opponent name
     */
    fun updateMatchInfo(newOpponent: String, newDateTime: String){
        if (!newOpponent.contentEquals(opponentName))
            opponentName = newOpponent
        if (!newDateTime.contentEquals(startTime))
            startTime = newDateTime
    }

    /**
     * Set opponent error in Match to statistics
     */
    fun opponentError(){
        opponentsError++
        val newSet = score.teamPoint()
        if (newSet){
            newSet()
        }
    }

    fun opponentPoint(){
        val newSet = score.opponentPoint()
        if (newSet){
            newSet()
        }
    }

    /**
     * Function for set statistic of player attack
     *
     * @param jerseyNumber jersey number of player, who attacked in match
     * @param attackType type of an attack with predefined enum [AttackEnum]
     */
    fun playerAttack(jerseyNumber: Int, attackType: AttackEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addAttackStat(attackType)
                break
            }
        }
        var newSet: Boolean = false
        if (attackType == AttackEnum.HIT){
            newSet = score.teamPoint()
        } else if (attackType == AttackEnum.ERROR || attackType == AttackEnum.BLOCK){
            newSet = score.opponentPoint()
        }
        if (newSet){
            newSet()
        }
    }
    /**
     * Function for set statistic of player serve
     *
     * @param jerseyNumber jersey number of player, who served
     * @param serveType type of an serve with predefined enum [ServeEnum]
     */
    fun playerServe(jerseyNumber: Int, serveType: ServeEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addServeStat(serveType)
                break
            }
        }
        var newSet: Boolean = false
        if (serveType == ServeEnum.ACE){
            newSet = score.teamPoint()
        }else if (serveType == ServeEnum.ERROR){
            newSet = score.opponentPoint()
        }
        if (newSet){
            newSet()
        }
    }
    /**
     * Function for set statistic of player block
     *
     * @param jerseyNumber jersey number of player, who blocked opponent
     * @param blockType type of an block with predefined enum [BlockEnum]
     */
    fun playerBlock(jerseyNumber: Int, blockType: BlockEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addBlockStat(blockType)
                break
            }
        }
        var newSet: Boolean = false
        if (blockType == BlockEnum.POINT){
            newSet = score.teamPoint()
        }else if (blockType == BlockEnum.ERROR){
            newSet = score.opponentPoint()
        }
        if (newSet){
            newSet()
        }
    }
    /**
     * Function for set statistic of player receiving opponents serve
     *
     * @param jerseyNumber jersey number of player, who received the opponents serve
     * @param receiveServeType type of a serve received with predefined enum [ReceiveServeEnum]
     */
    fun playerReceivedServe(jerseyNumber: Int, receiveServeType: ReceiveServeEnum){
        for(player: Player in players) {
            if (player.jerseyNumber == jerseyNumber) {
                player.addReceiveStat(receiveServeType)
                break
            }
        }
        var newSet: Boolean = false
        if (receiveServeType == ReceiveServeEnum.ERROR || receiveServeType == ReceiveServeEnum.CANT_CONTINUE){
            newSet = score.opponentPoint()
        }
        if (newSet){
            newSet()
        }
    }

    fun getActualScore(): String {
        val teamScore = score.getTeamSetScore(score.getNumberOfSet())
        val opponentScore = score.getOpponentScore(score.getNumberOfSet())
        return "$teamScore:$opponentScore"
    }

    fun getActualSetScore(): String {
        val teamSetScore = score.getTeamSetScore()
        val opponentSetScore = score.getOpponentSetScore()
        return "$teamSetScore:$opponentSetScore"
    }

    /**
     * Function for remembering rotations. This will be use while the application drop down
     */
    fun rotate(){
        var temp: Int = rotations
        rotations = (temp+1)%6
    }

    fun addSubstitution(playerInNumber: Int, playerOutNumber: Int){
        val playerIn: Player = getPlayer(playerInNumber) ?: return
        val playerOut: Player = getPlayer(playerOutNumber) ?: return
        val substitution = Substitution(playerOut,playerIn,score.getNumberOfSet(),score.getTeamSetScore(score.getNumberOfSet()),score.getOpponentScore(score.getNumberOfSet()))
        substitutions.add(substitution)
    }

    fun getSetStartService(): Boolean {
        if (score.getNumberOfSet() % 2 != 0) {
            return !serveStart
        }
        return serveStart
    }

    private fun newSet(){
        squads.add(ArrayList<Int>())
    }

    fun getTableData(): List<List<String>> {
        val tableData = ArrayList<List<String>>()
        val header = listOf(
            listOf("Hráč", "",
                "Podání", "", "", "", "",
                "Útok", "", "", "", "", "", "",
                "Blok", "", "", "", "",
                "Příjem", "", "", "", "",
                "Chyby celkem", "Body celkem", "+-"),
            listOf("Jméno", "Číslo",
                "Pokusy", "Zkažené", "%", "Esa", "% bodů",
                "Pokusy", "Zkažené", "%", "Bodové", "%", "Zablokované", "%",
                "Pokusy", "Úspěšné", "Neúspěšné", "Chyby", "%",
                "Pokusy", "Chyby", "Ideální", "Příjmuté", "%",
                "", "", "" )
        )
        header.forEach { item -> tableData.add(item) }
        var summary = Array<Int>(25) {0}
        for (player in players) {
            val playerData = player.getPlayerData()
            tableData.add(playerData)
            summary = player.updateSummary(summary)
        }
        val footer = mutableListOf<String>("Celkem/Průměr", "")
        summary.forEach { item -> footer.add(item.toString()) }

        tableData.add(footer.toList())

        return tableData.toList()
    }

}
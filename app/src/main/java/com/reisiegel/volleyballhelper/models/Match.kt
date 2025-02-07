package com.reisiegel.volleyballhelper.models

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
        if(squads == null){
            return ArrayList<Int>()
        }
        return squads[squads.size - 1]
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
        score.teamPoint()
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
    }

    fun getActualScore(): String {
        val teamScore = score.getTeamSetScore()
        val opponentScore = score.getOpponentScore()
        return "$teamScore:$opponentScore"
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
        val substitution = Substitution(playerOut,playerIn,score.getNumberOfSet(),score.getTeamSetScore(),score.getOpponentScore())
        substitutions.add(substitution)
    }
}
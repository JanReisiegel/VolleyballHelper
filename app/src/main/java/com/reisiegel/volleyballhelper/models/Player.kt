package com.reisiegel.volleyballhelper.models


class Player(var name: String, var jerseyNumber: Int) {
    private var errors: Int = 0
    private var serveStats: ServiceStats = ServiceStats()
    private var attackStats: AttackStats = AttackStats()
    private var blockStats: BlockStats = BlockStats()
    private var receiveStats: ReceiveServeStats = ReceiveServeStats()

    fun addError(){
        errors++
    }

    fun addServeStat(type: ServeEnum){
        serveStats.serve(type)
    }
    fun addBlockStat(type: BlockEnum){
        blockStats.block(type)
    }
    fun addReceiveStat(type: ReceiveServeEnum){
        receiveStats.receiveServe(type)
    }
    fun addAttackStat(type: AttackEnum){
        attackStats.attack(type)
    }

    fun getServeStats(serveType: ServeEnum): Int {
        return serveStats.getStatistics(serveType)
    }
    fun getBlockStats(blockType: BlockEnum): Int {
        return blockStats.getStatistics(blockType)
    }
    fun getReceiveStats(receiveType: ReceiveServeEnum): Int {
        return receiveStats.getStatistics(receiveType)
    }
    fun getAttackStats(attackType: AttackEnum): Int {
        return attackStats.getStatistics(attackType)
    }
}
package com.reisiegel.volleyballhelper.models


class Player(var name: String, var jerseyNumber: Int) {
    private var serveStats: ServiceStats = ServiceStats()
    private var attackStats: AttackStats = AttackStats()
    private var blockStats: BlockStats = BlockStats()
    private var receiveStats: ReceiveServeStats = ReceiveServeStats()

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
    fun getPlayerData(): List<String> {
        val serveAttempts = serveStats.getAttempts()
        val serveErrors = serveStats.getStatistics(ServeEnum.ERROR)
        val serveAces = serveStats.getStatistics(ServeEnum.ACE)
        val serveErrorPercentage = if (serveAttempts == 0) "0" else (serveErrors.toDouble() / serveAttempts.toDouble() * 100).toInt().toString()
        val servePointPercentage = if (serveAttempts == 0) "0" else (serveAces.toDouble() / serveAttempts.toDouble() * 100).toInt().toString()

        val attackAttempts = attackStats.getAttempt()
        val attackErrors = attackStats.getStatistics(AttackEnum.ERROR)
        val attackHits = attackStats.getStatistics(AttackEnum.HIT)
        val attackBlocks = attackStats.getStatistics(AttackEnum.BLOCK)
        val attackErrorPercentage = if (attackAttempts == 0) "0" else (attackErrors.toDouble() / attackAttempts.toDouble() * 100).toInt().toString()
        val attackPointPercentage = if (attackAttempts == 0) "0" else (attackHits.toDouble() / attackAttempts.toDouble() * 100).toInt().toString()
        val attackBlockedPercentage = if (attackAttempts == 0) "0" else (attackBlocks.toDouble() / attackAttempts.toDouble() * 100).toInt().toString()

        val blockAttempts = blockStats.getAttempts()
        val blockErrors = blockStats.getStatistics(BlockEnum.ERROR)
        val blockPoints = blockStats.getStatistics(BlockEnum.POINT)
        val blockNoPoints = blockStats.getStatistics(BlockEnum.NO_POINT)
        val blockPointPercentage = if (blockAttempts == 0) "0" else (blockPoints.toDouble() / blockAttempts.toDouble() * 100).toInt().toString()

        val receiveAttempts = receiveStats.getAttempts()
        val receiveErrors = receiveStats.getStatistics(ReceiveServeEnum.ERROR)
        val receiveCanContinue = receiveStats.getStatistics(ReceiveServeEnum.CAN_CONTINUE)
        val receiveIdeal = receiveStats.getStatistics(ReceiveServeEnum.IDEAL)
        val receivePercentage = if (receiveAttempts == 0) "0" else ((receiveIdeal.toDouble() + receiveCanContinue.toDouble()) / receiveAttempts.toDouble() * 100).toInt().toString()

        val totalErrors = serveErrors + attackErrors + blockErrors + receiveErrors
        val totalPoints = serveAces + attackHits + blockPoints
        val totalDifference = totalPoints - totalErrors

        return listOf<String>(jerseyNumber.toString(), name,
            serveAttempts.toString(), serveErrors.toString(), serveErrorPercentage, serveAces.toString(), servePointPercentage,
            attackAttempts.toString(), attackErrors.toString(), attackErrorPercentage, attackHits.toString(), attackPointPercentage, attackBlocks.toString(), attackBlockedPercentage,
            blockAttempts.toString(), blockPoints.toString(), blockNoPoints.toString(), blockErrors.toString(), blockPointPercentage,
            receiveAttempts.toString(), receiveErrors.toString(), receiveIdeal.toString(), receiveCanContinue.toString(), receivePercentage,
            totalErrors.toString(), totalPoints.toString(), totalDifference.toString())
    }

    fun updateSummary(summary: MutableList<Double>): MutableList<Double>{
        val serveAttempts = serveStats.getAttempts()
        val serveErrors = serveStats.getStatistics(ServeEnum.ERROR)
        val serveAces = serveStats.getStatistics(ServeEnum.ACE)
        val serveErrorPercentage = if (serveAttempts == 0) 0.0 else (serveErrors.toDouble() / serveAttempts.toDouble() * 100)
        val servePointPercentage = if (serveAttempts == 0) 0.0 else (serveAces.toDouble() / serveAttempts.toDouble() * 100)

        val attackAttempts = attackStats.getAttempt()
        val attackErrors = attackStats.getStatistics(AttackEnum.ERROR)
        val attackHits = attackStats.getStatistics(AttackEnum.HIT)
        val attackBlocks = attackStats.getStatistics(AttackEnum.BLOCK)
        val attackErrorPercentage = if (attackAttempts == 0) 0.0 else (attackErrors.toDouble() / attackAttempts.toDouble() * 100)
        val attackPointPercentage = if (attackAttempts == 0) 0.0 else (attackHits.toDouble() / attackAttempts.toDouble() * 100)
        val attackBlockedPercentage = if (attackAttempts == 0) 0.0 else (attackBlocks.toDouble() / attackAttempts.toDouble() * 100)

        val blockAttempts = blockStats.getAttempts()
        val blockErrors = blockStats.getStatistics(BlockEnum.ERROR)
        val blockPoints = blockStats.getStatistics(BlockEnum.POINT)
        val blockNoPoints = blockStats.getStatistics(BlockEnum.NO_POINT)
        val blockPointPercentage = if (blockAttempts == 0) 0.0 else (blockPoints.toDouble() / blockAttempts.toDouble() * 100)

        val receiveAttempts = receiveStats.getAttempts()
        val receiveErrors = receiveStats.getStatistics(ReceiveServeEnum.ERROR)
        val receiveCanContinue = receiveStats.getStatistics(ReceiveServeEnum.CAN_CONTINUE)
        val receiveIdeal = receiveStats.getStatistics(ReceiveServeEnum.IDEAL)
        val receivePercentage = if (receiveAttempts == 0) 0.0 else ((receiveIdeal.toDouble() + receiveCanContinue.toDouble()) / receiveAttempts.toDouble() * 100)

        val totalErrors = serveErrors + attackErrors + blockErrors + receiveErrors
        val totalPoints = serveAces + attackHits + blockPoints
        val totalDifference = totalPoints - totalErrors
        var temp = 0.0
        summary[0] += serveAttempts
        summary[1] += serveErrors
        //temp = summary[2] + serveErrorPercentage
        //summary[2] = if (serveErrorPercentage == 0.0 || serveErrorPercentage == temp) temp else temp/2
        summary[2] = if(summary[0] == 0.0) 0.0 else (summary[1] / summary[0]) * 100
        summary[3] += serveAces
//        temp = summary[4] + servePointPercentage
//        summary[4] = if (servePointPercentage == 0.0 || servePointPercentage == temp) temp else temp/2
        summary[4] = if (summary[0] == 0.0) 0.0 else (summary[3] / summary[0]) * 100
        summary[5] += attackAttempts
        summary[6] += attackErrors
//        temp = summary[7] + attackErrorPercentage
//        summary[7] = if (attackErrorPercentage == 0.0 || attackErrorPercentage == temp) temp else temp/2
        summary[7] = if (summary[5] == 0.0) 0.0 else (summary[6] / summary[5]) * 100
        summary[8] += attackHits
//        temp = summary[9] + attackPointPercentage
//        summary[9] = if (attackPointPercentage == 0.0 || attackPointPercentage == temp) temp else temp/2
        summary[9] = if (summary[5] == 0.0) 0.0 else (summary[8] / summary[5]) * 100
        summary[10] += attackBlocks
//        temp = summary[11] + attackBlockedPercentage
//        summary[11] = if (attackBlockedPercentage == 0.0 || attackBlockedPercentage == temp) temp else temp/2
        summary[11] = if (summary[5] == 0.0) 0.0 else (summary[10] / summary[5]) * 100
        summary[12] += blockAttempts
        summary[13] += blockPoints
        summary[14] += blockNoPoints
        summary[15] += blockErrors
//        temp = summary[16] + blockPointPercentage
//        summary[16] = if (blockPointPercentage == 0.0 || blockPointPercentage == temp) temp else temp/2
        summary[16] = if (summary[12] == 0.0) 0.0 else (summary[13] / summary[12]) * 100
        summary[17] += receiveAttempts
        summary[18] += receiveErrors
        summary[19] += receiveIdeal
        summary[20] += receiveCanContinue
//        temp = summary[21] + receivePercentage
//        summary[21] = if (receivePercentage == 0.0 || receivePercentage == temp) temp else temp/2
        summary[21] = if (summary[17] == 0.0) 0.0 else ((summary[19] + summary[20]) / summary[17]) * 100
        summary[22] += totalErrors
        summary[23] += totalPoints
        summary[24] += totalDifference
        return summary
    }
}
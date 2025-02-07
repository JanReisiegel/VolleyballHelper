package com.reisiegel.volleyballhelper.models

class AttackStats {
    private var received: Int = 0
    private var errors: Int = 0
    private var hits: Int = 0
    private var blocks: Int = 0

    fun attack(type: AttackEnum){
        when(type){
            AttackEnum.HIT -> hits++
            AttackEnum.ERROR -> errors++
            AttackEnum.RECEIVED -> received++
            AttackEnum.BLOCK -> blocks++
        }
    }
    fun getAttempt(): Int{
        return received + errors + hits + blocks
    }

    fun getStatistics(attackType: AttackEnum): Int{
        return when(attackType){
            AttackEnum.HIT -> hits
            AttackEnum.ERROR -> errors
            AttackEnum.RECEIVED -> received
            AttackEnum.BLOCK -> blocks
        }
    }
}
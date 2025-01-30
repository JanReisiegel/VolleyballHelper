package com.reisiegel.volleyballhelper.models

class AttackStats {
    private var received: Int = 0
        get() = field
    private var errors: Int = 0
        get() = field
    private var hits: Int = 0
        get() = field
    private var blocks: Int = 0
        get() = field

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
}
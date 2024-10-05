package com.reisiegel.volleyballhelper.models

class BlockStats {
    private var noPoint: Int = 0
        get() = field
    private var errors: Int = 0
        get() = field
    private var point: Int = 0
        get() = field

    fun block(type: BlockEnum){
        when(type){
            BlockEnum.ERROR -> errors++
            BlockEnum.POINT -> point++
            BlockEnum.NO_POINT -> noPoint++
        }
    }

    fun getAttempts(): Int{
        return noPoint + errors + point
    }
}
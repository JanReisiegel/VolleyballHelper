package com.reisiegel.volleyballhelper.models

class ReceiveServeStats {
    private var ideals: Int = 0
        get() = field
    private var goods: Int = 0
        get() = field
    private var bads: Int = 0
        get() = field
    private var toOpponent: Int = 0
        get() = field
    private var errors: Int = 0
        get() = field

    fun receiveServe(type: ReceiveServeEnum){
        when(type){
            ReceiveServeEnum.ERROR -> errors++
            ReceiveServeEnum.IDEAL -> ideals++
            ReceiveServeEnum.CAN_CONTINUE -> goods++
            ReceiveServeEnum.CANT_CONTINUE -> bads++
            ReceiveServeEnum.TO_OPPONENT_SIDE -> toOpponent++
        }
    }

    fun getAttempts(): Int{
        return ideals + goods + bads + errors
    }

    fun getStatistics(receiveServeType: ReceiveServeEnum): Int{
        return when(receiveServeType){
            ReceiveServeEnum.ERROR -> errors
            ReceiveServeEnum.IDEAL -> ideals
            ReceiveServeEnum.CAN_CONTINUE -> goods
            ReceiveServeEnum.CANT_CONTINUE -> bads
            ReceiveServeEnum.TO_OPPONENT_SIDE -> toOpponent
        }
    }
}
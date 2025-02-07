package com.reisiegel.volleyballhelper.models

class ServiceStats {
    private var received: Int = 0
        get() = field
    private var errors: Int = 0
        get() = field
    private var aces: Int = 0
        get() = field

    fun serve(type: ServeEnum){
        when(type){
            ServeEnum.ACE -> aces++
            ServeEnum.ERROR -> errors++
            ServeEnum.RECEIVED-> received++
        }
    }

    fun getAttempts(): Int{
        return received + errors + aces
    }

    fun getStatistics(serveType: ServeEnum): Int{
        return when(serveType){
            ServeEnum.ACE -> aces
            ServeEnum.ERROR -> errors
            ServeEnum.RECEIVED -> received
        }
    }
}
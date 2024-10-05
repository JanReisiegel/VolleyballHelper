package com.reisiegel.volleyballhelper.models

class MatchScore(private var teamSets: Int = 0, private var opponentSets: Int = 0, private var teamPoints: ArrayList<Int> = ArrayList<Int>(), private var opponentPoints: ArrayList<Int> = ArrayList<Int>()) {
    private fun setControl(){
        var opponentPoint = opponentPoints.last()
        var teamPoint = teamPoints.last()
        if(teamPoint >= 25 && (teamPoint-opponentPoint)>=2)
            teamSets++
        else if (opponentPoint >= 25 && (opponentPoint-teamPoint)>=2)
            opponentSets++
    }
    fun opponentPoint(){
        var lastIndex = opponentPoints.lastIndex
        opponentPoints[lastIndex]++
        setControl()
    }
    fun teamPoint(){
        var lastIndex = teamPoints.lastIndex
        teamPoints[lastIndex]++
        setControl()
    }
    fun getNumberOfSet(): Int{
        return teamSets + opponentSets + 1
    }
}
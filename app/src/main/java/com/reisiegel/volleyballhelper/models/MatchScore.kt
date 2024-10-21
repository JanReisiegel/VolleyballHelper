package com.reisiegel.volleyballhelper.models

class MatchScore(private var teamSets: Int = 0, private var opponentSets: Int = 0, private var teamPoints: ArrayList<Int> = ArrayList<Int>(), private var opponentPoints: ArrayList<Int> = ArrayList<Int>()) {
    private fun setControl(){
        var opponentPoint = opponentPoints.last()
        var teamPoint = teamPoints.last()
        if(teamPoint >= 25 && (teamPoint-opponentPoint)>=2)
            teamSets++
        else if (opponentPoint >= 25 && (opponentPoint-teamPoint)>=2)
            opponentSets++
        else
            return
        teamPoints.add(0)
        opponentPoints.add(0)
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

    private fun getSetsScores(): String{
        var finalScore: String = "("
        for (index in 0..(teamPoints.lastIndex)){
            finalScore += "${teamPoints[index]}:${opponentPoints[index]},"
        }
        finalScore+=")"
        finalScore.replace(",)",")")
        return finalScore
    }

    fun getFinalScore(): String{
        return "${teamSets}:${opponentSets} ${getSetsScores()}"
    }
}
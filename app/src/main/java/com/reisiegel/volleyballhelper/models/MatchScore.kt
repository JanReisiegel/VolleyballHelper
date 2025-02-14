package com.reisiegel.volleyballhelper.models

class MatchScore(private var teamSets: Int = 0, private var opponentSets: Int = 0, private var teamPoints: ArrayList<Int> = ArrayList<Int>(), private var opponentPoints: ArrayList<Int> = ArrayList<Int>()) {
    private fun setControl(){
        val setNumber = getNumberOfSet()
        if (opponentPoints.isEmpty())
            opponentPoints.add(0)
        if (teamPoints.isEmpty())
            teamPoints.add(0)
        val opponentPoint = opponentPoints[setNumber-1]
        val teamPoint = teamPoints[setNumber-1]
        val testLats = teamPoints.last()
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
        if(opponentPoints.isEmpty())
            opponentPoints.add(0)
        val lastIndex = opponentPoints.lastIndex
        opponentPoints[lastIndex]++
        setControl()
    }
    fun teamPoint(){
        if(teamPoints.isEmpty())
            teamPoints.add(0)
        val lastIndex = teamPoints.lastIndex
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

    fun getFinalScore(): String {
        return "${teamSets}:${opponentSets} ${getSetsScores()}"
    }

    fun getTeamSetScore(setNumber: Int): Int {
        if (teamPoints.size < setNumber)
            teamPoints.add(0)
        return teamPoints[setNumber - 1]
    }

    fun getOpponentScore(setNumber: Int): Int {
        if (opponentPoints.size < setNumber)
            opponentPoints.add(0)
        return opponentPoints[setNumber - 1]
    }

}
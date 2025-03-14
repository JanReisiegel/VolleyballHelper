package com.reisiegel.volleyballhelper.ui.matchchooser

import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentMatchStatisticsBinding
import com.reisiegel.volleyballhelper.models.AttackEnum
import com.reisiegel.volleyballhelper.models.BlockEnum
import com.reisiegel.volleyballhelper.models.Player
import com.reisiegel.volleyballhelper.models.ReceiveServeEnum
import com.reisiegel.volleyballhelper.models.SelectedTournament
import com.reisiegel.volleyballhelper.models.ServeEnum
import com.reisiegel.volleyballhelper.models.SetStates

class MatchStatisticsViewModel() : ViewModel() {
    private val _matchList = MutableLiveData<MutableList<MatchItem>>()
    private val _playersSquad = MutableLiveData<MutableList<Player?>>()
    private val _playersBench = MutableLiveData<MutableList<Player>>()
    private val _serve = MutableLiveData<Boolean>()
    private val _scoreboard = MutableLiveData<String>()
    private val _setState = MutableLiveData<SetStates>()
    private val _setScore = MutableLiveData<String>()
    private val _pageTitle = MutableLiveData<String>()

    private val serveButtonIds = listOf(R.id.service_ace,R.id.service_error,R.id.service_received)
    private val attackButtonIds = listOf(R.id.attack_error,R.id.attack_hit,R.id.attack_received,R.id.attack_block)
    private val blockButtonIds = listOf(R.id.block_point,R.id.block_error,R.id.block_no_point)
    private val receptionButtonIds = listOf(R.id.reception_ideal,R.id.reception_continue,R.id.reception_error,R.id.reception_no_continue)

    val matchList: LiveData<MutableList<MatchItem>> = _matchList
    private val playersSquad: LiveData<MutableList<Player?>> = _playersSquad
    val playersBench: LiveData<MutableList<Player>> = _playersBench
    val serve: LiveData<Boolean> = _serve
    val scoreboard: LiveData<String> = _scoreboard
    val setState: LiveData<SetStates> = _setState
    val setScore: LiveData<String> = _setScore
    val pageTitle: LiveData<String> = _pageTitle

    init {
        _serve.value = true
        _setState.value = SetStates.NONE
        _setScore.value = /*SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActualSetScore() ?:*/ "0:0"
        val allPlayers = /*SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.players*/ emptyList<Player>()
        if (allPlayers != null){
            val activeSquad: MutableList<Player?> = MutableList(6) { null }
            val bench: MutableList<Player> = mutableListOf()
            allPlayers.forEach(){
                if (SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActiveSquad()
                        ?.contains(it.jerseyNumber) == true){
                    val position = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActiveSquad()
                        ?.indexOf(it.jerseyNumber)
                    activeSquad[position!!] = it
                }
                else{
                    bench.add(it)
                }
            }
            _playersSquad.value = activeSquad
            _playersBench.value = bench
        }
    }

    val zoneIds = listOf(
        R.id.zone1, R.id.zone2, R.id.zone3, R.id.zone4, R.id.zone5, R.id.zone6,
    )

    fun zoneStartInit(root: View){
        val context = root.context
        zoneIds.forEachIndexed { index, zoneId ->
            val zoneView = root.findViewById<View>(zoneId)
            val substituteButton = zoneView.findViewById<Button>(R.id.substitute)
            val setPlayer = zoneView.findViewById<Button>(R.id.set_to_zone)
            val attackBlockLayout = zoneView.findViewById<LinearLayout>(R.id.attack_block_layout)
            attackBlockLayout.visibility = View.GONE
            val receptionLayout = zoneView.findViewById<LinearLayout>(R.id.reception_layout)
            receptionLayout.visibility = View.GONE
            val serviceLayout = zoneView.findViewById<LinearLayout>(R.id.service_layout)
            serviceLayout.visibility = View.GONE
            val selectLayout = zoneView.findViewById<LinearLayout>(R.id.select_layout)
            selectLayout.visibility = View.VISIBLE
            val playerName = zoneView.findViewById<TextView>(R.id.player_name)
            playerName.text = "Zóna ${index + 1}"
            val playerNumber = zoneView.findViewById<TextView>(R.id.player_number)
            playerNumber.text = ""
            val attackLayout = zoneView.findViewById<LinearLayout>(R.id.attack_layout)
            val blockLayout = zoneView.findViewById<LinearLayout>(R.id.block_layout)
            val attackBlockSeparator = zoneView.findViewById<View>(R.id.attack_block_separator)
            if(index == 0 || index == 5 || index == 4){
                attackBlockSeparator.visibility = View.GONE
                blockLayout.visibility = View.GONE
            } else{
                attackBlockSeparator.visibility = View.VISIBLE
                blockLayout.visibility = View.VISIBLE
            }
            attackLayout.visibility = View.VISIBLE

            serveButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    if (id == R.id.service_received){
                        _setState.value = SetStates.ATTACK_BLOCK
                    }
                    if (id == R.id.service_error){
                        _setState.value = SetStates.RECEIVE
                    }
                    val newValue = when(id){
                        R.id.service_ace -> "${button.text.split(" ")[0]} - ${serveButtonsAction(ServeEnum.ACE, index)}"
                        R.id.service_error -> "${button.text.split(" ")[0]} - ${serveButtonsAction(ServeEnum.ERROR, index)}"
                        R.id.service_received -> "${button.text.split(" ")[0]} - ${serveButtonsAction(ServeEnum.RECEIVED, index)}"
                        else -> return@setOnClickListener
                    }
                    //TODO: Nefunguje přičítání->podívat se na to
                    button.text = newValue
                    root.requestLayout()
                }

            }

            attackButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    if(id == R.id.attack_hit){
                        _setState.value = SetStates.SERVE
                        if (serve.value?.not()!!){
                            rotateFormation(root)
                        }
                        _serve.value = true
                    }
                    else if (id == R.id.attack_error){
                        _setState.value = SetStates.RECEIVE
                        _serve.value = false
                    }
                    val newValue = when(id){
                        R.id.attack_error -> "${button.text.split(" ")[0]} - ${attackButtonAction(AttackEnum.ERROR, index)}"
                        R.id.attack_hit -> "${button.text.split(" ")[0]} - ${attackButtonAction(AttackEnum.HIT, index)}"
                        R.id.attack_received -> "${button.text.split(" ")[0]} - ${attackButtonAction(AttackEnum.RECEIVED, index)}"
                        R.id.attack_block -> "${button.text.split(" ")[0]} - ${attackButtonAction(AttackEnum.BLOCK, index)}"
                        else -> return@setOnClickListener
                    }
                    //TODO: Nefunguje přičítání->podívat se na to
                    button.text = newValue
                    root.requestLayout()
                }
            }

            blockButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    if (id == R.id.block_point ){
                        _setState.value = SetStates.SERVE
                        if (serve.value?.not()!!){
                            rotateFormation(root)
                        }
                        _serve.value = true
                    }
                    else if (id == R.id.block_error){
                        _setState.value = SetStates.RECEIVE
                        _serve.value = false
                    }
                    val newValue = when(id){
                        R.id.block_point -> "${button.text.split(" ")[0]} - ${blockButtonAction(BlockEnum.POINT, index)}"
                        R.id.block_error -> "${button.text.split(" ")[0]} - ${blockButtonAction(BlockEnum.ERROR, index)}"
                        R.id.block_no_point -> "${button.text.split(" ")[0]} - ${blockButtonAction(BlockEnum.NO_POINT, index)}"
                        else -> return@setOnClickListener
                    }
                    //TODO: Nefunguje přičítání->podívat se na to
                    button.text = newValue
                    root.requestLayout()
                }

            }

            receptionButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    if (id == R.id.reception_ideal || id == R.id.reception_continue || id == R.id.reception_no_continue){
                        _setState.value = SetStates.ATTACK_BLOCK
                    }
                    val newValue = when(id){
                        R.id.reception_ideal -> "${button.text.split(" ")[0]} - ${receiveButtonAction(ReceiveServeEnum.IDEAL, index)}"
                        R.id.reception_continue -> "${button.text.split(" ")[0]} - ${receiveButtonAction(ReceiveServeEnum.CAN_CONTINUE, index)}"
                        R.id.reception_error -> "${button.text.split(" ")[0]} - ${receiveButtonAction(ReceiveServeEnum.ERROR, index)}"
                        R.id.reception_no_continue -> "${button.text.split(" ")[0]} - ${receiveButtonAction(ReceiveServeEnum.CANT_CONTINUE, index)}"
                        else -> return@setOnClickListener
                    }
                    //TODO: Nefunguje přičítání->podívat se na to
                    button.text = newValue
                    root.requestLayout()
                }
            }

            setPlayer.setOnClickListener {
                val players = getBenchedPlayers() ?: emptyList()
                val playerNames = players.map { "#${it.jerseyNumber} - ${it.name}" }.toTypedArray()
                var selectedIndex = -1

                MaterialAlertDialogBuilder(context ?: return@setOnClickListener)
                    .setTitle("Select Player")
                    .setSingleChoiceItems(playerNames, selectedIndex) { _, which ->
                        selectedIndex = which
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selectedIndex != -1) {
                            val player = players[selectedIndex]
                            val playerName = zoneView.findViewById<TextView>(R.id.player_name)
                            val playerNumber = zoneView.findViewById<TextView>(R.id.player_number)
                            playerName.text = player.name
                            playerNumber.text = player.jerseyNumber.toString()
                            if (containsPlayer(index)){
                                addPlayerToSquad(player.jerseyNumber, index, true)
                            }
                            else{
                                addPlayerToSquad(player.jerseyNumber, index)
                            }
                            serveButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.service_ace -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getServeStats(ServeEnum.ACE) ?: 0}"
                                    R.id.service_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getServeStats(ServeEnum.ERROR) ?: 0}"
                                    R.id.service_received -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getServeStats(ServeEnum.RECEIVED) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            attackButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.attack_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.ERROR) ?: 0}"
                                    R.id.attack_hit -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.HIT) ?: 0}"
                                    R.id.attack_received -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.RECEIVED) ?: 0}"
                                    R.id.attack_block -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.BLOCK) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            blockButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.block_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getBlockStats(BlockEnum.ERROR) ?: 0}"
                                    R.id.block_point -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getBlockStats(BlockEnum.POINT) ?: 0}"
                                    R.id.block_no_point -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getBlockStats(BlockEnum.NO_POINT) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            receptionButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.reception_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.ERROR) ?: 0}"
                                    R.id.reception_ideal -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.IDEAL) ?: 0}"
                                    R.id.reception_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.CAN_CONTINUE) ?: 0}"
                                    R.id.reception_no_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.CANT_CONTINUE) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            //SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?. Todo: přidat hráče do sestavy, ale asi až než začne zápas
                            root.requestLayout()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            substituteButton.setOnClickListener {
                val players = getBenchedPlayers() ?: emptyList()
                val playerNames = players.map { "#${it.jerseyNumber} - ${it.name}" }.toTypedArray()
                var selectedIndex = -1

                MaterialAlertDialogBuilder(context ?: return@setOnClickListener)
                    .setTitle("Select Player")
                    .setSingleChoiceItems(playerNames, selectedIndex) { _, which ->
                        selectedIndex = which
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selectedIndex != -1) {
                            val player = players[selectedIndex]
                            if (canSubstitute()) {
                                val dialog =
                                    AlertDialog.Builder(context ?: return@setPositiveButton)
                                        .setTitle("Chyba")
                                        .setMessage("Nemůžete vyměnit hráče")
                                        .setPositiveButton("OK") { dialog, _ ->
                                            dialog.dismiss()
                                            return@setPositiveButton
                                        }
                                        .create()
                                dialog.show()
                            } else {
                                substitution(player.jerseyNumber, index)
                                val playerName = zoneView.findViewById<TextView>(R.id.player_name)
                                val playerNumber =
                                    zoneView.findViewById<TextView>(R.id.player_number)
                                playerName.text = player.name
                                playerNumber.text = player.jerseyNumber.toString()
                            }
                            serveButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.service_ace -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getServeStats(ServeEnum.ACE) ?: 0}"
                                    R.id.service_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getServeStats(ServeEnum.ERROR) ?: 0}"
                                    R.id.service_received -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getServeStats(ServeEnum.RECEIVED) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            attackButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.attack_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.ERROR) ?: 0}"
                                    R.id.attack_hit -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.HIT) ?: 0}"
                                    R.id.attack_received -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.RECEIVED) ?: 0}"
                                    R.id.attack_block -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getAttackStats(AttackEnum.BLOCK) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            blockButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.block_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getBlockStats(BlockEnum.ERROR) ?: 0}"
                                    R.id.block_point -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getBlockStats(BlockEnum.POINT) ?: 0}"
                                    R.id.block_no_point -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getBlockStats(BlockEnum.NO_POINT) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            receptionButtonIds.forEach { id ->
                                val button = zoneView.findViewById<Button>(id)
                                val newValue = when(id){
                                    R.id.reception_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.ERROR) ?: 0}"
                                    R.id.reception_ideal -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.IDEAL) ?: 0}"
                                    R.id.reception_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.CAN_CONTINUE) ?: 0}"
                                    R.id.reception_no_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(player.jerseyNumber)?.getReceiveStats(ReceiveServeEnum.CANT_CONTINUE) ?: 0}"
                                    else -> return@forEach
                                }
                                button.text = newValue
                            }
                            root.requestLayout()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun getBenchedPlayers(): MutableList<Player>? {
        return _playersBench.value
    }

    fun addMatchItem(match: MatchItem){
        val updatedMatchList = _matchList.value ?: mutableListOf()
        if (updatedMatchList.contains(match)) return
        updatedMatchList.add(match)
        _matchList.value = updatedMatchList
    }

    private fun addPlayerToSquad(jerseyNumber: Int, position: Int, change: Boolean = false){
        val player = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(jerseyNumber)
        if (player == null) return
        if (!change) {
            val updatedPlayersSquad = _playersSquad.value ?: mutableListOf()
            updatedPlayersSquad[position] = player
            _playersSquad.value = updatedPlayersSquad

            val updatedPlayersBench = _playersBench.value ?: mutableListOf()
            updatedPlayersBench.remove(player)
            _playersBench.value = updatedPlayersBench
        }
        else{
            val updatedPlayersSquad = _playersSquad.value ?: mutableListOf()
            val playerToBench = updatedPlayersSquad[position]
            updatedPlayersSquad[position] = player
            _playersSquad.value = updatedPlayersSquad

            val updatedPlayersBench = _playersBench.value ?: mutableListOf()
            updatedPlayersBench.add(playerToBench!!)
            updatedPlayersBench.remove(player)
            _playersBench.value = updatedPlayersBench
        }
    }

    fun opponentPoint(){
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.opponentPoint()
        changeScoreboard()
    }

    fun opponentError(){
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.opponentError()
        changeScoreboard()
    }

    fun matchSelected(index: Int?, root: View){
        if(index == null){
            SelectedTournament.selectedMatchIndex = null
            _pageTitle.value = SelectedTournament.selectedTournament?.name
            return
        }
        SelectedTournament.selectedMatchIndex = index
        val allPlayers = SelectedTournament.selectedTournament?.getMatch(index)?.players
        if (allPlayers != null){
            val activeSquad: MutableList<Player?> = MutableList(6) { null }
            val bench: MutableList<Player> = mutableListOf()
            var exceptionErrors: Int = 0
            allPlayers.forEach {
                try{
                    if (SelectedTournament.selectedTournament?.getMatch(index)?.getActiveSquad()
                            ?.contains(it.jerseyNumber) == true){
                        val position = SelectedTournament.selectedTournament?.getMatch(index)?.getActiveSquad()
                            ?.indexOf(it.jerseyNumber)
                        activeSquad[position!!] = it
                    }
                    else{
                        bench.add(it)
                    }
                } catch (e: Exception){
                   exceptionErrors++
                }
            }
            if (activeSquad.size != 0) {
                zoneIds.forEachIndexed { index, zoneId ->
                    val player = activeSquad[index]
                    val zoneView = root.findViewById<View>(zoneId)
                    val playerName = zoneView.findViewById<TextView>(R.id.player_name)
                    val playerNumber = zoneView.findViewById<TextView>(R.id.player_number)
                    if (player != null) {
                        playerName.text = player.name
                        playerNumber.text = player.jerseyNumber.toString()
                    } else {
                        playerName.text = "Zóna ${index + 1}"
                        playerNumber.text = ""
                    }
                    serveButtonIds.forEach { id ->
                        val button = zoneView.findViewById<Button>(id)
                        val newValue = when (id) {
                            R.id.service_ace -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getServeStats(ServeEnum.ACE) ?: 0
                            }"

                            R.id.service_error -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getServeStats(ServeEnum.ERROR) ?: 0
                            }"

                            R.id.service_received -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getServeStats(ServeEnum.RECEIVED) ?: 0
                            }"

                            else -> return@forEach
                        }
                        button.text = newValue
                    }
                    attackButtonIds.forEach { id ->
                        val button = zoneView.findViewById<Button>(id)
                        val newValue = when (id) {
                            R.id.attack_error -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getAttackStats(AttackEnum.ERROR) ?: 0
                            }"

                            R.id.attack_hit -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getAttackStats(AttackEnum.HIT) ?: 0
                            }"

                            R.id.attack_received -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getAttackStats(AttackEnum.RECEIVED) ?: 0
                            }"

                            R.id.attack_block -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getAttackStats(AttackEnum.BLOCK) ?: 0
                            }"

                            else -> return@forEach
                        }
                        button.text = newValue
                    }
                    blockButtonIds.forEach { id ->
                        val button = zoneView.findViewById<Button>(id)
                        val newValue = when (id) {
                            R.id.block_error -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getBlockStats(BlockEnum.ERROR) ?: 0
                            }"

                            R.id.block_point -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getBlockStats(BlockEnum.POINT) ?: 0
                            }"

                            R.id.block_no_point -> "${button.text.split(" ")[0]} - ${
                                SelectedTournament.selectedTournament?.getMatch(
                                    index
                                )?.getPlayer(player?.jerseyNumber ?: 0)
                                    ?.getBlockStats(BlockEnum.NO_POINT) ?: 0
                            }"

                            else -> return@forEach
                        }
                        button.text = newValue
                    }
                    receptionButtonIds.forEach { id ->
                        val button = zoneView.findViewById<Button>(id)
                        val newValue = when (id) {
                            R.id.reception_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(index)?.getPlayer(player?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.ERROR) ?: 0}"
                            R.id.reception_ideal -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(index)?.getPlayer(player?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.IDEAL) ?: 0}"
                            R.id.reception_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(index)?.getPlayer(player?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.CAN_CONTINUE) ?: 0}"
                            R.id.reception_no_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(index)?.getPlayer(player?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.CANT_CONTINUE) ?: 0}"
                            else -> return@forEach
                        }
                        button.text = newValue

                    }
                }
            }
            Log.e("ExceptionErrors", exceptionErrors.toString())
            _playersSquad.value = activeSquad
            _playersBench.value = bench
        }
        _setScore.value = SelectedTournament.selectedTournament?.getMatch(index)?.getActualSetScore() ?: "0:0"
        _scoreboard.value = SelectedTournament.selectedTournament?.getMatch(index)?.getActualScore() ?: "0:0"
        _pageTitle.value = SelectedTournament.selectedTournament?.name + " × " + SelectedTournament.selectedTournament?.getMatch(index)?.opponentName
    }

    private fun containsPlayer(index: Int): Boolean {
        return _playersSquad.value?.get(index) != null
    }

    private fun substitution(jerseyNumber: Int, position: Int){
        val player = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(jerseyNumber)
        if (player == null) return
        val updatedPlayersSquad = _playersSquad.value ?: mutableListOf()
        val playerToBench = updatedPlayersSquad[position]
        if (playerToBench == null) return
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)
            ?.addSubstitution(player.jerseyNumber, playerToBench.jerseyNumber)

        updatedPlayersSquad[position] = player
        _playersSquad.value = updatedPlayersSquad

        val updatedPlayersBench = _playersBench.value ?: mutableListOf()
        updatedPlayersBench.add(playerToBench)
        updatedPlayersBench.remove(player)
        _playersBench.value = updatedPlayersBench
    }

    private fun serveButtonsAction(serveType: ServeEnum, playerZone: Int): Int {

        val playerNumber = playersSquad.value?.get(playerZone)?.jerseyNumber ?: return 0
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerServe(playerNumber, serveType)
        val result = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getServeStats(serveType) ?: 0
        if(serveType == ServeEnum.ACE){
            changeScoreboard()
        }
        if (serveType == ServeEnum.ERROR){
            changeScoreboard()
            changeServe()
        }
        return result
    }

    private fun attackButtonAction(attackType: AttackEnum, playerZone: Int): Int {
        val playerNumber = playersSquad.value?.get(playerZone)?.jerseyNumber ?: return 0
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerAttack(playerNumber, attackType)
        val result = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getAttackStats(attackType) ?: 0
        if (attackType == AttackEnum.ERROR || attackType == AttackEnum.BLOCK){
            if (serve.value == true){
                changeServe()
            }
            changeScoreboard()
            _setState.value = SetStates.RECEIVE
        } else if(attackType == AttackEnum.HIT){
            if (serve.value == false){
                changeServe()
            }
            changeScoreboard()
            _setState.value = SetStates.SERVE
        }
        return result
    }

    private fun blockButtonAction(blockType: BlockEnum, playerZone: Int): Int {

        val playerNumber = playersSquad.value?.get(playerZone)?.jerseyNumber ?: return 0
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerBlock(playerNumber, blockType)
        val result = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getBlockStats(blockType) ?: 0
        if (blockType == BlockEnum.ERROR){
            if (serve.value == true){
                changeServe()
            }
            changeScoreboard()
        } else if(blockType == BlockEnum.POINT){
            if (serve.value == false){
                changeServe()
            }
            changeScoreboard()
        }
        return result
    }

    private fun receiveButtonAction(receiveType: ReceiveServeEnum, playerZone: Int): Int {
        val playerNumber = playersSquad.value?.get(playerZone)?.jerseyNumber ?: return 0
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.playerReceivedServe(playerNumber, receiveType)
        val result = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(playerNumber)?.getReceiveStats(receiveType) ?: 0
        if (receiveType == ReceiveServeEnum.ERROR || receiveType == ReceiveServeEnum.CANT_CONTINUE){
            changeScoreboard()
        }
        return result
    }

    fun changeServe(){
        _serve.value = !_serve.value!!
    }

    fun canStartSet(): Boolean {
        return _playersSquad.value?.contains(null) != true
    }

    private fun canSubstitute(): Boolean {
        return _scoreboard.value == "0:0" || _scoreboard.value == null
    }

    fun clearSquad(){
        _playersSquad.value = MutableList(6) { null }
        _playersBench.value = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.players?.toMutableList()
    }

    fun changeServeStartSet(){
        val serve = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getSetStartService()
        _serve.value = serve ?: true
    }

    private fun changeScoreboard(){
        val teamScore = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActualScore()
        val newSetScore: String? = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getActualSetScore()
        if (newSetScore != setScore.value){
            _setState.value = SetStates.END_SET
            _setScore.value = newSetScore ?: "Error"
            clearSquad()
        }
        _scoreboard.value = teamScore ?: "Error"
    }

    fun setSetState(state: SetStates){
        _setState.value = state
    }

    fun changeZones(root: View, state: SetStates){
        zoneIds.forEachIndexed { index, it ->
            val zoneView = root.findViewById<View>(it)
            val selectLayout = zoneView.findViewById<LinearLayout>(it).findViewById<LinearLayout>(R.id.select_layout)
            val serviceLayout = zoneView.findViewById<LinearLayout>(it).findViewById<LinearLayout>(R.id.service_layout)
            val attackBlockLayout = zoneView.findViewById<LinearLayout>(it).findViewById<LinearLayout>(R.id.attack_block_layout)
            val receptionLayout = zoneView.findViewById<LinearLayout>(it).findViewById<LinearLayout>(R.id.reception_layout)
            val substitutionButton = zoneView.findViewById<Button>(R.id.substitute)
            val playerName = zoneView.findViewById<TextView>(R.id.player_name)
            val playerNumber = zoneView.findViewById<TextView>(R.id.player_number)
            when(state){
                SetStates.NONE -> {
                    substitutionButton.visibility = View.GONE
                    selectLayout.visibility = View.VISIBLE
                    serviceLayout.visibility = View.GONE
                    attackBlockLayout.visibility = View.GONE
                    receptionLayout.visibility = View.GONE
                    playerName.text = "Zóna ${index + 1}"
                    playerNumber.text = ""
                }
                SetStates.END_SET -> {
                    substitutionButton.visibility = View.GONE
                    selectLayout.visibility = View.VISIBLE
                    serviceLayout.visibility = View.GONE
                    attackBlockLayout.visibility = View.GONE
                    receptionLayout.visibility = View.GONE
                    playerName.text = "Zóna ${index + 1}"
                    playerNumber.text = ""
                    //_serve.value = SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getSetStartService()
                }
                SetStates.SERVE -> {
                    if (index == 0)
                        serviceLayout.visibility = View.VISIBLE
                    else
                        serviceLayout.visibility = View.GONE
                    substitutionButton.visibility = View.VISIBLE
                    selectLayout.visibility = View.GONE
                    attackBlockLayout.visibility = View.GONE
                    receptionLayout.visibility = View.GONE
                }
                SetStates.ATTACK_BLOCK -> {
                    substitutionButton.visibility = View.VISIBLE
                    selectLayout.visibility = View.GONE
                    serviceLayout.visibility = View.GONE
                    attackBlockLayout.visibility = View.VISIBLE
                    receptionLayout.visibility = View.GONE

                }
                SetStates.RECEIVE -> {
                    substitutionButton.visibility = View.VISIBLE
                    selectLayout.visibility = View.GONE
                    serviceLayout.visibility = View.GONE
                    attackBlockLayout.visibility = View.GONE
                    receptionLayout.visibility = View.VISIBLE
                }
            }
        }

    }

    /**
     * Rotate formation (1->6,2->1,3->2,4->3,5->4,6->5)
     */
    fun rotateFormation(root: View){
        val updatedPlayersSquad = _playersSquad.value ?: mutableListOf<Player?>(null, null, null, null, null, null)
        val player = updatedPlayersSquad[0]
        for (i in 0..4){
            updatedPlayersSquad[i] = updatedPlayersSquad[i+1]
        }
        updatedPlayersSquad[5] = player
        _playersSquad.value = updatedPlayersSquad

        zoneIds.forEachIndexed { index, i ->
            val zoneView = root.findViewById<View>(i)
            val playerName = zoneView.findViewById<TextView>(R.id.player_name)
            val playerNumber = zoneView.findViewById<TextView>(R.id.player_number)
            playerName.text = updatedPlayersSquad[index]?.name ?: "Zóna ${index + 1}"
            playerNumber.text = updatedPlayersSquad[index]?.jerseyNumber?.toString() ?: ""

            serveButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                val newValue = when(id){
                    R.id.service_ace -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getServeStats(ServeEnum.ACE) ?: 0}"
                    R.id.service_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getServeStats(ServeEnum.ERROR) ?: 0}"
                    R.id.service_received -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getServeStats(ServeEnum.RECEIVED) ?: 0}"
                    else -> return@forEach
                }
                button.text = newValue
            }

            attackButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                val newValue = when(id){
                    R.id.attack_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getAttackStats(AttackEnum.ERROR) ?: 0}"
                    R.id.attack_hit -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getAttackStats(AttackEnum.HIT) ?: 0}"
                    R.id.attack_received -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getAttackStats(AttackEnum.RECEIVED) ?: 0}"
                    R.id.attack_block -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getAttackStats(AttackEnum.BLOCK) ?: 0}"
                    else -> return@forEach
                }
                button.text = newValue
            }

            blockButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                val newValue = when(id){
                    R.id.block_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getBlockStats(BlockEnum.ERROR) ?: 0}"
                    R.id.block_point -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getBlockStats(BlockEnum.POINT) ?: 0}"
                    R.id.block_no_point -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getBlockStats(BlockEnum.NO_POINT) ?: 0}"
                    else -> return@forEach
                }
                button.text = newValue
            }

            receptionButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                val newValue = when(id){
                    R.id.reception_error -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.ERROR) ?: 0}"
                    R.id.reception_ideal -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.IDEAL) ?: 0}"
                    R.id.reception_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.CAN_CONTINUE) ?: 0}"
                    R.id.reception_no_continue -> "${button.text.split(" ")[0]} - ${SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.getPlayer(updatedPlayersSquad[index]?.jerseyNumber ?: 0)?.getReceiveStats(ReceiveServeEnum.CANT_CONTINUE) ?: 0}"
                    else -> return@forEach
                }
                button.text = newValue
            }
        }
        root.requestLayout()
    }

    fun endMatch(binding: FragmentMatchStatisticsBinding ){
        _matchList.value?.get(SelectedTournament.selectedMatchIndex!!)?.setFinished(true)

        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.finishMatch()
        SelectedTournament.selectedMatchIndex = null

        _pageTitle.value = SelectedTournament.selectedTournament?.name
        _setState.value = SetStates.NONE

        binding.matchStatistics.visibility = View.GONE
        binding.matchList.visibility = View.VISIBLE
        binding.root.requestLayout()

        //TODO: Uložení do souboru
    }

    fun closeMatch(binding: FragmentMatchStatisticsBinding){
        SelectedTournament.selectedMatchIndex = null
        _pageTitle.value = SelectedTournament.selectedTournament?.name
        _setState.value = SetStates.NONE

        binding.matchStatistics.visibility = View.GONE
        binding.matchList.visibility = View.VISIBLE
        binding.root.requestLayout()
    }

    fun startSet(){
        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.setSquad(playersSquad.value!!.toList())
        if (_serve.value == true){
            setSetState(SetStates.SERVE)
        }
        else{
            setSetState(SetStates.RECEIVE)
        }
    }
}
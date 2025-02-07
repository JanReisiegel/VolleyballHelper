package com.reisiegel.volleyballhelper.ui.matchchooser

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentMatchStatisticsBinding
import com.reisiegel.volleyballhelper.models.AttackEnum
import com.reisiegel.volleyballhelper.models.BlockEnum
import com.reisiegel.volleyballhelper.models.ReceiveServeEnum
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchAdapter
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchItem
import com.reisiegel.volleyballhelper.models.SelectedTournament
import com.reisiegel.volleyballhelper.models.ServeEnum
import com.reisiegel.volleyballhelper.models.Tournament
import java.io.File

class MatchStatistics : Fragment() {

    private lateinit var viewModel: MatchStatisticsViewModel
    private var _binding: FragmentMatchStatisticsBinding? = null
    private val binding get() = _binding!!


    private lateinit var recycleMatchesView: RecyclerView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(MatchStatisticsViewModel::class.java)
        _binding = FragmentMatchStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        SelectedTournament.selectedTournament = Tournament.loadFromJson(File(context?.filesDir, "Statistics/${SelectedTournament.filePath}"))

        val matchListLayout = binding.matchList
        val statisticsLayout = binding.matchStatistics
        if (SelectedTournament.selectedMatchIndex == null){
            matchListLayout.visibility = View.VISIBLE
            statisticsLayout.visibility = View.INVISIBLE
        }
        else {
            matchListLayout.visibility = View.INVISIBLE
            statisticsLayout.visibility = View.VISIBLE
        }


//        binding.toListButton.setOnClickListener {
//            viewModel.matchSelected(null)
//            matchListLayout.visibility = View.VISIBLE
//            statisticsLayout.visibility = View.INVISIBLE
//            root.requestLayout()
//        }

        val matchList = SelectedTournament.selectedTournament?.getmatchesArrayList()

        matchList?.forEach {
            match -> viewModel.addMatchItem(MatchItem(match.opponentName, match.startTime))
        }

        binding.serveButton.setOnClickListener {
            viewModel.changeServe()
            binding.serveButton.text = if (viewModel.serve.value == true) "Serve" else "Receive"
        }

        val zoneIds = listOf(
            R.id.zone1, R.id.zone2, R.id.zone3, R.id.zone4, R.id.zone5, R.id.zone6,
        )

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

            val serveButtonIds = listOf(R.id.service_ace,R.id.service_error,R.id.service_received)
            val attackButtonIds = listOf(R.id.attack_error,R.id.attack_hit,R.id.attack_received,R.id.attack_block)
            val blockButtonIds = listOf(R.id.block_point,R.id.block_error,R.id.block_no_point)
            val receptionButtonIds = listOf(R.id.reception_ideal,R.id.reception_continue,R.id.reception_error,R.id.reception_no_continue)

            serveButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    val newValue = when(id){
                        R.id.service_ace -> "${button.text.split(" ")[0]} - ${viewModel.serveButtonsAction(ServeEnum.ACE, index)}"
                        R.id.service_error -> "${button.text.split(" ")[0]} - ${viewModel.serveButtonsAction(ServeEnum.ERROR, index)}"
                        R.id.service_received -> "${button.text.split(" ")[0]} - ${viewModel.serveButtonsAction(ServeEnum.RECEIVED, index)}"
                        else -> return@setOnClickListener
                    }
                }
            }

            attackButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    val newValue = when(id){
                        R.id.attack_error -> "${button.text.split(" ")[0]} - ${viewModel.attackButtonAction(AttackEnum.ERROR, index)}"
                        R.id.attack_hit -> "${button.text.split(" ")[0]} - ${viewModel.attackButtonAction(AttackEnum.HIT, index)}"
                        R.id.attack_received -> "${button.text.split(" ")[0]} - ${viewModel.attackButtonAction(AttackEnum.RECEIVED, index)}"
                        R.id.attack_block -> "${button.text.split(" ")[0]} - ${viewModel.attackButtonAction(AttackEnum.BLOCK, index)}"
                        else -> return@setOnClickListener
                    }
                }
            }

            blockButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    val newValue = when(id){
                        R.id.block_point -> "${button.text.split(" ")[0]} - ${viewModel.blockButtonAction(BlockEnum.POINT, index)}"
                        R.id.block_error -> "${button.text.split(" ")[0]} - ${viewModel.blockButtonAction(BlockEnum.ERROR, index)}"
                        R.id.block_no_point -> "${button.text.split(" ")[0]} - ${viewModel.blockButtonAction(BlockEnum.NO_POINT, index)}"
                        else -> return@setOnClickListener
                    }
                }
            }

            receptionButtonIds.forEach { id ->
                val button = zoneView.findViewById<Button>(id)
                button.setOnClickListener {
                    val newValue = when(id){
                        R.id.reception_ideal -> "${button.text.split(" ")[0]} - ${viewModel.receiveButtonAction(ReceiveServeEnum.IDEAL, index)}"
                        R.id.reception_continue -> "${button.text.split(" ")[0]} - ${viewModel.receiveButtonAction(ReceiveServeEnum.CAN_CONTINUE, index)}"
                        R.id.reception_error -> "${button.text.split(" ")[0]} - ${viewModel.receiveButtonAction(ReceiveServeEnum.ERROR, index)}"
                        R.id.reception_no_continue -> "${button.text.split(" ")[0]} - ${viewModel.receiveButtonAction(ReceiveServeEnum.CANT_CONTINUE, index)}"
                        else -> return@setOnClickListener
                    }
                }
            }

            setPlayer.setOnClickListener {
                val players = viewModel.getBanchedPlayers() ?: emptyList()
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
                            if (viewModel.containsPlayer(index)){
                                viewModel.addPlayerToSquad(player.jerseyNumber, index, true)
                            }
                            else{
                                viewModel.addPlayerToSquad(player.jerseyNumber, index)
                            }
                            root.requestLayout()
                            substituteButton.visibility = View.VISIBLE
                            attackBlockLayout.visibility = View.VISIBLE
                            selectLayout.visibility = View.GONE
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            substituteButton.setOnClickListener {
                val players = viewModel.getBanchedPlayers() ?: emptyList()
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

                            root.requestLayout()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }


        }


        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycleMatchesView = _binding?.matchListView ?: return

        recycleMatchesView.layoutManager = LinearLayoutManager(requireContext())

        val matchesAdapter = MatchAdapter(viewModel.matchList.value?.toMutableList() ?: mutableListOf(), requireContext(), view) {
            viewModel.matchSelected(it)
        }
        recycleMatchesView.adapter = matchesAdapter

        viewModel.matchList.observe(viewLifecycleOwner){
            matches -> matchesAdapter?.updateItems(matches)
                matchesAdapter?.notifyDataSetChanged()
        }

        viewModel.scoreboard.observe(viewLifecycleOwner){
            scoreboard -> binding.matchScore.text = scoreboard
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
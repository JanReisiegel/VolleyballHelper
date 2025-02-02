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
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentMatchStatisticsBinding
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchAdapter
import com.reisiegel.volleyballhelper.ui.matchchooser.MatchItem
import com.reisiegel.volleyballhelper.models.SelectedTournament
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

        binding.toStatisticsButton.setOnClickListener {
            SelectedTournament.selectedMatchIndex = 1
            matchListLayout.visibility = View.INVISIBLE
            statisticsLayout.visibility = View.VISIBLE
            root.requestLayout()
        }

        binding.toListButton.setOnClickListener {
            SelectedTournament.selectedMatchIndex = null
            matchListLayout.visibility = View.VISIBLE
            statisticsLayout.visibility = View.INVISIBLE
            root.requestLayout()
        }

        val matchList = SelectedTournament.selectedTournament?.getmatchesArrayList()

        matchList?.forEach {
            match -> viewModel.addMatchItem(MatchItem(match.opponentName, match.startTime))
        }

        val zoneIds = listOf(
            R.id.zone1, R.id.zone6, R.id.zone5,
            R.id.zone2, R.id.zone3, R.id.zone4,
        )

        zoneIds.forEachIndexed { index, zoneId ->
            val zoneView = root.findViewById<View>(zoneId)
            val attackBlockLayout = zoneView.findViewById<LinearLayout>(R.id.attack_block_layout)
            val receptionLayout = zoneView.findViewById<LinearLayout>(R.id.reception_layout)
            val serviceLayout = zoneView.findViewById<LinearLayout>(R.id.service_layout)
            val playerName = zoneView.findViewById<TextView>(R.id.player_name)
            val playerNumber = zoneView.findViewById<TextView>(R.id.player_number)
            val setPlayer = zoneView.findViewById<Button>(R.id.set_to_zone)
            val serviceAce = zoneView.findViewById<Button>(R.id.service_ace)
            val serviceError = zoneView.findViewById<Button>(R.id.service_error)
            val serviceReceived = zoneView.findViewById<Button>(R.id.service_received)
            val attackError = zoneView.findViewById<Button>(R.id.attack_error)
            val attackHit = zoneView.findViewById<Button>(R.id.attack_hit)
            val attackReceived = zoneView.findViewById<Button>(R.id.attack_received)
            val attackBlocked = zoneView.findViewById<Button>(R.id.attack_block)
            val blockPoint = zoneView.findViewById<Button>(R.id.block_point)
            val blockError = zoneView.findViewById<Button>(R.id.block_error)
            val blockNoPoint = zoneView.findViewById<Button>(R.id.block_no_point)
            val receptionIdeal = zoneView.findViewById<Button>(R.id.reception_ideal)
            val receptionContinue = zoneView.findViewById<Button>(R.id.reception_continue)
            val receptionError = zoneView.findViewById<Button>(R.id.reception_error)
            val receptionNoContinue = zoneView.findViewById<Button>(R.id.reception_no_continue)


        }


        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycleMatchesView = _binding?.matchListView ?: return

        recycleMatchesView.layoutManager = LinearLayoutManager(requireContext())

        val matchesAdapter = MatchAdapter(viewModel.matchList.value?.toMutableList() ?: mutableListOf(), requireContext(), view)
        recycleMatchesView.adapter = matchesAdapter

        viewModel.matchList.observe(viewLifecycleOwner){
            matches -> matchesAdapter?.updateItems(matches)
                matchesAdapter?.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
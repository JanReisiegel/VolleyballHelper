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
import androidx.appcompat.app.AlertDialog
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
import com.reisiegel.volleyballhelper.models.SetStates
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
            binding.serveButton.text = if (viewModel.serve.value == true) "Podání" else "Příjem"
        }



        viewModel.zoneStartInit(root)

        binding.startSet.setOnClickListener {
            val canStart = viewModel.canStartSet()
            var text = "Set Začal"
            if (!canStart) {
                text = "Set nemohl začít"
            }
            else{
                viewModel.zoneIds.forEachIndexed { index, zoneId ->
                    val zoneView = root.findViewById<View>(zoneId)
                    val selectLayout = zoneView.findViewById<LinearLayout>(R.id.select_layout)
                    val serviceLayout = zoneView.findViewById<LinearLayout>(R.id.service_layout)
                    val attackBlockLayout = zoneView.findViewById<LinearLayout>(R.id.attack_block_layout)
                    val receptionLayout = zoneView.findViewById<LinearLayout>(R.id.reception_layout)
                    val substitutionButton = zoneView.findViewById<Button>(R.id.substitute)
                    if (viewModel.serve.value == true && index == 0){
                        serviceLayout.visibility = View.VISIBLE
                        attackBlockLayout.visibility = View.GONE
                        receptionLayout.visibility = View.GONE
                    }
                    else if (viewModel.serve.value == true){
                        selectLayout.visibility = View.GONE
                        serviceLayout.visibility = View.GONE
                        attackBlockLayout.visibility = View.VISIBLE
                        receptionLayout.visibility = View.GONE
                    }
                    else{
                        selectLayout.visibility = View.GONE
                        serviceLayout.visibility = View.GONE
                        attackBlockLayout.visibility = View.GONE
                        receptionLayout.visibility = View.VISIBLE
                    }
                    selectLayout.visibility = View.GONE
                    substitutionButton.visibility = View.VISIBLE

                }
            }
            val dialog = AlertDialog.Builder(context ?: return@setOnClickListener)
                .setTitle("Chyba")
                .setMessage("Set začal")
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
            dialog.show()
            binding.opponentError.visibility = View.VISIBLE
            binding.serveButton.isEnabled = false
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

        viewModel.setState.observe(viewLifecycleOwner){
            state -> when(state){
                SetStates.NONE -> {
                    binding.serveButton.isEnabled = true
                    binding.startSet.visibility = View.VISIBLE
                    binding.opponentError.visibility = View.GONE
                    binding.endMatch.visibility = View.GONE
                }
                SetStates.SERVE -> {
                    binding.startSet.visibility = View.GONE
                    binding.opponentError.visibility = View.VISIBLE
                    binding.serveButton.isEnabled = false
                    binding.endMatch.visibility = View.GONE
                }
                SetStates.RECEIVE -> {
                    binding.startSet.isEnabled = false
                    binding.opponentError.isEnabled = false
                    binding.endMatch.visibility = View.GONE
                }
                SetStates.ATTACK_BLOCK -> {
                    binding.startSet.isEnabled = false
                    binding.opponentError.isEnabled = false
                    binding.endMatch.visibility = View.GONE
                }
                SetStates.END_SET -> {
                    viewModel.clearSquad()
                    binding.startSet.isEnabled = false
                    binding.opponentError.isEnabled = false
                    binding.endMatch.visibility = View.VISIBLE
                }

            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
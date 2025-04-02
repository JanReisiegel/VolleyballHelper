package com.reisiegel.volleyballhelper.ui.matchchooser

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentMatchStatisticsBinding
import com.reisiegel.volleyballhelper.models.SelectedTournament
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
        viewModel = ViewModelProvider(this)[MatchStatisticsViewModel::class.java]
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

        val matchList = SelectedTournament.selectedTournament?.getMatchesArrayList()

        matchList?.forEach {
            match -> viewModel.addMatchItem(MatchItem(match.opponentName, match.startTime, match.isFinished()))
        }

        binding.serveButton.setOnClickListener {
            viewModel.changeServe()
            SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex ?: return@setOnClickListener)?.changeStartServe(viewModel.serve.value ?: return@setOnClickListener)
            binding.serveButton.text = if (viewModel.serve.value == true) getString(R.string.serve) else getString(R.string.reception)
        }



        viewModel.zoneStartInit(root)

        binding.startSet.setOnClickListener {
            val canStart = viewModel.canStartSet()
            if (!canStart) {
                val text = getString(R.string.set_could_not_start)
                val dialog = AlertDialog.Builder(context ?: return@setOnClickListener)
                    .setTitle(getString(R.string.error_header))
                    .setMessage(text)
                    .setPositiveButton(getString(R.string.OK)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                dialog.show()
                return@setOnClickListener
            }
            else{
                viewModel.startSet()
            }
            binding.opponentError.visibility = View.VISIBLE
            binding.serveButton.isEnabled = false

            binding.opponentPoint.setOnClickListener {
                viewModel.changeZones(root, SetStates.RECEIVE)
                viewModel.opponentPoint()
            }
            binding.opponentError.setOnClickListener {
                viewModel.opponentError(root)
                viewModel.changeZones(root, SetStates.SERVE)
            }
        }




        (requireActivity() as AppCompatActivity).supportActionBar?.title = SelectedTournament.selectedTournament?.name

        return root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycleMatchesView = _binding?.matchListView ?: return

        recycleMatchesView.layoutManager = LinearLayoutManager(requireContext())

        binding.endMatch.setOnClickListener {
            viewModel.endMatch(binding)
            val newMatchesAdapter = MatchAdapter(viewModel.matchList.value?.toMutableList() ?: mutableListOf(), requireContext(), view) {
                viewModel.matchSelected(it, binding.root.rootView)
            }
            recycleMatchesView.adapter = newMatchesAdapter

            viewModel.matchList.observe(viewLifecycleOwner){
                matches -> newMatchesAdapter.updateItems(matches)
                newMatchesAdapter.notifyDataSetChanged()
            }
        }

        binding.backToList.setOnClickListener {
            val dialog =
                AlertDialog.Builder(context ?: return@setOnClickListener)
                    .setTitle(getString(R.string.end_match_header))
                    .setMessage(getString(R.string.end_match_message))
                    .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
//                        SelectedTournament.selectedTournament?.getMatch(SelectedTournament.selectedMatchIndex!!)?.finishMatch()
//                        SelectedTournament.selectedMatchIndex = null
//                        viewModel.matchSelected(null)
//                        matchListLayout.visibility = View.VISIBLE
//                        statisticsLayout.visibility = View.INVISIBLE
//
//                        viewModel.setSetState(SetStates.NONE)
                        viewModel.endMatch(binding)
                        val newMatchesAdapter = MatchAdapter(viewModel.matchList.value?.toMutableList() ?: mutableListOf(), requireContext(), view) {
                            viewModel.matchSelected(it, binding.root.rootView)
                        }
                        recycleMatchesView.adapter = newMatchesAdapter

                        viewModel.matchList.observe(viewLifecycleOwner){
                                matches -> newMatchesAdapter.updateItems(matches)
                            newMatchesAdapter.notifyDataSetChanged()
                        }
                        requireView().requestLayout()
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        viewModel.closeMatch(binding)
                        dialog.dismiss()
                    }
                    .create()
            dialog.show()
        }

        val matchesAdapter = MatchAdapter(viewModel.matchList.value?.toMutableList() ?: mutableListOf(), requireContext(), view) {
            viewModel.matchSelected(it, binding.root.rootView)
        }
        recycleMatchesView.adapter = matchesAdapter

        viewModel.matchList.observe(viewLifecycleOwner){
                matches -> matchesAdapter.updateItems(matches)
            matchesAdapter.notifyDataSetChanged()
        }

        viewModel.scoreboard.observe(viewLifecycleOwner){
            scoreboard -> binding.setScore.text = scoreboard
        }

        viewModel.setScore.observe(viewLifecycleOwner){
            setScore -> binding.matchScore.text = setScore
        }

        viewModel.setState.observe(viewLifecycleOwner){
            state -> when(state){
                SetStates.NONE -> {
                    binding.startSet.visibility = View.VISIBLE
                    binding.opponentError.visibility = View.GONE
                    binding.endMatch.visibility = View.GONE
                    binding.serveButton.isEnabled = true
                    binding.opponentPoint.visibility = View.GONE
                    viewModel.changeZones(binding.root.rootView, state)
                }
                SetStates.SERVE -> {
                    binding.startSet.visibility = View.GONE
                    binding.opponentError.visibility = View.VISIBLE
                    binding.serveButton.isEnabled = false
                    binding.endMatch.visibility = View.GONE
                    viewModel.changeZones(binding.root.rootView, state)
                    binding.opponentPoint.visibility = View.VISIBLE
                }
                SetStates.RECEIVE -> {
                    binding.opponentError.visibility = View.VISIBLE
                    binding.endMatch.visibility = View.GONE
                    binding.startSet.visibility = View.GONE
                    binding.serveButton.isEnabled = false
                    viewModel.changeZones(binding.root.rootView, state)
                    binding.opponentPoint.visibility = View.VISIBLE
                }
                SetStates.ATTACK_BLOCK -> {
                    binding.opponentError.visibility = View.VISIBLE
                    binding.endMatch.visibility = View.GONE
                    binding.serveButton.isEnabled = false
                    viewModel.changeZones(binding.root.rootView, state)
                    binding.opponentPoint.visibility = View.VISIBLE
                }
                SetStates.END_SET -> {
                    viewModel.clearSquad()
                    binding.opponentError.visibility = View.GONE
                    binding.startSet.visibility = View.VISIBLE
                    binding.serveButton.isEnabled = false
                    binding.endMatch.visibility = View.VISIBLE
                    viewModel.changeZones(binding.root.rootView, state)
                    binding.opponentPoint.visibility = View.GONE
                    viewModel.changeServeStartSet()
                }
                else -> {
                    binding.startSet.isEnabled = true
                    binding.opponentError.isEnabled = true
                    binding.endMatch.visibility = View.GONE
                    binding.opponentPoint.visibility = View.GONE
                }
            }
            binding.root.requestLayout()
        }

        viewModel.serve.observe(viewLifecycleOwner){
            serve -> binding.serveButton.text = if (serve) getString(R.string.serve) else getString(R.string.reception)
        }

        viewModel.pageTitle.observe(viewLifecycleOwner){
            title -> (requireActivity() as AppCompatActivity).supportActionBar?.title = title
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
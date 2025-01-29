package com.reisiegel.volleyballhelper.ui.matchchooser

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
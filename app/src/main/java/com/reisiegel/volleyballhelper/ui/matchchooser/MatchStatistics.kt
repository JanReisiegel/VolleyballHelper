package com.reisiegel.volleyballhelper.ui.matchchooser

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reisiegel.volleyballhelper.databinding.FragmentMatchStatisticsBinding
import com.reisiegel.volleyballhelper.models.SelectedTournament

class MatchStatistics : Fragment() {

    private val viewModel: MatchStatisticsViewModel by viewModels()
    private var _binding: FragmentMatchStatisticsBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMatchStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val matchListLayout = binding.matchList
        val statisticsLayout = binding.matchStatistics
        if (SelectedTournament.selectedMatchIndex != null){
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
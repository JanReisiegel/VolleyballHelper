package com.reisiegel.volleyballhelper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.reisiegel.volleyballhelper.databinding.FragmentHomeBinding
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val statisticsDirectory = File(context?.filesDir, "Statistics")
        if (!statisticsDirectory.isDirectory)
            statisticsDirectory.mkdir()
        var statisticFiles = statisticsDirectory.listFiles()

        var filesAdapter = mutableMapOf<String, String>()
        var listFileNames = ArrayList<String>()

        statisticFiles.forEach { file: File ->
            filesAdapter[file.name] = file.path
            listFileNames.add(file.name)
        }

        var filesView: Spinner = binding.spinner
        var adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listFileNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filesView.adapter = adapter

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
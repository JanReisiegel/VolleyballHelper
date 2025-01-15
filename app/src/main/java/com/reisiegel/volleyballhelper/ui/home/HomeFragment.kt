package com.reisiegel.volleyballhelper.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.reisiegel.volleyballhelper.R
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
        val statisticFiles = statisticsDirectory.listFiles()

        val filesAdapter = mutableMapOf<String, String>()
        val listFileNames = ArrayList<String>()

        statisticFiles?.forEach { file: File ->
            filesAdapter[file.name] = file.path
            listFileNames.add(file.name)
        }

        val filesView: Spinner = binding.spinner
        var adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            listFileNames
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            filesView.adapter = adapter
        }




        /*val button: Button = binding.button
        button.setOnClickListener {
            findNavController().navigate(R.id.redirect_to_create_fragment)
        }*/

        redirectButton(binding.button, R.id.redirect_to_create_fragment)

        return root
    }

    private fun redirectButton(button: Button, destination: Int) {
        button.setOnClickListener {
            findNavController().navigate(destination)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.reisiegel.volleyballhelper.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentHomeBinding
import com.reisiegel.volleyballhelper.models.SelectedTournament
import com.reisiegel.volleyballhelper.models.Tournament
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
            val fileName = file.name.subSequence(0,file.name.length-5).toString()
            filesAdapter[fileName] = file.path
            listFileNames.add(fileName)
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

        filesView.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                val selectedPath = filesAdapter[selectedItem]
                SelectedTournament.filePath = selectedPath.toString()
                //println("Selected item: $selectedItem")
                Log.d("Home", "Selected item: $selectedItem")
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //println("Nothing selected")
                Log.d("Home", "Nothing selected")
            }
        }

        redirectButton(binding.createButton, R.id.redirect_to_create_fragment)

        binding.editButton.setOnClickListener {
            if(SelectedTournament.filePath.isNullOrEmpty()){
                val dialog = AlertDialog.Builder(context ?: return@setOnClickListener)
                    .setTitle(getString(R.string.error_header))
                    .setMessage(getString(R.string.file_not_selected))
                    .setPositiveButton(getString(R.string.OK)){
                        dialog, _ -> dialog.dismiss()
                    }
                    .create()
                dialog.show()
            }
            else
                findNavController().navigate(R.id.nav_match_statistics)
        }



        return root
    }



    private fun redirectButton(button: Button, destination: Int) {
        button.setOnClickListener {
            findNavController().navigate(destination)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.select_tournament)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
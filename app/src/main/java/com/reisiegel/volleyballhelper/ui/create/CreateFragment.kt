package com.reisiegel.volleyballhelper.ui.create

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentCreateBinding
import java.util.Calendar

class CreateFragment : Fragment() {
    private var _binding: FragmentCreateBinding? = null
    private lateinit var viewModel: CreateViewModel
    private lateinit var recyclerPlayersView: RecyclerView
    private lateinit var jerseyNumber: EditText
    private lateinit var playerName: EditText
    private lateinit var addPlayerButton: Button
    private lateinit var matchTimeButton: Button
    private lateinit var opponentName: EditText
    private lateinit var addMatchButton: Button
    private lateinit var recyclerMatchesView: RecyclerView
    private lateinit var tournamentName: EditText
    private lateinit var createTournamentButton: Button


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel =
            ViewModelProvider(this)[CreateViewModel::class.java]
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        val root: View = binding.root
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Vytvořit turnaj"
        return root
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerPlayersView = _binding?.playersView ?: return
        jerseyNumber = _binding?.newJerseyNumber ?: return
        playerName = _binding?.newPlayerName ?: return
        addPlayerButton = _binding?.addPlayerButton ?: return
        matchTimeButton = _binding?.addMatchTimeButton ?: return
        opponentName = _binding?.opponentName ?: return
        addMatchButton = _binding?.addMatchButton ?: return
        recyclerMatchesView = _binding?.matchesView ?: return
        tournamentName = _binding?.tournamentName ?: return
        createTournamentButton = _binding?.createTournamentButton ?: return

        recyclerPlayersView.layoutManager = LinearLayoutManager(requireContext())
        val playerAdapter = PlayerAdapter(emptyList<PlayerItem>().toMutableList())
        recyclerPlayersView.adapter = playerAdapter

        recyclerMatchesView.layoutManager = LinearLayoutManager(requireContext())
        val matchAdapter = MatchAdapter(emptyList<MatchItem>().toMutableList())/*{
            viewModel.removeMatch(it)
        }*/
        recyclerMatchesView.adapter = matchAdapter

        viewModel.players.observe(viewLifecycleOwner){
            players -> playerAdapter.updateItems(players)
                playerAdapter.notifyDataSetChanged()
        }

        viewModel.matches.observe(viewLifecycleOwner){
            matches -> matchAdapter.updateItems(matches)
                matchAdapter.notifyDataSetChanged()
        }

        matchTimeButton.setOnClickListener {
            showDatePicker()
        }
        viewModel.startMatchTime.observe(viewLifecycleOwner){
            time -> matchTimeButton.text = time
        }

        viewModel.tournamentName.observe(viewLifecycleOwner){
            name -> tournamentName.setText(name)
        }


        addPlayerButton.setOnClickListener {
            val player = PlayerItem(jerseyNumber.text.toString(), playerName.text.toString())
            try {
                if (player.getJersey() > 0 && player.getName() != ""){
                    viewModel.addPlayer(player)
                    jerseyNumber.text.clear()
                    playerName.text.clear()
                }
                else{
                    if(player.getJersey() == 0)
                        jerseyNumber.error = "Číslo musí být větší než 0"
                    if (player.getName() == "")
                        playerName.error = "Jméno hráče musí být vyplněno"
                }
            }
            catch (e: NumberFormatException){
                jerseyNumber.error = "Číslo musí být vyplněno a větší než 0"
            }
            viewModel.sortPlayers()
        }

        addMatchButton.setOnClickListener {
            val match = MatchItem(opponentName.text.toString(), viewModel.startMatchTime.value.toString())
            if (match.getOpponent() != "" && match.getStartTime() != ""){
                viewModel.addMatch(match)
                opponentName.text.clear()
                viewModel.updateStartMatchTime("")
                matchTimeButton.text = "Začátek zápasu"
            }
            else {
                opponentName.error = "Jméno soupeře a čas začátku utkání musí být vyplněno"
            }

            viewModel.sortMatches()
        }

        createTournamentButton.setOnClickListener {
            viewModel.updateActionName(tournamentName.text.toString())
            if (viewModel.tournamentName.value.isNullOrEmpty())
                tournamentName.error = "Název turnaje nesmí být prázdný"
            if (viewModel.players.value.isNullOrEmpty())
                playerName.error = "Seznam hráčů nesmí být prázdný"
            if (viewModel.matches.value.isNullOrEmpty())
                opponentName.error = "Seznam zápasů nesmí být prázdný"
            if (viewModel.tournamentName.value != "" && viewModel.players.value?.isNotEmpty() == true && viewModel.matches.value?.isNotEmpty() == true) {
                viewModel.createTournament(requireContext())
                findNavController().navigate(R.id.redirect_to_home_fragment)
            }

        }

    }

    @SuppressLint("DefaultLocale")
    private fun showDatePicker(){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            {
                _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = String.format("%02d. %02d. %d", selectedDay, selectedMonth+1, selectedYear)
                    showTimePicker(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
    @SuppressLint("DefaultLocale")
    private fun showTimePicker(date: String){
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                viewModel.updateStartMatchTime("$date $formattedTime")
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
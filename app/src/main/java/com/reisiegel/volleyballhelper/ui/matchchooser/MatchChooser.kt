package com.reisiegel.volleyballhelper.ui.matchchooser

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reisiegel.volleyballhelper.R
import com.reisiegel.volleyballhelper.databinding.FragmentMatchChooserBinding

class MatchChooser : Fragment() {

    private val viewModel: MatchChooserViewModel by viewModels()
    private val _binding: FragmentMatchChooserBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_match_chooser, container, false)
    }
}
package com.reisiegel.volleyballhelper.ui.create

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reisiegel.volleyballhelper.R

class CreateMatchFragment : Fragment() {

    companion object {
        fun newInstance() = CreateMatchFragment()
    }

    private val viewModel: CreateMatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_create_match, container, false)
    }
}
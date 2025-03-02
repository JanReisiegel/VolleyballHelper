package com.reisiegel.volleyballhelper.ui.export

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.reisiegel.volleyballhelper.R

class ExportStatistics : Fragment() {

    companion object {
        fun newInstance() = ExportStatistics()
    }

    private val viewModel: ExportStatisticsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_export_statistics, container, false)
    }
}
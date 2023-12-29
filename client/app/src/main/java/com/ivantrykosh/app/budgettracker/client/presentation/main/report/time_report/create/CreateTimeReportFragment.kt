package com.ivantrykosh.app.budgettracker.client.presentation.main.report.time_report.create

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentCreatedTimeReportBinding
import com.ivantrykosh.app.budgettracker.client.presentation.main.report.time_report.TimeReportViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Create time report fragment
 */
@AndroidEntryPoint
class CreateTimeReportFragment : Fragment(), OnChartValueSelectedListener {
    private var _binding: FragmentCreatedTimeReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TimeReportViewModel by activityViewModels()
    private var toast: Toast? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreatedTimeReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.createdTimeReportTopAppBar.setOnClickListener {
            findNavController().navigate(R.id.action_createdTimeReportFragment_to_timeReportFragment)
        }

        showReport()
    }

    private fun showReport() {
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(android.R.attr.colorAccent, typedValue, true)
        val color = Color.valueOf(typedValue.data)

        val lineData = viewModel.getLineData()
        lineData.setValueTextSize(16f)
        lineData.setDrawValues(false)
        lineData.setValueTextColor(color.toArgb())
        binding.createdTimeReportLineChart.data = lineData
        binding.createdTimeReportLineChart.description.isEnabled = false
        binding.createdTimeReportLineChart.xAxis.setDrawLabels(false)
        binding.createdTimeReportLineChart.axisLeft.textSize = 16f
        binding.createdTimeReportLineChart.axisRight.textSize = 16f
        binding.createdTimeReportLineChart.axisLeft.textColor = color.toArgb()
        binding.createdTimeReportLineChart.axisRight.textColor = color.toArgb()
        binding.createdTimeReportLineChart.legend.isEnabled = false
        binding.createdTimeReportLineChart.setOnChartValueSelectedListener(this)

        binding.createdTimeReportLineChart.axisLeft.axisMinimum = -viewModel.maxTimeValue
        binding.createdTimeReportLineChart.axisLeft.axisMaximum = viewModel.maxTimeValue
        binding.createdTimeReportLineChart.axisRight.axisMinimum = -viewModel.maxTimeValue
        binding.createdTimeReportLineChart.axisRight.axisMaximum = viewModel.maxTimeValue
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onValueSelected(e: Entry?, h: Highlight?) {
        if (e != null) {
            val selectedDate = viewModel.labels.elementAtOrNull(e.x.toInt()) ?: ""
            val selectedValue = e.y
            toast?.cancel()
            toast = Toast.makeText(
                requireContext(),
                "Selected Date: $selectedDate, Value: $selectedValue",
                Toast.LENGTH_SHORT
            )
            toast?.show()
        }
    }

    override fun onNothingSelected() {
    }
}
package com.ivantrykosh.app.budgettracker.client.presentation.main.report.pdf_report

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.google.android.material.datepicker.MaterialDatePicker
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentPdfReportBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

/**
 * Pdf report fragment
 */
@AndroidEntryPoint
class PdfReportFragment : Fragment() {
    private var _binding: FragmentPdfReportBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PdfReportViewModel by viewModels()

    private val datePicker =
        MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select date")
            .setSelection(
                Pair(
                    MaterialDatePicker.thisMonthInUtcMilliseconds(),
                    MaterialDatePicker.todayInUtcMilliseconds()
                )
            )
            .build()

    private var isAllAccounts = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPdfReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAccounts()

        binding.root.setOnRefreshListener {
            loadAccounts()
        }

        binding.pdfReportTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        viewModel.dateRange.observe(requireActivity()) {
            binding.pdfReportInputDatesText.setText(it)
        }

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (binding.pdfReportInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        binding.pdfReportInputTransactionTypeText.text = null
        binding.pdfReportInputTransactionTypeText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.pdfReportInputTransactionTypeText.text.isBlank()) {
                    binding.pdfReportInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
                } else {
                    binding.pdfReportInputTransactionType.error = null
                }
            }
        }

        binding.pdfReportInputAccountText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.pdfReportInputAccountText.text.isBlank()) {
                    binding.pdfReportInputAccount.error = resources.getString(R.string.invalid_account)
                } else {
                    binding.pdfReportInputAccount.error = null
                }
            }
        }
        binding.pdfReportInputAccountText.setOnItemClickListener { _, _, position, _ ->
            isAllAccounts = position == 0
        }

        val periods = resources.getStringArray(R.array.period_types)
        val adapterPeriods = ArrayAdapter(requireContext(), R.layout.list_item_name_item, periods)
        (binding.pdfReportInputPeriod.editText as? AutoCompleteTextView)?.setAdapter(adapterPeriods)
        binding.pdfReportInputPeriodText.setText(periods[0], false)
        viewModel.setPeriod(PdfReportViewModel.Period.DAY)
        binding.pdfReportInputPeriodText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.pdfReportInputPeriodText.text.isBlank()) {
                    binding.pdfReportInputPeriod.error = resources.getString(R.string.invalid_period)
                } else {
                    binding.pdfReportInputPeriod.error = null
                }
            }
        }
        binding.pdfReportInputPeriodText.setOnItemClickListener { parent, _, position, _ ->
            viewModel.setPeriod(
                when (parent.getItemAtPosition(position).toString()) {
                    "Days" -> PdfReportViewModel.Period.DAY
                    "Weeks" -> PdfReportViewModel.Period.WEEK
                    "Months" -> PdfReportViewModel.Period.MONTH
                    else -> PdfReportViewModel.Period.YEAR
                }
            )
        }

        binding.pdfReportInputDatesText.keyListener = null
        binding.pdfReportInputDatesText.setOnFocusChangeListener { _, isFocus ->
            if (isFocus) {
                if (!datePicker.isAdded) {
                    datePicker.show(parentFragmentManager, "datePicker")
                }
            } else {
                if (binding.pdfReportInputDatesText.text?.isBlank() != false) {
                    binding.pdfReportInputDates.error = resources.getString(R.string.invalid_dates)
                } else {
                    binding.pdfReportInputDates.error = null
                }
            }
        }
        binding.pdfReportInputDatesText.setOnClickListener {
            if (!datePicker.isAdded) {
                datePicker.show(parentFragmentManager, "datePicker")
            }
        }
        datePicker.addOnPositiveButtonClickListener {
            viewModel.updateDateRange(Date(it.first), Date(it.second))
        }

        binding.pdfReportButtonShowReport.setOnClickListener {
            createReport()
        }

        binding.pdfReportError.errorOk.setOnClickListener {
            binding.pdfReportError.root.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        val types = resources.getStringArray(R.array.transaction_types)
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, types)
        (binding.pdfReportInputTransactionType.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        val periods = resources.getStringArray(R.array.period_types)
        val adapterPeriods = ArrayAdapter(requireContext(), R.layout.list_item_name_item, periods)
        (binding.pdfReportInputPeriod.editText as? AutoCompleteTextView)?.setAdapter(adapterPeriods)
        binding.pdfReportInputPeriodText.setText(periods[0], false)
        viewModel.setPeriod(PdfReportViewModel.Period.DAY)

        loadAccounts()
    }

    private fun loadAccounts() {
        binding.pdfReportError.root.visibility = View.GONE
        binding.root.isRefreshing = true

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.getAccounts()
        viewModel.isLoadingGetAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false
                if (viewModel.getAccountsState.value.error.isBlank()) {
                    if (viewModel.getAccountsState.value.accounts.isEmpty()) {
                        binding.pdfReportError.root.visibility = View.VISIBLE
                        binding.pdfReportError.errorTitle.text = resources.getString(R.string.error)
                        binding.pdfReportError.errorText.text = resources.getString(R.string.no_accounts)
                    } else {
                        setAccounts()
                    }
                } else {
                    if (viewModel.getAccountsState.value.error.startsWith("403") || viewModel.getAccountsState.value.error.startsWith("401") || viewModel.getAccountsState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.getAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.pdfReportError.root.visibility = View.VISIBLE
                        binding.pdfReportError.errorTitle.text = resources.getString(R.string.error)
                        binding.pdfReportError.errorText.text = viewModel.getAccountsState.value.error
                    } else {
                        binding.pdfReportError.root.visibility = View.VISIBLE
                        binding.pdfReportError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.pdfReportError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingGetAccounts.removeObservers(requireActivity())
            }
        }
    }

    private fun setAccounts() {
        val items: MutableList<String> = viewModel.getAccountsState.value.accounts.map { it.name }.toMutableList()
        items.add(0, resources.getString(R.string.select_all))
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_name_item, items)
        (binding.pdfReportInputAccount.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.pdfReportInputAccountText.text = null
    }

    private fun createReport() {
        binding.pdfReportError.root.visibility = View.GONE
        binding.pdfReportInputTransactionTypeText.clearFocus()
        binding.pdfReportInputAccountText.clearFocus()
        binding.pdfReportInputPeriodText.clearFocus()
        binding.pdfReportInputDatesText.clearFocus()

        if (binding.pdfReportInputTransactionTypeText.text.isBlank()) {
            binding.pdfReportInputTransactionType.error = resources.getString(R.string.invalid_transaction_type)
        } else if (binding.pdfReportInputAccountText.text.isBlank()) {
            binding.pdfReportInputAccount.error = resources.getString(R.string.invalid_account)
        } else if (binding.pdfReportInputPeriodText.text.isBlank()) {
            binding.pdfReportInputPeriod.error = resources.getString(R.string.invalid_period)
        } else if (binding.pdfReportInputDatesText.text?.isBlank() == true) {
            binding.pdfReportInputDates.error = resources.getString(R.string.invalid_dates)
        } else {
            binding.root.isRefreshing = true

            requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

            val accountIds: List<Long> = when (isAllAccounts) {
                true -> viewModel.getAccountsState.value.accounts.map { it.accountId }
                false -> listOf(viewModel.getAccountIdByName(binding.pdfReportInputAccountText.text.toString()) ?: -1)
            }
            viewModel.getTransactions(accountIds, binding.pdfReportInputTransactionTypeText.text.toString())
            viewModel.isLoadingGetTransactions.observe(requireActivity()) { isLoading  ->
                if (!isLoading) {
                    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    binding.root.isRefreshing = false

                    if (viewModel.getTransactionsState.value.error.isBlank()) {
                        if (viewModel.getTransactionsState.value.transactions.isEmpty()) {
                            Toast.makeText(requireContext(), resources.getString(R.string.no_transactions), Toast.LENGTH_SHORT).show()
                        } else {
                            inflateLayout()
                        }
                    } else {
                        if (viewModel.getTransactionsState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.getTransactionsState.value.error.startsWith("401") || viewModel.getTransactionsState.value.error.contains("JWT", ignoreCase = true)) {
                            startAuthActivity()
                        } else if (viewModel.getTransactionsState.value.error.contains("HTTP", ignoreCase = true)) {
                            binding.pdfReportError.root.visibility = View.VISIBLE
                            binding.pdfReportError.errorTitle.text = resources.getString(R.string.error)
                            binding.pdfReportError.errorText.text = viewModel.getTransactionsState.value.error
                        } else {
                            binding.pdfReportError.root.visibility = View.VISIBLE
                            binding.pdfReportError.errorTitle.text = resources.getString(R.string.network_error)
                            binding.pdfReportError.errorText.text = resources.getString(R.string.connection_failed_message)
                        }
                    }
                    viewModel.isLoadingGetTransactions.removeObservers(requireActivity())
                }
            }
        }
    }

    private fun startAuthActivity() {
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        requireActivity().startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.dateRange.removeObservers(requireActivity())
        _binding = null
    }

    private fun inflateLayout() {
        val width = 950
        val height = 1375

        val pdfView = LayoutInflater.from(requireContext()).inflate(R.layout.pdf_layout, null)
        pdfView.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY))
        pdfView.layout(0, 0, width, height)

        val textViewName = pdfView.rootView.findViewById<TextView>(R.id.pdf_layout_pdf_name)
        val string = viewModel.dateRange.value
        textViewName.text = getString(R.string.report_for, string)

        val textViewBarTitle = pdfView.rootView.findViewById<TextView>(R.id.pdf_layout_bar_chart_title)
        textViewBarTitle.text = getString(R.string.transaction_type_by_categories, binding.pdfReportInputTransactionTypeText.text)

        val textViewLineTitle = pdfView.rootView.findViewById<TextView>(R.id.pdf_layout_line_chart_title)
        textViewLineTitle.text = getString(R.string.transaction_type_by_date, binding.pdfReportInputTransactionTypeText.text)

        val barData = viewModel.getBarDataByCategory()
        barData.setValueTextSize(4f)
        val barChart = pdfView.rootView.findViewById<BarChart>(R.id.pdf_layout_category_report_barchart)
        barChart.data = barData
        barChart.extraLeftOffset = 70f
        barChart.description.isEnabled = false
        barChart.xAxis.setDrawLabels(false)
        barChart.xAxis.setDrawGridLines(false)
        barChart.axisRight.textSize = 4f
        barChart.axisLeft.isEnabled = false
        barChart.axisLeft.axisMinimum = -viewModel.maxCategoryValue
        barChart.axisLeft.axisMaximum = viewModel.maxCategoryValue
        barChart.axisRight.axisMinimum = -viewModel.maxCategoryValue
        barChart.axisRight.axisMaximum = viewModel.maxCategoryValue
        val barChartLegend = barChart.legend
        barChartLegend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        barChartLegend.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        barChartLegend.orientation = Legend.LegendOrientation.VERTICAL
        barChartLegend.form = Legend.LegendForm.SQUARE
        barChartLegend.setDrawInside(true)
        barChartLegend.isWordWrapEnabled = true
        barChartLegend.textSize = 4f


        val lineData = viewModel.getLineData()
        lineData.setValueTextSize(4f)
        lineData.setDrawValues(false)
        val lineChart = pdfView.rootView.findViewById<LineChart>(R.id.pdf_layout_time_report_line_chart)
        lineChart.data = lineData
        lineChart.description.isEnabled = false
        lineChart.xAxis.setDrawLabels(false)
        lineChart.axisLeft.textSize = 4f
        lineChart.axisRight.textSize = 4f
        lineChart.legend.isEnabled = false
        lineChart.axisLeft.axisMinimum = -viewModel.maxTimeValue
        lineChart.axisLeft.axisMaximum = viewModel.maxTimeValue
        lineChart.axisRight.axisMinimum = -viewModel.maxTimeValue
        lineChart.axisRight.axisMaximum = viewModel.maxTimeValue


        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        pdfView.draw(Canvas(bitmap))

        viewModel.PdfGenerator(requireContext()).generatePdf(bitmap)
    }
}
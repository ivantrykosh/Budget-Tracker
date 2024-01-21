package com.ivantrykosh.app.budgettracker.client.presentation.main.settings

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentSettingsBinding
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Settings fragment
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    private lateinit var timePicker: MaterialTimePicker

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDailyReminder(requireContext(), timePicker.hour, timePicker.minute)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.isEnabled = false

        timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(if (is24HourFormat(requireContext())) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H)
            .build()

        binding.settingsTopAppBar.setNavigationOnClickListener {
            (activity as MainActivity).openDrawer()
        }

        viewModel.currency.observe(requireActivity()) {
            binding.settingsChooseCurrencyInputText.setText(it, false)
        }

        viewModel.dateFormat.observe(requireActivity()) {
            binding.settingsChooseDateFormatInputText.setText(it, false)
        }

        viewModel.dailyReminderTime.observe(requireActivity()) {
            binding.settingsChooseRemainderInputText.setText(it)
        }

        val currencies = viewModel.getCurrencies()
        val adapterCurrency = ArrayAdapter(requireContext(), R.layout.list_item_name_item, currencies)
        (binding.settingsChooseCurrencyInput.editText as? AutoCompleteTextView)?.setAdapter(adapterCurrency)
        binding.settingsChooseCurrencyInputText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.settingsChooseCurrencyInputText.text.isBlank()) {
                    binding.settingsChooseCurrencyInput.error = resources.getString(R.string.invalid_currency)
                } else {
                    binding.settingsChooseCurrencyInput.error = null
                }
            }
        }
        binding.settingsChooseCurrencyInputText.setOnItemClickListener { parent, _, position, _ ->
            viewModel.setCurrency(
                parent.getItemAtPosition(position).toString()
            )
        }

        val dateFormats = viewModel.getDateFormats()
        val adapterDateFormat = ArrayAdapter(requireContext(), R.layout.list_item_name_item, dateFormats)
        (binding.settingsChooseDateFormatInput.editText as? AutoCompleteTextView)?.setAdapter(adapterDateFormat)
        binding.settingsChooseDateFormatInputText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                if (binding.settingsChooseDateFormatInputText.text.isBlank()) {
                    binding.settingsChooseDateFormatInput.error = resources.getString(R.string.invalid_date_format)
                } else {
                    binding.settingsChooseDateFormatInput.error = null
                }
            }
        }
        binding.settingsChooseDateFormatInputText.setOnItemClickListener { parent, _, position, _ ->
            viewModel.setDateFormat(
                parent.getItemAtPosition(position).toString()
            )
        }

        binding.settingsChooseRemainderInputText.keyListener = null
        binding.settingsChooseRemainderInputText.setOnFocusChangeListener { v, isFocus ->
            if (isFocus) {
                showMenu(v, R.menu.reminder_menu)
            }
        }
        binding.settingsChooseRemainderInputText.setOnClickListener {
            showMenu(it, R.menu.reminder_menu)
        }
        timePicker.addOnPositiveButtonClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setDailyReminder(requireContext(), timePicker.hour, timePicker.minute)
            }
        }

        binding.settingsDeleteDataMain.setOnClickListener {
            onDeleteData()
        }
        binding.settingsDeleteDataIcon.setOnClickListener {
            onDeleteData()
        }
        binding.settingsDeleteDataText.setOnClickListener {
            onDeleteData()
        }

        binding.settingsFeedbackMain.setOnClickListener {
            showFeedBackMessage()
        }
        binding.settingsFeedbackIcon.setOnClickListener {
            showFeedBackMessage()
        }
        binding.settingsFeedbackText.setOnClickListener {
            showFeedBackMessage()
        }
    }

    /**
     * Show context menu
     */
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.reminder_menu_set_time -> {
                    if (!timePicker.isAdded) {
                        timePicker.show(parentFragmentManager, "timePicker")
                    }
                    true
                }
                R.id.reminder_menu_delete_reminder -> {
                    viewModel.cancelDailyReminder(requireContext())
                    true
                }
                else -> true
            }
        }
        popup.setOnDismissListener { }
        popup.show()
    }

    /**
     * Show feedback message
     */
    private fun showFeedBackMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.support_and_feedback))
            .setMessage(resources.getString(R.string.support_and_feedback_message))
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> }
            .show()
    }

    /**
     * On delete data click
     */
    private fun onDeleteData() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.delete_data_question_title))
            .setMessage(resources.getString(R.string.delete_data_question_message))
            .setPositiveButton(resources.getString(R.string.yes)) { _, _ ->
                deleteData()
            }
            .setNegativeButton(resources.getString(R.string.no)) { _, _ -> }
            .show()
    }

    /**
     * Delete data
     */
    private fun deleteData() {
        binding.settingsError.root.visibility = View.GONE
        progressStart()

        viewModel.deleteAllAccounts()
        viewModel.deleteAllAccountsState.observe(requireActivity()) { deleteAllAccounts ->
            if (!deleteAllAccounts.isLoading) {
                progressEnd()

                when (deleteAllAccounts.error) {
                    null -> {
                        Toast.makeText(requireContext(), resources.getString(R.string.data_was_deleted), Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        showError(resources.getString(R.string.error), resources.getString(R.string.unexpected_error_occurred))
                    }
                }

                viewModel.deleteAllAccountsState.removeObservers(requireActivity())
            }
        }
    }

    /**
     * Show progress indicator and make screen not touchable
     */
    private fun progressStart() {
        binding.root.isRefreshing = true
        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    /**
     * Hide progress indicator and make screen touchable
     */
    private fun progressEnd() {
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        binding.root.isRefreshing = false
    }

    /**
     * Show error message
     *
     * @param title title of message
     * @param text text of message
     */
    private fun showError(title: String, text: String) {
        binding.settingsError.root.visibility = View.VISIBLE
        binding.settingsError.errorTitle.text = title
        binding.settingsError.errorText.text = text
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
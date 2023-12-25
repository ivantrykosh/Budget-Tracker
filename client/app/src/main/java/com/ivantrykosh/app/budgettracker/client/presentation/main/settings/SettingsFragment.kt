package com.ivantrykosh.app.budgettracker.client.presentation.main.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.ivantrykosh.app.budgettracker.client.R
import com.ivantrykosh.app.budgettracker.client.common.Constants
import com.ivantrykosh.app.budgettracker.client.databinding.FragmentSettingsBinding
import com.ivantrykosh.app.budgettracker.client.presentation.auth.AuthActivity
import com.ivantrykosh.app.budgettracker.client.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date

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

//    private fun showPermissionRequest() {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions()
//        }
//    }

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
            .setTitleText("Select time")
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
            // todo add this if to every date and time pickers
            if (isFocus) {
                showMenu(v, R.menu.reminder_menu)
            }
        }
        binding.settingsChooseRemainderInputText.setOnClickListener {
            // todo add this if to every date and time pickers
            showMenu(it, R.menu.reminder_menu)
        }
        timePicker.addOnPositiveButtonClickListener {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }

    private fun showFeedBackMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.support_and_feedback))
            .setMessage(resources.getString(R.string.support_and_feedback_message))
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ -> }
            .show()
    }

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

    private fun deleteData() {
        binding.root.isRefreshing = true
        binding.settingsError.root.visibility = View.GONE

        requireActivity().window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        viewModel.deleteAllAccounts()
        viewModel.isLoadingDeleteAllAccounts.observe(requireActivity()) { isLoading ->
            if (!isLoading) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                binding.root.isRefreshing = false

                if (viewModel.deleteAllAccountsState.value.error.isBlank()) {
                    Toast.makeText(requireContext(), resources.getString(R.string.data_was_deleted), Toast.LENGTH_LONG).show()
                } else {
                    if (viewModel.deleteAllAccountsState.value.error.contains("Email is not verified", ignoreCase = true) || viewModel.deleteAllAccountsState.value.error.startsWith("401") || viewModel.deleteAllAccountsState.value.error.contains("JWT", ignoreCase = true)) {
                        startAuthActivity()
                    } else if (viewModel.deleteAllAccountsState.value.error.contains("HTTP", ignoreCase = true)) {
                        binding.settingsError.root.visibility = View.VISIBLE
                        binding.settingsError.errorTitle.text = resources.getString(R.string.error)
                        binding.settingsError.errorText.text = viewModel.deleteAllAccountsState.value.error
                    } else {
                        binding.settingsError.root.visibility = View.VISIBLE
                        binding.settingsError.errorTitle.text = resources.getString(R.string.network_error)
                        binding.settingsError.errorText.text = resources.getString(R.string.connection_failed_message)
                    }
                }
                viewModel.isLoadingDeleteAllAccounts.removeObservers(requireActivity())
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
        _binding = null
    }
}
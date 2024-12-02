package com.example.pennywise.ui.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.R
import com.example.pennywise.databinding.FragmentPastTransactionsBinding
import com.example.pennywise.ui.adapter.TransactionAdapter
import com.example.pennywise.ui.viewmodel.HomePageViewModel
import com.example.pennywise.ui.viewmodel.HomePageViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PastTransactionsFragment : Fragment() {

    private var _binding: FragmentPastTransactionsBinding? = null
    private val binding get() = _binding!!

    private val homePageViewModel: HomePageViewModel by viewModels {
        HomePageViewModelFactory(requireContext())
    }

    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPastTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        transactionAdapter = TransactionAdapter(emptyList(), emptyMap())
        binding.rvPastTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnFilterPastTransactions.setOnClickListener {
            showFilterDialog()
        }


        observeViewModel()

        homePageViewModel.fetchPastTransactions()
        homePageViewModel.fetchCategoriesForMapping()
    }

    private fun observeViewModel() {
        homePageViewModel.pastTransactions.observe(viewLifecycleOwner) { transactions ->
            val categoriesMap = homePageViewModel.categories.value?.associateBy { it.id }.orEmpty()
            transactionAdapter.updateTransactions(transactions, categoriesMap)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun filterTransactionsBy(filterType: String) {
        when (filterType) {
            "Day" -> {
                val today = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(Date())
                homePageViewModel.fetchFilteredTransactions(today, null, null)
            }
            "Week" -> {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                val startOfWeek = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(calendar.time)

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val endOfWeek = SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(calendar.time)

                homePageViewModel.fetchFilteredTransactions(null, startOfWeek, endOfWeek)
            }
            "Month" -> {
                val currentMonth = SimpleDateFormat("M/yyyy", Locale.getDefault()).format(Date())
                homePageViewModel.fetchFilteredTransactions(null, currentMonth, null)
            }
        }
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.filter_dialog, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rgFilterOptions)
        val selectDateButton = dialogView.findViewById<Button>(R.id.btnSelectDate)
        val applyFilterButton = dialogView.findViewById<Button>(R.id.btnApplyFilter)

        var selectedDate: String? = null
        var selectedStartOfWeekOrMonth: String? = null
        var selectedEndOfWeek: String? = null

        // Date picker logic
        selectDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                val date = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                selectedDate = date
                selectedStartOfWeekOrMonth = "$month/$year"
                selectedEndOfWeek = calculateEndOfWeek(dayOfMonth, month, year) // Helper for week range
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Create the dialog here
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Apply filter
        applyFilterButton.setOnClickListener {
            val selectedOption = when (radioGroup.checkedRadioButtonId) {
                R.id.rbDay -> selectedDate
                R.id.rbWeek -> selectedStartOfWeekOrMonth to selectedEndOfWeek
                R.id.rbMonth -> selectedStartOfWeekOrMonth
                else -> null
            }

            when (selectedOption) {
                is String -> filterTransactionsByDay(selectedOption)
                is Pair<*, *> -> filterTransactionsByWeek(selectedOption.first as String, selectedOption.second as String)
                is String? -> selectedOption?.let { it1 -> filterTransactionsByMonth(it1) }
            }

            dialog.dismiss() // Now dialog is properly defined and can be dismissed
        }

        dialog.show() // Show the dialog
    }


    // Helper function to calculate end of the week (example only, logic may vary)
    private fun calculateEndOfWeek(day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, 6) // Assuming a 7-day week
        val endDay = calendar.get(Calendar.DAY_OF_MONTH)
        val endMonth = calendar.get(Calendar.MONTH) + 1
        val endYear = calendar.get(Calendar.YEAR)
        return String.format("%02d/%02d/%04d", endDay, endMonth, endYear)
    }

    private fun filterTransactionsByDay(day: String) {
        homePageViewModel.fetchFilteredTransactionsByDay(day)
    }

    private fun filterTransactionsByWeek(startOfWeek: String, endOfWeek: String) {
        homePageViewModel.fetchFilteredTransactionsByWeek(startOfWeek, endOfWeek)
    }

    private fun filterTransactionsByMonth(month: String) {
        homePageViewModel.fetchFilteredTransactionsByMonth(month)
    }

}

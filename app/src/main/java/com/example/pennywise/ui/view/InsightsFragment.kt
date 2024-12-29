package com.example.pennywise.ui.view

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.compose.material3.DatePickerDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.databinding.FragmentInsightsBinding
import com.example.pennywise.ui.adapter.CategorySpendingAdapter
import com.example.pennywise.ui.viewmodel.InsightsViewModel
import com.example.pennywise.ui.viewmodel.InsightsViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: InsightsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        binding.topCategoriesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val repository = PennyWiseRepository.getInstance(requireContext())
        viewModel = ViewModelProvider(
            this,
            InsightsViewModelFactory(repository)
        ).get(InsightsViewModel::class.java)

        val loggedInUserId =
            FirebaseAuth.getInstance().currentUser?.uid // Replace with actual user ID logic

        if (loggedInUserId != null) {
            viewModel.calculateFinancialHealthScore(loggedInUserId)
//            viewModel.fetchTopSpendingCategories(loggedInUserId)
            setupFilter(loggedInUserId)
        }

        viewModel.financialHealthScore.observe(viewLifecycleOwner) { score ->
            binding.scoreTextView.text = score.toString()
            val range = when {
                score >= 80 -> "Excellent"
                score >= 60 -> "Good"
                score >= 40 -> "Average"
                score >= 20 -> "Poor"
                else -> "Critical"
            }
            binding.scoreRangeTextView.text = range
        }

        viewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            binding.incomeTextView.text = "Income: $${String.format("%.2f", income)}"
        }

        viewModel.totalExpense.observe(viewLifecycleOwner) { expense ->
            binding.expenseTextView.text = "Expense: $${String.format("%.2f", expense)}"
        }

        // Observe spending by category and update RecyclerView
        viewModel.spendingByCategory.observe(viewLifecycleOwner) { spendingList ->
            val adapter = CategorySpendingAdapter(spendingList)
            binding.topCategoriesRecyclerView.adapter = adapter
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            // Optionally display error message
        }

        return binding.root
    }


    private fun setupFilter(userId: String) {
        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val filterType = parent.getItemAtPosition(position).toString()
                binding.selectDateButton.setOnClickListener {
                    showDatePicker(filterType, userId)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun showDatePicker(filterType: String, userId: String) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                when (filterType) {
                    "Day" -> {
                        val formattedDate = dateFormat.format(selectedDate.time)
                        viewModel.fetchTopSpendingCategories(userId, "Day", formattedDate)
                    }
                    "Week" -> {
                        // Fix: Set the calendar to the start of the selected week
                        selectedDate.set(Calendar.DAY_OF_WEEK, selectedDate.firstDayOfWeek)
                        val startOfWeek = dateFormat.format(selectedDate.time)

                        // Fix: Calculate the end of the selected week
                        val endOfWeekCalendar = selectedDate.clone() as Calendar
                        endOfWeekCalendar.add(Calendar.DAY_OF_WEEK, 6)
                        val endOfWeek = dateFormat.format(endOfWeekCalendar.time)

                        // Pass start and end of the week
                        viewModel.fetchTopSpendingCategories(userId, "Week", startOfWeek)
                    }
                    "Month" -> {
                        selectedDate.set(Calendar.DAY_OF_MONTH, 1) // Start of the month
                        val startOfMonth = dateFormat.format(selectedDate.time)

                        selectedDate.set(Calendar.DAY_OF_MONTH, selectedDate.getActualMaximum(Calendar.DAY_OF_MONTH)) // End of the month
                        val endOfMonth = dateFormat.format(selectedDate.time)

                        // Pass start and end of the month
                        viewModel.fetchTopSpendingCategories(userId, "Month", startOfMonth)
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }


    // Get the filter value based on the filter type
    private fun getFilterValue(filterType: String): String? {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        return when (filterType) {
            "Day" -> dateFormat.format(calendar.time) // Today's date
            "Week" -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                dateFormat.format(calendar.time) // Start of the week
            }

            "Month" -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                dateFormat.format(calendar.time) // Start of the month
            }

            else -> null
        }
    }
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

}


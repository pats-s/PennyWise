package com.example.pennywise.ui.view

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
//import androidx.compose.material3.DatePickerDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.databinding.FragmentInsightsBinding
import com.example.pennywise.ui.adapter.CategorySpendingAdapter
import com.example.pennywise.ui.adapter.InsightsSavingGoalAdapter
import com.example.pennywise.ui.viewmodel.InsightsViewModel
import com.example.pennywise.ui.viewmodel.InsightsViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore

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
            fetchUserWalletId(loggedInUserId) { walletId ->
                viewModel.fetchSavingGoals(walletId)
            }
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

        viewModel.savingGoals.observe(viewLifecycleOwner) { savingGoals ->
            val adapter = InsightsSavingGoalAdapter(savingGoals)
            binding.rvSavingGoals.adapter = adapter
            binding.rvSavingGoals.layoutManager = LinearLayoutManager(requireContext())
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
                val formattedDate = dateFormat.format(selectedDate.time)


                val firestore = FirebaseFirestore.getInstance()
                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val walletId = document.getString("walletId") // Ensure this matches your Firestore schema
                        if (walletId != null) {
                            viewModel.fetchTopSpendingCategories(walletId, filterType, formattedDate)
                        }
                    }




            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }



    private fun calculateStartAndEndDatesForWeek(selectedDate: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Parse the provided date
        calendar.time = dateFormat.parse(selectedDate) ?: Date()

        // Set to the start of the week
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = dateFormat.format(calendar.time)

        // Set to the end of the week
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = dateFormat.format(calendar.time)

        return Pair(startOfWeek, endOfWeek)
    }

    private fun calculateStartAndEndDatesForMonth(selectedDate: String): Pair<String, String> {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Parse the provided date
        calendar.time = dateFormat.parse(selectedDate) ?: Date()

        // Set to the start of the month
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startOfMonth = dateFormat.format(calendar.time)

        // Set to the end of the month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endOfMonth = dateFormat.format(calendar.time)

        return Pair(startOfMonth, endOfMonth)
    }

    private fun fetchUserWalletId(userId: String, onWalletIdFetched: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val walletId = snapshot.getString("walletId")
                if (!walletId.isNullOrEmpty()) {
                    onWalletIdFetched(walletId)
                } else {
                    Log.e("InsightsFragment", "Wallet ID is null or empty")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("InsightsFragment", "Failed to fetch wallet ID: ${exception.message}")
            }
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


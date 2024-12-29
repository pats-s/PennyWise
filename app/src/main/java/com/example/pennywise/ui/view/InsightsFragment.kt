package com.example.pennywise.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.databinding.FragmentInsightsBinding
import com.example.pennywise.ui.adapter.CategorySpendingAdapter
import com.example.pennywise.ui.viewmodel.InsightsViewModel
import com.example.pennywise.ui.viewmodel.InsightsViewModelFactory
import com.google.firebase.auth.FirebaseAuth

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
            viewModel.fetchTopSpendingCategories(loggedInUserId)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


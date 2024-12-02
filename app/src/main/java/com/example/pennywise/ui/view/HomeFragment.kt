package com.example.pennywise.ui.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.R
import com.example.pennywise.databinding.FragmentHomepageBinding
import com.example.pennywise.remote.SavingGoal
import com.example.pennywise.ui.adapter.TransactionAdapter
import com.example.pennywise.ui.viewmodel.HomePageViewModel
import com.example.pennywise.ui.viewmodel.HomePageViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class HomeFragment : Fragment() {

    private var _binding: FragmentHomepageBinding? = null
    private val binding get() = _binding!!

    // Lazy initialization of ViewModel
    private val homePageViewModel: HomePageViewModel by viewModels {
        HomePageViewModelFactory(requireContext())
    }

    // Initialize TransactionAdapter
    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homePageViewModel.fetchCategoriesForMapping()
        // Initialize the RecyclerView
        setupRecyclerView()

        // Observe LiveData for welcome message, wallet balance, and transactions
        observeViewModel()

        // Add click listener for Add Transaction button
        binding.btnAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }

        binding.btnAddSavingGoal.setOnClickListener {
            showSavingGoalDialog()
        }


        homePageViewModel.fetchTodayTransactions()
        // Add click listener for View All button
        binding.btnViewAll.setOnClickListener {
            navigateToPastTransactions()
        }

        homePageViewModel.fetchExchangeRates()
        fun Double.format(): String = String.format(Locale.getDefault(), "%.2f", this)

        // Observe exchange rates and update UI
        homePageViewModel.exchangeRates.observe(viewLifecycleOwner) { rates ->
            val usdToLbp = rates["LBP"] ?: 0.0
            val usdToEur = rates["EUR"] ?: 0.0
            val eurToLbp = if (usdToEur > 0) usdToLbp / usdToEur else 0.0

            binding.tvUsdToLbp.text = "USD to LBP: ${usdToLbp.format()}"
            binding.tvUsdToEur.text = "USD to EUR: ${usdToEur.format()}"
            binding.tvEurToLbp.text = "EUR to LBP: ${eurToLbp.format()}"
        }

    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList(), emptyMap())
        binding.rvRecentTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {

        homePageViewModel.todayTransactions.observe(viewLifecycleOwner) { transactions ->
            val categoriesMap = homePageViewModel.categories.value?.associateBy { it.id }.orEmpty()
            transactionAdapter.updateTransactions(transactions, categoriesMap)
        }

        homePageViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.tvWelcome.text = "Welcome Back, $name!"
        }

        homePageViewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = "$${String.format("%.2f", balance)}"
        }

        homePageViewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoriesMap = categories.associateBy { it.id }
            val transactions = homePageViewModel.todayTransactions.value.orEmpty()
            transactionAdapter.updateTransactions(transactions, categoriesMap)
        }

        homePageViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun showAddTransactionDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null)

        val transactionTypeGroup = dialogView.findViewById<RadioGroup>(R.id.rgTransactionType)
        val amountInput = dialogView.findViewById<EditText>(R.id.etAmount)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.spCategory)
        val datePicker = dialogView.findViewById<TextView>(R.id.tvDate)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancel)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmit)

        // Set up categories spinner
        homePageViewModel.categories.observe(viewLifecycleOwner) { categories ->
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categories.map { it.name } // Display category names
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // Set up date picker
        datePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    datePicker.text = selectedDate // Update the TextView with the selected date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Restrict future dates
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        // Show the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Handle Cancel Button
        cancelButton.setOnClickListener { dialog.dismiss() }

        // Handle Submit Button
        submitButton.setOnClickListener {
            val selectedType = when (transactionTypeGroup.checkedRadioButtonId) {
                R.id.rbIncome -> "Income"
                R.id.rbExpense -> "Expense"
                else -> ""
            }

            val amount = amountInput.text.toString().toDoubleOrNull()
            val selectedCategoryPosition = categorySpinner.selectedItemPosition
            val date = datePicker.text.toString()

            if (selectedType.isNotEmpty() && amount != null && date.isNotEmpty()) {
                // Fetch categories from ViewModel and validate selectedCategoryPosition
                val categories = homePageViewModel.categories.value
                if (categories != null && selectedCategoryPosition in categories.indices) {
                    val selectedCategory = categories[selectedCategoryPosition]

                    // Submit transaction
                    homePageViewModel.addTransaction(
                        type = selectedType,
                        amount = amount,
                        categoryId = selectedCategory.id,
                        date = date
                    )
                    dialog.dismiss()

                    // Refresh today's transactions
                    homePageViewModel.fetchTodayTransactions()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select a valid category",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun showSavingGoalDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_saving_goal, null)

        val titleInput = dialogView.findViewById<EditText>(R.id.etSavingGoalTitle)
        val amountInput = dialogView.findViewById<EditText>(R.id.etSavingGoalAmount)
        val startDatePicker = dialogView.findViewById<TextView>(R.id.tvSavingGoalStartDate)
        val endDatePicker = dialogView.findViewById<TextView>(R.id.tvSavingGoalEndDate)
        val cancelButton = dialogView.findViewById<Button>(R.id.btnCancelSavingGoal)
        val submitButton = dialogView.findViewById<Button>(R.id.btnSubmitSavingGoal)

        val calendar = Calendar.getInstance()

        startDatePicker.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    startDatePicker.text = "$dayOfMonth/${month + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        endDatePicker.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    endDatePicker.text = "$dayOfMonth/${month + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        submitButton.setOnClickListener {
            val title = titleInput.text.toString()
            val targetAmount = amountInput.text.toString().toDoubleOrNull()
            val startDate = startDatePicker.text.toString()
            val endDate = endDatePicker.text.toString()

            if (title.isEmpty() || targetAmount == null || startDate.isEmpty() || endDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (targetAmount <= 0) {
                Toast.makeText(requireContext(), "Target amount must be greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (sdf.parse(endDate)?.before(sdf.parse(startDate)) == true) {
                Toast.makeText(requireContext(), "End date must be after start date", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savingGoal = SavingGoal(
                id = UUID.randomUUID().toString(),
                title = title,
                targetAmount = targetAmount,
                startDate = startDate,
                endDate = endDate,
                walletId = "wallet123" // Replace with logged-in user's wallet ID
            )

            homePageViewModel.addSavingGoal(savingGoal)
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun navigateToPastTransactions() {
        findNavController().navigate(R.id.action_homeFragment_to_pastTransactionsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

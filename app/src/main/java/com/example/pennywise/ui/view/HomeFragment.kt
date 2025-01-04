package com.example.pennywise.ui.view

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.ProfileActivity
import com.example.pennywise.R
import com.example.pennywise.databinding.FragmentHomepageBinding
import com.example.pennywise.remote.SavingGoal
import com.example.pennywise.ui.adapter.SavingGoalAdapter
import com.example.pennywise.ui.adapter.TransactionAdapter
import com.example.pennywise.ui.viewmodel.HomePageViewModel
import com.example.pennywise.ui.viewmodel.HomePageViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import com.example.pennywise.local.entities.AppSettingsEntity
import kotlinx.coroutines.launch
import androidx.room.Room
import androidx.lifecycle.lifecycleScope
import com.example.pennywise.PennyWiseDatabase
import com.example.pennywise.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomepageBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()


    // Lazy initialization of ViewModel
    private val homePageViewModel: HomePageViewModel by viewModels {
        HomePageViewModelFactory(requireContext())
    }

    // Initialize TransactionAdapter
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var savingGoalAdapter: SavingGoalAdapter

    private lateinit var currentUserWalletId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomepageBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n", "UseSwitchCompatOrMaterialCode")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Fetch wallet ID dynamically
        fetchUserWalletId { walletId ->
            currentUserWalletId = walletId
            setupRecyclerView()
            observeViewModel()
            loadInitialData()
        }

        homePageViewModel.fetchCategoriesForMapping()
        // Initialize the RecyclerView
        setupRecyclerView()

        // Observe LiveData for welcome message, wallet balance, and transactions
        fetchAndSetWelcomeMessage()

        observeViewModel()
        sharedViewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = "$${String.format("%.2f", balance)}"
        }


        // Add click listener for Add Transaction button
        binding.btnAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }

        binding.btnAddSavingGoal.setOnClickListener {
            showSavingGoalDialog()
        }


        //homePageViewModel.fetchTodayTransactions()
        homePageViewModel.fetchTodayUserTransactions()
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

        binding.btnGoToProfile.setOnClickListener{
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }




    }

    private fun fetchUserWalletId(onWalletIdFetched: (String) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val firestore = FirebaseFirestore.getInstance()
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val walletId = snapshot.getString("walletId")
                    if (!walletId.isNullOrEmpty()) {
                        onWalletIdFetched(walletId)
                    } else {
                        showToast("Failed to fetch wallet ID")
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to fetch user data: ${it.message}")
                }
        } else {
            showToast("No user is logged in.")
        }
    }

    private fun fetchAndSetWelcomeMessage() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val firestore = FirebaseFirestore.getInstance()

            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val firstName = snapshot.getString("firstname") ?: "User"
                        binding.tvWelcome.text = "Welcome Back, $firstName!"
                    } else {
                        binding.tvWelcome.text = "Welcome!"
                        Toast.makeText(requireContext(), "User not found in database.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    binding.tvWelcome.text = "Welcome!"
                    Toast.makeText(requireContext(), "Failed to fetch user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            binding.tvWelcome.text = "Welcome!"
            Toast.makeText(requireContext(), "No user is logged in.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(emptyList(), emptyMap())
        binding.rvRecentTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        savingGoalAdapter = SavingGoalAdapter(emptyList())
        binding.rvSavingGoals.apply {
            adapter = savingGoalAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {

        homePageViewModel.todayTransactions.observe(viewLifecycleOwner) { transactions ->
            val categoriesMap = homePageViewModel.categories.value?.associateBy { it.id }.orEmpty()
            transactionAdapter.updateTransactions(transactions, categoriesMap)
        }

//        homePageViewModel.userName.observe(viewLifecycleOwner) { name ->
//            binding.tvWelcome.text = "Welcome Back, $name!"
//        }

//        homePageViewModel.userName.observe(viewLifecycleOwner) { name ->
//            if (!name.isNullOrEmpty()) {
//                binding.tvWelcome.text = "Welcome Back, $name!"
//            } else {
//                binding.tvWelcome.text = "Welcome!"
//            }
//        }

        homePageViewModel.walletBalance.observe(viewLifecycleOwner) { balance ->
            binding.tvTotalBalance.text = "$${String.format("%.2f", balance)}"
        }

        homePageViewModel.savingGoals.observe(viewLifecycleOwner) { savingGoals ->
            savingGoalAdapter.updateSavingGoals(savingGoals)
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
                categories.map { it.name }
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
                    datePicker.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        cancelButton.setOnClickListener { dialog.dismiss() }

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
                val categories = homePageViewModel.categories.value
                if (categories != null && selectedCategoryPosition in categories.indices) {
                    val selectedCategory = categories[selectedCategoryPosition]

                    homePageViewModel.addTransaction(
                        type = selectedType,
                        amount = amount,
                        categoryId = selectedCategory.id,
                        date = date
                    )

                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Please select a valid category", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }


    private fun loadInitialData() {
        //homePageViewModel.fetchTodayTransactions()
        homePageViewModel.fetchTodayUserTransactions()
        homePageViewModel.fetchSavingGoals(currentUserWalletId)
        homePageViewModel.fetchCategoriesForMapping()
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

        var selectedStartDate: Calendar? = null
        var selectedEndDate: Calendar? = null

        startDatePicker.setOnClickListener {
            showDatePicker { calendar ->
                selectedStartDate = calendar
                startDatePicker.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            }
        }

        endDatePicker.setOnClickListener {
            showDatePicker { calendar ->
                selectedEndDate = calendar
                endDatePicker.text = "${calendar.get(Calendar.DAY_OF_MONTH)}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
            }
        }

        val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

        cancelButton.setOnClickListener { dialog.dismiss() }

        submitButton.setOnClickListener {
            val title = titleInput.text.toString()
            val targetAmount = amountInput.text.toString().toDoubleOrNull()
            val startDate = startDatePicker.text.toString()
            val endDate = endDatePicker.text.toString()

            // Validation
            if (title.isEmpty() || targetAmount == null || startDate.isEmpty() || endDate.isEmpty()) {
                showToast("Please fill in all fields")
                return@setOnClickListener
            }

            if (selectedStartDate != null && selectedEndDate != null && selectedEndDate!!.before(selectedStartDate)) {
                showToast("End date cannot be earlier than the start date")
                return@setOnClickListener
            }

            // Create saving goal
            val savingGoal = SavingGoal(
                id = UUID.randomUUID().toString(),
                title = title,
                targetAmount = targetAmount,
                startDate = startDate,
                endDate = endDate,
                walletId = currentUserWalletId
            )
            homePageViewModel.addSavingGoal(savingGoal)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker(onDateSelected: (Calendar) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }



    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToPastTransactions() {
        findNavController().navigate(R.id.action_homeFragment_to_pastTransactionsFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        homePageViewModel.fetchUserData()
    }

}

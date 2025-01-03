package com.example.pennywise.ui.view

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pennywise.PennyWiseRepository
import com.example.pennywise.R
import com.example.pennywise.databinding.FragmentBillsBinding
import com.example.pennywise.remote.Bill
import com.example.pennywise.ui.adapter.BillAdapter
import com.example.pennywise.ui.viewmodel.BillsViewModel
import com.example.pennywise.ui.viewmodel.BillsViewModelFactory
import com.example.pennywise.ui.viewmodel.HomePageViewModel
import com.example.pennywise.ui.viewmodel.HomePageViewModelFactory
import com.example.pennywise.ui.viewmodel.SharedViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.UUID

class BillsFragment : Fragment() {

    private var _binding: FragmentBillsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BillsViewModel
    private lateinit var billAdapter: BillAdapter
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBillsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = PennyWiseRepository.getInstance(requireContext())
        viewModel = ViewModelProvider(this, BillsViewModelFactory(repository))
            .get(BillsViewModel::class.java)




        // Initialize the adapter with the debug log
        billAdapter = BillAdapter(emptyList()) { bill ->
            println("Bill Wallet ID: ${bill.walletId}")
            viewModel.payBill(bill)
            repository.getWallet(bill.walletId, onSuccess = { wallet ->
                sharedViewModel.updateWalletBalance(wallet.balance)
                println("wallet balance (shared view model)" + wallet.balance)
            }, onFailure = { exception ->

            })


        }


        binding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnAddBill.setOnClickListener { showAddBillDialog() }

        viewModel.bills.observe(viewLifecycleOwner) { bills ->
            billAdapter.updateBills(bills)
        }

        // Fetch bills for the logged-in user's wallet
        getCurrentWalletId { walletId ->
            if (walletId.isNotEmpty()) {
                viewModel.fetchBills(walletId)
            } else {
                Toast.makeText(requireContext(), "Failed to fetch wallet ID", Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }



    }




    private fun showAddBillDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_bill, null)
        val billNameInput = dialogView.findViewById<EditText>(R.id.etBillName)
        val billAmountInput = dialogView.findViewById<EditText>(R.id.etBillAmount)
        val billDateInput = dialogView.findViewById<TextView>(R.id.tvBillDate)

        billDateInput.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                billDateInput.text = "$day/${month + 1}/$year"
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Add Bill")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = billNameInput.text.toString()
                val amount = billAmountInput.text.toString().toDoubleOrNull() ?: 0.0
                val date = billDateInput.text.toString()

                // Fetch walletId using the callback
                getCurrentWalletId { walletId ->
                    if (walletId.isNotEmpty()) {
                        val bill = Bill(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            amount = amount,
                            paymentDate = date,
                            walletId = walletId
                        )
                        viewModel.addBill(bill)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch wallet ID. Cannot add bill.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun getCurrentWalletId(onWalletIdFetched: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return onWalletIdFetched("")
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val walletId = documentSnapshot.getString("walletId") ?: ""
                onWalletIdFetched(walletId)
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                onWalletIdFetched("")
            }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


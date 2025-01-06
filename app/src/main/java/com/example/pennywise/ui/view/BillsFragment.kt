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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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




        billAdapter = BillAdapter(emptyList()) { bill ->
            println("Bill Wallet ID: ${bill.walletId}")

            // Deduct bill amount and update Firestore ( this is for the pay now button)
            viewModel.payBill(bill, onSuccess = {
                // we update the payment date after we pay
                repository.updateBillAfterPayment(bill, onSuccess = {
                    Toast.makeText(binding.root.context, "Bill marked as paid.", Toast.LENGTH_SHORT).show()
                }, onFailure = { exception ->
                    Toast.makeText(binding.root.context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                })

                repository.getWallet(bill.walletId, onSuccess = { wallet ->
                    sharedViewModel.updateWalletBalance(wallet.balance)
                    println("Updated wallet balance (shared view model): ${wallet.balance}")
                }, onFailure = { exception ->
                    println("Failed to fetch updated wallet: ${exception.message}")
                })
            }, onFailure = { exception ->
                println("Failed to pay bill: ${exception.message}")
            })
        }



        binding.rvBills.apply {
            adapter = billAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnAddBill.setOnClickListener { showAddBillDialog() }

        viewModel.bills.observe(viewLifecycleOwner) { bills ->
            val today = getTodayDate()

            bills.forEach { bill ->
                if(bill.paymentDate == today && !bill.paid){
                    repository.getWallet(bill.walletId, onSuccess = {wallet ->
                        if (wallet.balance >= bill.amount){
                            viewModel.deductBill(bill)
                            repository.updateBillAfterPayment(bill, onSuccess = {
                                Toast.makeText(requireContext(), "Bill '${bill.name}', paid automatically", Toast.LENGTH_SHORT).show()
                            },
                                onFailure = { exception ->
                                    Toast.makeText(requireContext(), "Error updating bill: ${exception.message}", Toast.LENGTH_SHORT).show()
                                })
                        }else{
                            Toast.makeText(requireContext(), "Insufficient balance for bill '${bill.name}'. Please pay manually later", Toast.LENGTH_SHORT).show()
                        }
                    }, onFailure = {exception ->
                        Toast.makeText(requireContext(), "Error fetching wallet: ${exception.message}", Toast.LENGTH_SHORT).show()
                    })
                }
            }
            billAdapter.updateBills(bills)
        }


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


    private fun getTodayDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
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
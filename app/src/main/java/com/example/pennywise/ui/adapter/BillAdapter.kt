package com.example.pennywise.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.databinding.ItemBillBinding
import com.example.pennywise.remote.Bill

class BillAdapter(
    private var bills: List<Bill>,
    private val onPayNowClicked: (Bill) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    class BillViewHolder(private val binding: ItemBillBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(bill: Bill, onPayNowClicked: (Bill) -> Unit) {
            binding.tvBillName.text = bill.name
            binding.tvBillAmount.text = "Amount: $${String.format("%.2f", bill.amount)}"
            binding.tvBillDate.text = "Payment Date: ${bill.paymentDate}"

            binding.btnDeduct.setOnClickListener {
                onPayNowClicked(bill)

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val binding = ItemBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(bills[position], onPayNowClicked)
    }

    override fun getItemCount(): Int = bills.size

    fun updateBills(newBills: List<Bill>) {
        bills = newBills
        notifyDataSetChanged()
    }
}
package com.example.pennywise.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.databinding.ItemTransactionBinding
import com.example.pennywise.remote.Transaction
import com.example.pennywise.remote.Category

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private var categories: Map<String, Category> // Map categoryId to Category object
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction, category: Category?) {
            binding.tvTransactionAmount.text = "$${String.format("%.2f", transaction.amount)}"
            binding.tvTransactionDate.text = transaction.date
            binding.tvTransactionCategory.text = category?.name ?: "Unknown Category"
            binding.tvTransactionType.text = transaction.type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val category = categories[transaction.categoryId] // Get the category object
        holder.bind(transaction, category)
    }

    override fun getItemCount(): Int = transactions.size

    fun updateTransactions(newTransactions: List<Transaction>, categories: Map<String, Category>) {
        this.transactions = newTransactions
        this.categories = categories
        notifyDataSetChanged() // Notify RecyclerView of data changes
    }

}

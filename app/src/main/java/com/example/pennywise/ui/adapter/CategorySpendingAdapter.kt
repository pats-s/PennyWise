package com.example.pennywise.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.databinding.ItemCategorySpendingBinding

class CategorySpendingAdapter(
    private val spendingList: List<Pair<String, Double>>
) : RecyclerView.Adapter<CategorySpendingAdapter.CategorySpendingViewHolder>() {

    inner class CategorySpendingViewHolder(private val binding: ItemCategorySpendingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(categorySpending: Pair<String, Double>) {
            binding.categoryNameTextView.text = categorySpending.first
            binding.spendingAmountTextView.text =
                "$${String.format("%.2f", categorySpending.second)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategorySpendingViewHolder {
        val binding = ItemCategorySpendingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategorySpendingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategorySpendingViewHolder, position: Int) {
        holder.bind(spendingList[position])
    }

    override fun getItemCount(): Int = spendingList.size
}

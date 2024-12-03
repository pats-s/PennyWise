package com.example.pennywise.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.databinding.ItemSavingGoalBinding
import com.example.pennywise.remote.SavingGoal

class SavingGoalAdapter(
    private var savingGoals: List<SavingGoal>
) : RecyclerView.Adapter<SavingGoalAdapter.SavingGoalViewHolder>() {

    class SavingGoalViewHolder(private val binding: ItemSavingGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(savingGoal: SavingGoal) {
            binding.tvSavingGoalTitle.text = savingGoal.title
            binding.tvStartDate.text = "Start Date: ${savingGoal.startDate}"
            binding.tvEndDate.text = "End Date: ${savingGoal.endDate}"
            binding.tvTargetAmount.text = "$${String.format("%.2f", savingGoal.targetAmount)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingGoalViewHolder {
        val binding = ItemSavingGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavingGoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SavingGoalViewHolder, position: Int) {
        holder.bind(savingGoals[position])
    }

    override fun getItemCount(): Int = savingGoals.size

    fun updateSavingGoals(newSavingGoals: List<SavingGoal>) {
        savingGoals = newSavingGoals
        notifyDataSetChanged()
    }
}

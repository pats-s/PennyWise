package com.example.pennywise.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.pennywise.databinding.ItemSavingGoalWithProgressBinding
import com.example.pennywise.remote.SavingGoal

class InsightsSavingGoalAdapter(
    private val savingGoals: List<SavingGoal>
) : RecyclerView.Adapter<InsightsSavingGoalAdapter.SavingGoalViewHolder>() {

    class SavingGoalViewHolder(private val binding: ItemSavingGoalWithProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(savingGoal: SavingGoal) {
            binding.tvSavingGoalTitle.text = savingGoal.title
            binding.tvTargetAmount.text = "Target: $${savingGoal.targetAmount}"
            binding.tvSavedAmount.text = "Saved: $${savingGoal.savedAmount}"

            // Calculate progress
            val progress = if (savingGoal.targetAmount > 0) {
                ((savingGoal.savedAmount / savingGoal.targetAmount) * 100).toInt()
            } else {
                0
            }
            binding.progressBar.progress = progress
            binding.tvProgressPercentage.text = "$progress%"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavingGoalViewHolder {
        val binding = ItemSavingGoalWithProgressBinding.inflate(
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
}

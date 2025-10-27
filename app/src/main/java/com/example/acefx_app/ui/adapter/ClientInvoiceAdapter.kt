package com.example.acefx_app.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.InvoiceModel
import com.example.acefx_app.databinding.ItemInvoiceBinding



class ClientInvoiceAdapter(
    private val invoices: List<InvoiceModel>,
    private val onInvoiceClick: (InvoiceModel) -> Unit
) : RecyclerView.Adapter<ClientInvoiceAdapter.InvoiceViewHolder>() {

    inner class InvoiceViewHolder(private val binding: ItemInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(invoice: InvoiceModel) = with(binding) {
            tvProjectName.text = invoice.projectName
            tvClientName.text = "Client: ${invoice.clientName}"
            tvInvoiceDate.text = invoice.date
            tvAmountValue.text = "₹${invoice.amount}"
            tvStatus.text = invoice.status

            // Change background dynamically based on status
            val context = root.context
            val bgRes = when (invoice.status.lowercase()) {
                "paid" -> R.drawable.status_paid_bg
                "pending" -> R.drawable.status_pending_bg
                "overdue" -> R.drawable.status_failed_bg
                else -> R.drawable.status_pending_bg
            }
            tvStatus.setBackgroundResource(bgRes)

            root.setOnClickListener { onInvoiceClick(invoice) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = ItemInvoiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        holder.bind(invoices[position])
    }

    override fun getItemCount(): Int = invoices.size
}

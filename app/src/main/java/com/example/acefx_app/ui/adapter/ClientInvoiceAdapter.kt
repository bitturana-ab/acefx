package com.example.acefx_app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.InvoiceData
import com.example.acefx_app.data.InvoiceModel

class ClientInvoiceAdapter :
    RecyclerView.Adapter<ClientInvoiceAdapter.InvoiceViewHolder>() {

    private var allInvoices: List<InvoiceData> = emptyList()
    private var filteredInvoices: List<InvoiceData> = emptyList()

    // Called when invoices are fetched
    fun submitList(list: List<InvoiceData>) {
        allInvoices = list
        filteredInvoices = list
        notifyDataSetChanged()
    }

    // Filter by status (All / Paid / Unpaid)
    fun filterList(status: String) {
        filteredInvoices = when (status.lowercase()) {
            "paid" -> allInvoices.filter { it.paid }
            "unpaid" -> allInvoices.filter { !it.paid  }
            else -> allInvoices
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        if (position in filteredInvoices.indices) {
            holder.bind(filteredInvoices[position])
        }
    }

    override fun getItemCount(): Int = filteredInvoices.size

    // 🔹 ViewHolder class
    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        @SuppressLint("SetTextI18n")
        fun bind(invoice: InvoiceData) {
            tvProjectName.text = invoice.projectId.title
            tvDate.text = ""
            tvAmount.text = "₹${invoice.amount}"

            // Change color dynamically (optional)
            val context = itemView.context
            val colorRes = when (invoice.paid) {
                true -> R.color.green_400
                false -> R.color.orange_200
                else -> R.color.gray
            }
            tvAmount.setTextColor(context.getColor(colorRes))
        }
    }
}

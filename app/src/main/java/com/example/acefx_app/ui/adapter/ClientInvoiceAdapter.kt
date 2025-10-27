package com.example.acefx_app.ui.client.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.InvoiceModel

class ClientInvoiceAdapter :
    RecyclerView.Adapter<ClientInvoiceAdapter.InvoiceViewHolder>() {

    private var allInvoices: List<InvoiceModel> = emptyList()
    private var filteredInvoices: List<InvoiceModel> = emptyList()

    // 🔹 Called when invoices are fetched from DB
    fun submitList(list: List<InvoiceModel>) {
        allInvoices = list
        filteredInvoices = list
        notifyDataSetChanged()
    }

    // 🔹 Filter by status (All / Paid / Unpaid)
    fun filterList(status: String) {
        filteredInvoices = when (status.lowercase()) {
            "paid" -> allInvoices.filter { it.status.equals("paid", ignoreCase = true) }
            "unpaid" -> allInvoices.filter { it.status.equals("unpaid", ignoreCase = true) }
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
        holder.bind(filteredInvoices[position])
    }

    override fun getItemCount(): Int = filteredInvoices.size

    // 🔹 ViewHolder class
    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProjectName = itemView.findViewById<TextView>(R.id.tvProjectName)
        private val tvAmount = itemView.findViewById<TextView>(R.id.tvAmountValue)
        private val tvDate = itemView.findViewById<TextView>(R.id.tvInvoiceDate)
        private val tvStatus = itemView.findViewById<TextView>(R.id.tvStatus)
        private val clientName = itemView.findViewById<TextView>(R.id.tvClientName)

        @SuppressLint("SetTextI18n")
        fun bind(invoice: InvoiceModel) {
            tvProjectName.text = invoice.projectName
            tvAmount.text = "₹${invoice.amount.toString()}"
            tvDate.text = invoice.date
            tvStatus.text = invoice.status
            clientName.text = invoice.clientName

            // Change color based on status
            val context = itemView.context
            val colorRes = when (invoice.status.lowercase()) {
                "paid" -> R.color.green_400
                "unpaid" -> R.color.orange_200
                else -> R.color.gray
            }
            tvStatus.setTextColor(context.getColor(colorRes))
        }
    }
}

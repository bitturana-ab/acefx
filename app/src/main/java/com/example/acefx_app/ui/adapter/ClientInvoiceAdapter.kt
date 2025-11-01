package com.example.acefx_app.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.acefx_app.R
import com.example.acefx_app.data.PaymentInfoForInvoice
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ClientInvoiceAdapter(
    private var invoiceList: List<PaymentInfoForInvoice>,
    private val onInvoiceClick: (PaymentInfoForInvoice) -> Unit
) : RecyclerView.Adapter<ClientInvoiceAdapter.InvoiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_invoice, parent, false)
        return InvoiceViewHolder(view, onInvoiceClick)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        if (position in invoiceList.indices) {
            holder.bind(invoiceList[position])
        }
    }

    override fun getItemCount(): Int = invoiceList.size

    fun submitList(newList: List<PaymentInfoForInvoice>) {
        invoiceList = newList
        notifyDataSetChanged()
    }

    class InvoiceViewHolder(
        itemView: View,
        private val onInvoiceClick: (PaymentInfoForInvoice) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)

        @SuppressLint("SetTextI18n")
        fun bind(invoice: PaymentInfoForInvoice) {
            tvProjectName.text = invoice.projectId?.title ?: "No Project"
            tvDate.text = formatDateTime(invoice.status)
            tvAmount.text = "₹${invoice.amount}"

            val context = itemView.context
            val colorRes = if (invoice.status == "success") R.color.status_active else R.color.orange_200
            tvAmount.setTextColor(context.getColor(colorRes))

            itemView.setOnClickListener {
                onInvoiceClick(invoice)
            }
        }

        private fun formatDateTime(isoDate: String?): String {
            if (isoDate.isNullOrEmpty()) return ""
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(isoDate)
                val formatter = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
                formatter.format(date!!)
            } catch (e: Exception) {
                e.printStackTrace()
                isoDate
            }
        }
    }
}

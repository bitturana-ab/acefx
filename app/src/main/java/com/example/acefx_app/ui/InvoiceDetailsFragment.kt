package com.example.acefx_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.acefx_app.databinding.FragmentInvoiceDetailsBinding
import com.example.acefx_app.R

class InvoiceDetailsFragment : Fragment() {

    private lateinit var binding: FragmentInvoiceDetailsBinding
    private val args: InvoiceDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInvoiceDetailsBinding.inflate(inflater, container, false)

        with(binding) {
            tvProjectName.text = args.invoiceProject
            tvClientName.text = args.invoiceClient
            tvInvoiceDate.text = args.invoiceDate
            tvInvoiceAmount.text = "₹${args.invoiceAmount}"
            tvInvoiceStatus.text = args.invoiceStatus

            val bgRes = when (args.invoiceStatus.lowercase()) {
                "paid" -> R.drawable.status_paid_bg
                "pending" -> R.drawable.status_pending_bg
                "overdue" -> R.drawable.status_failed_bg
                else -> R.drawable.status_pending_bg
            }
            tvInvoiceStatus.setBackgroundResource(bgRes)
        }

        return binding.root
    }
}

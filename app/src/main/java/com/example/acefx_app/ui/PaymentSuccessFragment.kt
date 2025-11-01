package com.example.acefx_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.databinding.FragmentPaymentSuccessBinding

class PaymentSuccessFragment : Fragment() {

    private var _binding: FragmentPaymentSuccessBinding? = null
    private val binding get() = _binding!!

    private var projectName: String? = null
    private var amount: Float = 0f
    private var transactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            transactionId = it.getString("transactionId")
            projectName = it.getString("projectName")
            amount = it.getFloat("amount")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display details
        binding.apply {
            tvProjectName.text = projectName ?: "Project"
            tvAmount.text = "₹%.2f".format(amount)
            tvTransactionId.text = transactionId ?: "N/A"
        }

        // Button: Back to Home
        binding.btnBackToHome.setOnClickListener {
            TODO("on back navigate to invoice")
            findNavController().navigateUp() // safely navigates back
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

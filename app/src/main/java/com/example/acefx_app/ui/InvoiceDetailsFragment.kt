package com.example.acefx_app.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acefx_app.R
import com.example.acefx_app.data.GetInvoiceById
import com.example.acefx_app.data.InvoiceData
import com.example.acefx_app.databinding.FragmentInvoiceDetailsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InvoiceDetailsFragment : Fragment() {

    private var _binding: FragmentInvoiceDetailsBinding? = null
    private val binding get() = _binding!!

    private var invoiceId: String? = null
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        invoiceId = arguments?.getString("invoiceId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvoiceDetailsBinding.inflate(inflater, container, false)
        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPref.getString("authToken", "") ?: ""

        if (token.isNullOrEmpty() || invoiceId.isNullOrEmpty()) {
            Toast.makeText(requireContext(),"No invoice Id there $invoiceId", Toast.LENGTH_SHORT).show()
        }
        fetchInvoiceDetails("Bearer $token", invoiceId!!)

        return binding.root
    }

    private fun fetchInvoiceDetails(token: String, invoiceId: String) {
        apiService.getInvoiceDetails(token, invoiceId).enqueue(object : Callback<GetInvoiceById> {
            override fun onResponse(call: Call<GetInvoiceById>, response: Response<GetInvoiceById>) {
                if (response.isSuccessful && response.body() != null) {
                    val invoice = response.body()!!.data
                    updateUI(invoice)
                }
                else{
                    Toast.makeText(requireContext(),"Not fetching", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetInvoiceById>, t: Throwable) {
                Log.d("INVOICE_DETAILS",t.toString())
                Toast.makeText(requireContext(),"Failed ${t.toString()}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(invoice: InvoiceData) {
        with(binding) {
            tvProjectName.text = invoice.projectId?.title ?: "N/A"
            tvClientName.text = invoice.currency ?: "N/A"
            tvInvoiceDate.text = invoice.completedTime ?: "N/A"
            tvInvoiceAmount.text = "₹${invoice.amount}"

            val status = if (invoice.paid) "Paid" else "Pending"
            tvInvoiceStatus.text = status

            val bgRes = when (status.lowercase()) {
                "paid" -> R.drawable.status_paid_bg
                "pending" -> R.drawable.status_pending_bg
                else -> R.drawable.status_failed_bg
            }
            tvInvoiceStatus.setBackgroundResource(bgRes)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

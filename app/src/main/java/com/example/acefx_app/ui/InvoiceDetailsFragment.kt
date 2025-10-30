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
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.data.GetInvoiceById
import com.example.acefx_app.data.InvoiceData
import com.example.acefx_app.databinding.FragmentInvoiceDetailsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
        //  back navigation setup
        binding.backBtn.setOnClickListener { findNavController().popBackStack() }

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPref.getString("authToken", "") ?: ""

        if (token.isNullOrEmpty() || invoiceId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "No invoice Id there $invoiceId", Toast.LENGTH_SHORT)
                .show()
        }
        fetchInvoiceDetails("Bearer $token", invoiceId!!)

        return binding.root
    }

    private fun fetchInvoiceDetails(token: String, invoiceId: String) {
        showLoading(true)
        apiService.getInvoiceDetails(token, invoiceId).enqueue(object : Callback<GetInvoiceById> {
            override fun onResponse(
                call: Call<GetInvoiceById>,
                response: Response<GetInvoiceById>
            ) {
                showLoading(false)
                if (response.isSuccessful && response.body() != null) {
                    val invoice = response.body()!!.data
                    updateUI(invoice)
                } else {
                    Toast.makeText(requireContext(), "Not fetching", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetInvoiceById>, t: Throwable) {
                Log.d("INVOICE_DETAILS", t.toString())
                showLoading(false)
                Toast.makeText(requireContext(), "Failed ${t.toString()}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(invoice: InvoiceData) {
        with(binding) {
            tvProjectName.text = invoice.projectId?.title ?: "N/A"
            tvDesc.text = invoice.projectId.description ?: "N/A"
            tvInvoiceDate.text = formatDateTime(invoice.completedTime) ?: "N/A"
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
    // loading screen
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.fadeIn()
            binding.progressBar.fadeIn()
        } else {
            binding.progressBar.fadeOut()
            binding.loadingOverlay.fadeOut()
        }
    }
    private fun View.fadeIn(duration: Long = 300) {
        alpha = 0f
        visibility = View.VISIBLE
        animate().alpha(1f).setDuration(duration).start()
    }

    private fun View.fadeOut(duration: Long = 300, endVisibility: Int = View.GONE) {
        animate().alpha(0f).setDuration(duration).withEndAction {
            visibility = endVisibility
        }.start()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

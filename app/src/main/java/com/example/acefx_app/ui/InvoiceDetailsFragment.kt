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
import com.example.acefx_app.data.PaymentInfoForDetails
import com.example.acefx_app.data.PaymentInfoForDetailsRes
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
    private val binding get() = _binding

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

        // Back button
        binding?.backBtn?.setOnClickListener {
            findNavController().popBackStack()
        }

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPref.getString("authToken", "") ?: ""

        if (token.isEmpty() || invoiceId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Missing invoice or token", Toast.LENGTH_SHORT).show()
            return binding!!.root
        }

        fetchInvoiceDetails("Bearer $token", invoiceId!!)
        return binding!!.root
    }

    private fun fetchInvoiceDetails(token: String, paymentId: String) {
        showLoading(true)
        apiService.getPaymentDetails(token, paymentId)
            .enqueue(object : Callback<PaymentInfoForDetailsRes> {
                override fun onResponse(
                    call: Call<PaymentInfoForDetailsRes>,
                    response: Response<PaymentInfoForDetailsRes>
                ) {
                    if (!isAdded || _binding == null) return // Fragment might be destroyed
                    showLoading(false)

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        if (body.success && body.data != null) {
                            updateUI(body.data)
                        } else {
                            Toast.makeText(requireContext(), body.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Log.e(
                            "INVOICE_DETAILS",
                            "Response Error: ${response.errorBody()?.string()}"
                        )
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<PaymentInfoForDetailsRes>, t: Throwable) {
                    if (!isAdded || _binding == null) return
                    showLoading(false)
                    Log.e("INVOICE_DETAILS", "API Failure: ${t.message}", t)
                    Toast.makeText(
                        requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(invoice: PaymentInfoForDetails) {
        binding?.let { b ->
            b.tvProjectName.text = " Project Name: ${invoice.projectId.title}" ?: "N/A"
            b.tvDesc.text = "Description: ${invoice.projectId.description}" ?: "N/A"
            b.tvClientName.text = "Name: ${invoice.clientId.name}" ?: "N/A"
            b.tvClientEmail.text = "Email: ${invoice.clientId.email}" ?: "N/A"
            b.tvPaymentId.text = "Payment ID: ${invoice.paymentId}" ?: "N/A"
            b.tvInvoiceDate.text =
                "Invoice Date: ${formatDateTime(invoice.projectId.completedTime)}" ?: "N/A"
            b.tvInvoiceAmount.text = "₹${invoice.amount}"
            b.tvNotes.text = "we are doing best work for you!"

            val status = when (invoice.status?.lowercase(Locale.ROOT)) {
                "success", "paid" -> "Paid"
                else -> "Pending"
            }

            b.tvInvoiceStatus.text = status
            val bgRes = when (status.lowercase(Locale.ROOT)) {
                "paid" -> R.drawable.status_paid_bg
                "pending" -> R.drawable.status_pending_bg
                else -> R.drawable.status_failed_bg
            }
            b.tvInvoiceStatus.setBackgroundResource(bgRes)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        val b = binding ?: return  // 🔒 Avoid NPE if view destroyed
        if (isLoading) {
            b.loadingOverlay.fadeIn()
            b.progressBar.fadeIn()
        } else {
            b.progressBar.fadeOut()
            b.loadingOverlay.fadeOut()
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
        if (isoDate.isNullOrEmpty()) return "N/A"
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            parser.timeZone = TimeZone.getTimeZone("UTC")
            val date = parser.parse(isoDate)
            val formatter = SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault())
            formatter.format(date!!)
        } catch (e: Exception) {
            Log.e("DATE_FORMAT", "Error parsing date: ${e.message}")
            isoDate
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

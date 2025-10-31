package com.example.acefx_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acefx_app.databinding.FragmentPaymentBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.data.CreatePaymentResponse
import com.example.acefx_app.data.PaymentRequest
import com.example.acefx_app.data.VerifyPaymentResponse
import com.razorpay.Checkout
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFragment : Fragment() {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var projectId: String? = null
    private var amount: Double = 0.0
    private var projectName: String? = null

    private lateinit var apiService: ApiService
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            projectId = it.getString("projectId")
            amount = it.getDouble("amount")
            projectName = it.getString("projectName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val sharedPref = requireContext().getSharedPreferences("UserSession", 0)
        token = sharedPref.getString("authToken", "") ?: ""

        binding.tvProjectName.text = projectName
        binding.tvAmount.text = "₹$amount"

        binding.btnPayNow.setOnClickListener {
            if (token.isNotEmpty() && projectId != null) {
                createOrder()
            } else {
                Toast.makeText(requireContext(), "Invalid project or user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createOrder() {
        val paymentRequest = PaymentRequest(
        amount = amount,
        projectId = projectId!!
    )

//        val response = apiService.createPaymentOrder(
//            token = token ?: "",
//            paymentData = paymentRequest
//        )

        apiService.createPaymentOrder("Bearer $token", paymentRequest)
            .enqueue(object : Callback<CreatePaymentResponse> {
                override fun onResponse(call: Call<CreatePaymentResponse>, response: Response<CreatePaymentResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val orderId = response.body()?.data?.order?.id
                        startRazorpay(orderId)
                    } else {
                        Toast.makeText(requireContext(), "Order creation failed ${response.toString()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CreatePaymentResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun startRazorpay(orderId: String?) {
        try {
            val checkout = Checkout()
            checkout.setKeyID("fnmdknvf") // Replace your Razorpay key
            val options = JSONObject()
            options.put("name", projectName)
            options.put("description", "Project Payment")
            options.put("order_id", orderId)
            options.put("currency", "INR")
            options.put("amount", (amount * 100).toInt())
            checkout.open(requireActivity(), options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onPaymentSuccess(razorpayPaymentId: String?, razorpayOrderId: String?, razorpaySignature: String?) {
        val body = mapOf(
            "razorpay_order_id" to razorpayOrderId.orEmpty(),
            "razorpay_payment_id" to razorpayPaymentId.orEmpty(),
            "razorpay_signature" to razorpaySignature.orEmpty()
        )

        apiService.verifyPayment("Bearer $token", body)
            .enqueue(object : Callback<VerifyPaymentResponse> {
                override fun onResponse(call: Call<VerifyPaymentResponse>, response: Response<VerifyPaymentResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Payment Successful!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Payment verification failed", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<VerifyPaymentResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

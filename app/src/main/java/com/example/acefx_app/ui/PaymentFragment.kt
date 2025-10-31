package com.example.acefx_app.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.data.CreatePaymentResponse
import com.example.acefx_app.data.PaymentRequest
import com.example.acefx_app.data.VerifyPaymentResponse
import com.example.acefx_app.databinding.FragmentPaymentBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaymentFragment : Fragment(), PaymentResultWithDataListener {

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    // Razorpay and backend variables
    private lateinit var apiService: ApiService
    private lateinit var token: String
    private var razorpayKeyId: String = ""
    private var orderId: String = ""

    // Project details
    private var projectId: String? = null
    private var amount: Double = 0.0
    private var projectName: String? = null

    // User details
    private var userName: String? = null
    private var companyName: String? = null
    private var userEmail: String? = null
    private var userPhone: String? = null

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

        // Load data from SharedPreferences
        val sharedPref = requireContext().getSharedPreferences("UserSession", 0)
        token = sharedPref.getString("authToken", "") ?: ""
        userName = sharedPref.getString("name", "")
        companyName = sharedPref.getString("companyName", "")
        userEmail = sharedPref.getString("email", "")
        userPhone = sharedPref.getString("phoneNumber", "")

        // Display info
        binding.tvProjectName.text = projectName ?: "Project"
        binding.tvAmount.text = "₹$amount"
        binding.tvCompany.text = "Company: $companyName"
        binding.tvEmail.text = "Email: $userEmail"
        binding.tvPhone.text = "Phone: $userPhone"

        // Fetch Razorpay key securely
        fetchRazorpayKey()

        // Handle Pay button
        binding.btnPayNow.setOnClickListener {
            if (token.isNotEmpty() && projectId != null) {
                createOrder()
            } else {
                Toast.makeText(requireContext(), "Invalid project or session", Toast.LENGTH_SHORT).show()
            }
        }

        Checkout.preload(requireContext())
    }

    /** Step 1: Fetch Razorpay Key from Backend */
    private fun fetchRazorpayKey() {
        apiService.getRazorpayKey("Bearer $token").enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                if (response.isSuccessful) {
                    razorpayKeyId = response.body()?.get("key") ?: ""
                    Log.d("RAZORPAY_KEY", "Fetched key: $razorpayKeyId")
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch Razorpay key", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /** Step 2: Create Order in Backend */
    private fun createOrder() {
        val paymentRequest = PaymentRequest(amount, projectId ?: "")

        apiService.createPaymentOrder("Bearer $token", paymentRequest)
            .enqueue(object : Callback<CreatePaymentResponse> {
                override fun onResponse(call: Call<CreatePaymentResponse>, response: Response<CreatePaymentResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val order = response.body()?.data?.order
                        orderId = order?.id ?: ""
                        startRazorpay(orderId)
                    } else {
                        Toast.makeText(requireContext(), "Failed to create order", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CreatePaymentResponse>, t: Throwable) {
//                    TODO("npe and not navigating")
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    /** Step 3: Start Razorpay Checkout */
    private fun startRazorpay(orderId: String) {
        try {
            val checkout = Checkout()
            checkout.setKeyID(razorpayKeyId)

            val options = JSONObject().apply {
                put("name", companyName ?: userName ?: "Client")
                put("description", "Payment for $projectName")
                put("order_id", orderId)
                put("currency", "INR")
                put("amount", (amount * 100).toInt()) // amount in paise
                put("prefill", JSONObject().apply {
                    put("email", userEmail ?: "test@example.com")
                    put("contact", userPhone ?: "9999999999")
                })
            }

            checkout.open(requireActivity(), options)
        } catch (e: Exception) {
            Log.e("RAZORPAY_ERROR", "Error initializing Razorpay: ${e.message}")
            Toast.makeText(requireContext(), "Error initializing Razorpay", Toast.LENGTH_SHORT).show()
        }
    }

    /** Step 4: Payment Success — with all details */
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        Toast.makeText(requireContext(), "Payment Success!", Toast.LENGTH_SHORT).show()

        val orderId = paymentData?.orderId ?: ""
        val signature = paymentData?.signature ?: ""
        val paymentId = razorpayPaymentId ?: ""

        Log.d("RAZORPAY_PAYMENT", "OrderID: $orderId | PaymentID: $paymentId | Signature: $signature")

        verifyPayment(orderId, paymentId, signature)
    }

    /** Step 5: Payment Failed */
    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        Toast.makeText(requireContext(), "Payment Failed: $description", Toast.LENGTH_LONG).show()
    }

    /** Step 6: Verify Payment with Backend */
    private fun verifyPayment(orderId: String, razorpayPaymentId: String, signature: String) {
        val verifyBody = mapOf(
            "razorpay_order_id" to orderId,
            "razorpay_payment_id" to razorpayPaymentId,
            "razorpay_signature" to signature
        )

        apiService.verifyPayment("Bearer $token", verifyBody)
            .enqueue(object : Callback<VerifyPaymentResponse> {
                override fun onResponse(call: Call<VerifyPaymentResponse>, response: Response<VerifyPaymentResponse>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Payment Verified Successfully!", Toast.LENGTH_LONG).show()

                        val action = PaymentFragmentDirections
                            .actionPaymentFragmentToPaymentSuccessFragment(
                                razorpayPaymentId,
                                amount.toFloat(),
                                projectName ?: "Project"
                            )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "Verification Failed!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<VerifyPaymentResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

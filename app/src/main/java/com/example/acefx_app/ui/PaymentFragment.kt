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

    private lateinit var apiService: ApiService
    private lateinit var token: String

    private var razorpayKeyId = ""
    private var orderId = ""

    private var projectId: String? = null
    private var amount = 0.0
    private var projectName: String? = null

    private var userName: String? = null
    private var companyName: String? = null
    private var userEmail: String? = null
    private var userPhone: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString("projectId")
            amount = it.getDouble("amount").div(2)
            projectName = it.getString("projectName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
        userName = sharedPref.getString("name", "")
        companyName = sharedPref.getString("companyName", "")
        userEmail = sharedPref.getString("email", "")
        userPhone = sharedPref.getString("phoneNumber", "")

        binding.apply {
            tvProjectName.text = projectName ?: "Project"
            tvAmount.text = "₹$amount"
            tvCompany.text = "Company: ${companyName ?: "N/A"}"
            tvEmail.text = "Email: ${userEmail ?: "N/A"}"
            tvPhone.text = "Phone: ${userPhone ?: "N/A"}"
        }

        Checkout.preload(requireContext())
        fetchRazorpayKey()

        binding.btnPayNow.setOnClickListener {
            if (token.isNotEmpty() && projectId != null)
                createOrder()
            else
                Toast.makeText(requireContext(), "Invalid session", Toast.LENGTH_SHORT).show()
        }
    }

    /** Step 1: Fetch Razorpay Key */
    private fun fetchRazorpayKey() {
        apiService.getRazorpayKey("Bearer $token")
            .enqueue(object : Callback<Map<String, String>> {
                override fun onResponse(
                    call: Call<Map<String, String>>,
                    response: Response<Map<String, String>>
                ) {
                    razorpayKeyId = response.body()?.get("key") ?: ""
                }

                override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Check Internet Connections}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** Step 2: Create Order in Backend */
    private fun createOrder() {
        val paymentRequest = PaymentRequest(amount, projectId ?: "")
        apiService.createPaymentOrder("Bearer $token", paymentRequest)
            .enqueue(object : Callback<CreatePaymentResponse> {
                override fun onResponse(
                    call: Call<CreatePaymentResponse>,
                    response: Response<CreatePaymentResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        orderId = response.body()?.data?.order?.id ?: ""

                        Toast.makeText(
                            requireContext(),
                            "Verifying! Please wait a minute!",
                            Toast.LENGTH_SHORT
                        ).show()
                        startRazorpay(orderId)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to create order",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CreatePaymentResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Check Internet Connections",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    /** Step 3: Start Razorpay Checkout */
    private fun startRazorpay(orderId: String) {
        try {
            val checkout = Checkout().apply { setKeyID(razorpayKeyId) }

            val options = JSONObject().apply {
                put("name", companyName ?: userName ?: "Client")
                put("description", "Payment for $projectName")
                put("order_id", orderId)
                put("currency", "INR")
                put("amount", (amount * 100).toLong())
                put(
                    "prefill",
                    JSONObject().apply {
                        put("email", userEmail ?: "test@example.com")
                        put("contact", userPhone ?: "9999999999")
                    }
                )
            }

            checkout.open(requireActivity(), options)

        } catch (e: Exception) {
            Log.e("RAZORPAY_ERROR", e.message.toString())
            Toast.makeText(requireContext(), "Failed to start Razorpay", Toast.LENGTH_SHORT).show()
        }
    }

    /** Called from Activity on success */
    override fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val orderId = paymentData?.orderId ?: ""
        val signature = paymentData?.signature ?: ""
        val paymentId = razorpayPaymentId ?: ""
        Toast.makeText(requireContext(), "Verifying Payment: ${paymentData.toString()}", Toast.LENGTH_LONG).show()

        verifyPayment(orderId, paymentId, signature)
    }

    /** Called from Activity on error */
    override fun onPaymentError(code: Int, description: String?, paymentData: PaymentData?) {
        Toast.makeText(requireContext(), "Payment Failed: $description", Toast.LENGTH_LONG).show()
    }

    /** Step 4: Verify Payment */
    private fun verifyPayment(orderId: String, razorpayPaymentId: String, signature: String) {
        val verifyBody = mapOf(
            "razorpay_order_id" to orderId,
            "razorpay_payment_id" to razorpayPaymentId,
            "razorpay_signature" to signature
        )

        apiService.verifyPayment("Bearer $token", verifyBody)
            .enqueue(object : Callback<VerifyPaymentResponse> {
                override fun onResponse(
                    call: Call<VerifyPaymentResponse>,
                    response: Response<VerifyPaymentResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(requireContext(), "Payment Successful!", Toast.LENGTH_SHORT)
                            .show()

                        val action = PaymentFragmentDirections
                            .actionPaymentFragmentToPaymentSuccessFragment(
                                projectName=projectName ?: "Project",
                                amount = amount.toFloat(),
                                razorpayPaymentId = razorpayPaymentId
                            )
                        findNavController().navigate(action)
                    } else {
                        Toast.makeText(requireContext(), "Verification Failed!", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                override fun onFailure(call: Call<VerifyPaymentResponse>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

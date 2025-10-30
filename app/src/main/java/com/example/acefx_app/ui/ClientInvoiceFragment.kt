package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.data.InvoiceModel
import com.example.acefx_app.data.AllInvoices
import com.example.acefx_app.data.InvoiceData
import com.example.acefx_app.databinding.FragmentClientInvoiceBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ClientInvoiceAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ClientInvoiceFragment : Fragment() {

    private var _binding: FragmentClientInvoiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var invoiceAdapter: ClientInvoiceAdapter
    private lateinit var apiService: ApiService
    private lateinit var token: String
    private var allInvoices = listOf<InvoiceData>()
    private val gson = Gson()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientInvoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        setupTabs()
        setupSwipeRefresh()

        loadCachedInvoices()
        fetchInvoices()
    }

    private fun setupRecyclerView() {
        invoiceAdapter = ClientInvoiceAdapter()
        binding.recyclerInvoices.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInvoices.adapter = invoiceAdapter
    }

    private fun setupTabs() {
        binding.invoiceTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterInvoices(tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            val firstTab = binding.invoiceTabLayout.getTabAt(0)
            firstTab?.select()
            fetchInvoices()
        }
    }

    /** Load cached invoices from SharedPreferences */
    private fun loadCachedInvoices() {
        val prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("cachedInvoices", null)
        if (cachedJson != null) {
            val type = object : TypeToken<List<InvoiceModel>>() {}.type
            val cachedList: List<InvoiceData> = gson.fromJson(cachedJson, type)
            if (cachedList.isNotEmpty()) {
                allInvoices = cachedList
                invoiceAdapter.submitList(allInvoices)
                showEmptyState(false)
                Log.d("CACHE", "Loaded ${cachedList.size} cached invoices")
            }
        }
    }

    /** Save invoices to SharedPreferences */
    private fun saveInvoicesToCache(invoices: List<InvoiceData>) {
        val prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val json = gson.toJson(invoices)
        prefs.edit().putString("cachedInvoices", json).apply()
        Log.d("CACHE", "Saved ${invoices.size} invoices to cache")
    }

    /** Fetch invoices from backend */
    private fun fetchInvoices() {
        showLoading(true)
        apiService.getMyInvoices("Bearer $token").enqueue(object : Callback<AllInvoices> {
            override fun onResponse(call: Call<AllInvoices>, response: Response<AllInvoices>) {
                if (!isAdded) return
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false

                if (response.isSuccessful) {
                    val invoiceResponse = response.body()
                    allInvoices = invoiceResponse?.data ?: emptyList()
                    Log.d("INVOICES", "Fetched ${allInvoices.size} invoices")

                    if (allInvoices.isEmpty()) {
                        showEmptyState(true)
                    } else {
                        showEmptyState(false)
                        saveInvoicesToCache(allInvoices)

                        val selectedTab = binding.invoiceTabLayout.getTabAt(
                            binding.invoiceTabLayout.selectedTabPosition
                        )
                        val tabText = selectedTab?.text?.toString() ?: "All"
                        filterInvoices(tabText)
                    }

                } else {
                    Snackbar.make(binding.root, "Failed to load invoices", Snackbar.LENGTH_SHORT).show()
                    showEmptyState(true)
                }
            }

            override fun onFailure(call: Call<AllInvoices>, t: Throwable) {
                if (!isAdded) return
                showLoading(false)
                binding.swipeRefresh.isRefreshing = false
                Snackbar.make(binding.root, "Network error, please check your connection", Snackbar.LENGTH_SHORT).show()
                Log.e("INVOICES", "Error fetching invoices: ${t.message}")
            }
        })
    }

    /** Filter invoices based on tab selection */
    private fun filterInvoices(statusText: String) {
        val filtered = when (statusText.lowercase()) {
            "paid" -> allInvoices.filter { it.paid }
            "unpaid" -> allInvoices.filter { !it.paid }
            else -> allInvoices
        }

        invoiceAdapter.submitList(filtered)
        showEmptyState(filtered.isEmpty())
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(show: Boolean) {
//        binding.emptyText.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerInvoices.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

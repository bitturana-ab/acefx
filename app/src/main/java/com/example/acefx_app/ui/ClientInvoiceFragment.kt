package com.example.acefx_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.data.InvoiceModel
import com.example.acefx_app.databinding.FragmentClientInvoiceBinding
import com.example.acefx_app.ui.client.adapters.ClientInvoiceAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*

class ClientInvoiceFragment : Fragment() {

    private var _binding: FragmentClientInvoiceBinding? = null
    private val binding get() = _binding!!

    private lateinit var invoiceAdapter: ClientInvoiceAdapter
    private var allInvoices = mutableListOf<InvoiceModel>()
    private val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientInvoiceBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupTabs()
        loadInvoices()
        return binding.root
    }

    private fun setupRecyclerView() {
        invoiceAdapter = ClientInvoiceAdapter()
        binding.recyclerInvoices.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = invoiceAdapter
        }
    }

    private fun setupTabs() {
        binding.invoiceTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> updateFilter("All")
                    1 -> updateFilter("Paid")
                    2 -> updateFilter("Unpaid")
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun loadInvoices() {
        showLoading(true)
        uiScope.launch {
            delay(1000) // simulate network delay

            // Replace this with your API or MongoDB query
            allInvoices = mutableListOf(
                InvoiceModel("Website Redesign", "ab", "2025-10-10",3495.43,"paid"),
                InvoiceModel("Android App Development", "jd", "2025-10-15",234.35,"paid"),
                InvoiceModel("Logo Package", "sanjay", "2025-09-28",345.654,"unpaid"),
                InvoiceModel("SEO Optimization", "jason", "2025-10-21",2345.35,"unpaid")
            )

            invoiceAdapter.submitList(allInvoices)
            showLoading(false)
        }
    }

    private fun updateFilter(filter: String) {
        val filteredList = when (filter) {
            "Paid" -> allInvoices.filter { it.status.equals("Paid", true) }
            "Unpaid" -> allInvoices.filter { it.status.equals("Unpaid", true) }
            else -> allInvoices
        }
        invoiceAdapter.submitList(filteredList)
    }

    private fun showLoading(show: Boolean) {
        binding.loadingOverlay.visibility = if (show) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        uiScope.cancel()
    }
}

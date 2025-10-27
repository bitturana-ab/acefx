package com.example.acefx_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.acefx_app.R

class AccountFragment : Fragment() {

    private lateinit var btnAccount: Button
    private lateinit var btnInvoice: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        btnAccount = view.findViewById(R.id.btnAccount)
        btnInvoice = view.findViewById(R.id.btnInvoice)

        // Default load - show Account section
        loadFragment(ClientAccountFragment())
        updateTabSelection(activeTab = "account")

        btnAccount.setOnClickListener {
            loadFragment(ClientAccountFragment())
            updateTabSelection(activeTab = "account")
        }

        btnInvoice.setOnClickListener {
            loadFragment((ClientInvoiceFragment()))
            updateTabSelection(activeTab = "invoice")
        }

        return view
    }

    private fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.accountContentContainer, fragment)
            .commit()
    }

    private fun updateTabSelection(activeTab: String) {
        when (activeTab) {
            "account" -> {
                btnAccount.backgroundTintList = requireContext().getColorStateList(R.color.purple_500)
                btnAccount.setTextColor(resources.getColor(R.color.white))
                btnInvoice.backgroundTintList = requireContext().getColorStateList(R.color.gray)
                btnInvoice.setTextColor(resources.getColor(R.color.white))
            }
            "invoice" -> {
                btnInvoice.backgroundTintList = requireContext().getColorStateList(R.color.purple_500)
                btnInvoice.setTextColor(resources.getColor(R.color.white))
                btnAccount.backgroundTintList = requireContext().getColorStateList(R.color.gray)
                btnAccount.setTextColor(resources.getColor(R.color.white))
            }
        }
    }
}

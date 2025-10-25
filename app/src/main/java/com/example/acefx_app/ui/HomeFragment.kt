package com.example.acefx_app.ui


import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.R
import com.example.acefx_app.databinding.FragmentHomeBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.goToHomeBtn.setOnClickListener {
            goToProfileOrProjects()
        }
    }

    private fun goToProfileOrProjects() {
        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val companyName = sharedPref.getString("companyName", null)
        val phoneNumber = sharedPref.getString("phoneNumber", null)
        val pinCode = sharedPref.getString("pinCode", null)

        if (!companyName.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && !pinCode.isNullOrEmpty()) {
            // All profile info exists, go to projects
            findNavController().navigate(R.id.clientProjectsFragment)
        } else {
            // Profile incomplete, go to update screen
            findNavController().navigate(R.id.clientProfileFragment)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

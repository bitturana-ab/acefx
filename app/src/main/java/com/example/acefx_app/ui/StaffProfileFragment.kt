package com.example.acefx_app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.example.acefx_app.R
import com.example.acefx_app.databinding.FragmentStaffProfileBinding

class StaffProfileFragment : Fragment() {
    private var _binding: FragmentStaffProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var fullNameInput: EditText
    private lateinit var staffPhoneInput: EditText
    private lateinit var idInput: EditText
    private lateinit var createProfileBtn: Button
    private lateinit var nearbyCheck: CheckBox
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStaffProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullNameInput = binding.fullNameInput
        staffPhoneInput = binding.staffPhoneInput
        idInput = binding.idInput
        nearbyCheck = binding.nearbyCheck
        createProfileBtn = binding.createProfileBtn

        createProfileBtn.setOnClickListener {
            val name = fullNameInput.text.toString().trim()
            val phone = staffPhoneInput.text.toString().trim()
            val id = idInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || id.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val nearby = nearbyCheck.isChecked
                Toast.makeText(
                    requireContext(),
                    "Profile Created! Nearby: $nearby",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

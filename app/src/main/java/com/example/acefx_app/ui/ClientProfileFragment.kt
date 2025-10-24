package com.example.acefx_app.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.acefx_app.databinding.FragmentClientProfileBinding

class ClientProfileFragment : Fragment() {
    private var _binding: FragmentClientProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var companyNameInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var pinCodeInput: EditText
    private lateinit var nextBtn: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentClientProfileBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        companyNameInput = binding.companyNameInput
        phoneInput = binding.phoneInput
        pinCodeInput = binding.pinCodeInput
        nextBtn = binding.nextBtn

        nextBtn.setOnClickListener {
            val name = companyNameInput.text.toString().trim()
            val phone = phoneInput.text.toString().trim()
            val pin = pinCodeInput.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Next Step → Profile Saved!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}
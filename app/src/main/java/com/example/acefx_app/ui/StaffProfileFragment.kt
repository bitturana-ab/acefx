package com.example.acefx_app.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acefx_app.databinding.FragmentStaffProfileBinding

class StaffProfileFragment : Fragment() {

    private var _binding: FragmentStaffProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStaffProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCreateProfileButton()
    }

    /** Setup button behavior with safety and animation */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupCreateProfileButton() {
        val nameInput = binding.fullNameInput
        val phoneInput = binding.staffPhoneInput
        val idInput = binding.idInput
        val nearbyCheck = binding.nearbyCheck
        val createBtn = binding.createProfileBtn

        // Touch animation for a natural feel
        createBtn.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }
            false
        }

        createBtn.setOnClickListener {
            try {
                val name = nameInput.text?.toString()?.trim().orEmpty()
                val phone = phoneInput.text?.toString()?.trim().orEmpty()
                val id = idInput.text?.toString()?.trim().orEmpty()
                val nearby = nearbyCheck.isChecked

                when {
                    name.isEmpty() || phone.isEmpty() || id.isEmpty() -> {
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    }
                    phone.length !in 10..13 -> {
                        Toast.makeText(requireContext(), "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(
                            requireContext(),
                            "Profile Created! Nearby: $nearby",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Something went wrong!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

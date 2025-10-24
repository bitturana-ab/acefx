package com.example.acefx_app.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
            gotoMyHome()
        }
    }

    private fun gotoMyHome() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(100) // Ensure NavController is ready
            val action = HomeFragmentDirections.actionHomeFragmentToClientProfileFragment()
            findNavController().navigate(action)
//            staff page is disable for now

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

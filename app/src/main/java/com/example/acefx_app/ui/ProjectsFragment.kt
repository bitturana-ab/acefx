package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.acefx_app.R
import com.example.acefx_app.data.ProjectItem
import com.example.acefx_app.data.ProjectsResponse
import com.example.acefx_app.databinding.FragmentProjectsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService
import com.example.acefx_app.ui.adapter.ProjectsAdapter
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProjectsAdapter
    private lateinit var apiService: ApiService
    private lateinit var token: String
    private var allProjects = listOf<ProjectItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        token = sharedPref.getString("authToken", "") ?: ""
        val companyName = sharedPref.getString("companyName", "Client")
        binding.clientNameText.text = companyName

        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        adapter = ProjectsAdapter(emptyList()) { project ->
            // You can still open project details using NavController if needed
            Toast.makeText(requireContext(), "Project: ${project.title}", Toast.LENGTH_SHORT).show()
        }
        binding.projectsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.projectsRecyclerView.adapter = adapter

        loadProjects()

        binding.projectTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                filterProjectsByStatus(tab?.text.toString())
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.chatNow.setOnClickListener {
            findNavController().navigate(R.id.chatFragment)
        }

    }

    /** Load projects from backend */
    private fun loadProjects() {
        showLoading(true)
        apiService.getClientProjects("Bearer $token")
            .enqueue(object : Callback<ProjectsResponse> {
                override fun onResponse(
                    call: Call<ProjectsResponse>,
                    response: Response<ProjectsResponse>
                ) {
                    Log.d("LOAD_PROJECTS", response.toString())
                    if (!isAdded) return
                    showLoading(false)

                    if (response.isSuccessful) {
                        allProjects = response.body()?.projects ?: emptyList()
                        filterProjectsByStatus("approved")
                    } else {
                        Toast.makeText(requireContext(), "Failed to load projects!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ProjectsResponse>, t: Throwable) {
                    if (!isAdded) return
                    showLoading(false)
                    Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun filterProjectsByStatus(status: String) {
        val filtered = allProjects.filter { it.status.equals(status, ignoreCase = true) }
        adapter.updateData(filtered)
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.fadeIn()
            binding.progressBar.fadeIn()
        } else {
            binding.progressBar.fadeOut()
            binding.loadingOverlay.fadeOut()
        }
    }

    private fun View.fadeIn(duration: Long = 300) {
        this.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        }
    }

    private fun View.fadeOut(duration: Long = 300, endVisibility: Int = View.GONE) {
        this.animate().alpha(0f).setDuration(duration).withEndAction {
            visibility = endVisibility
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

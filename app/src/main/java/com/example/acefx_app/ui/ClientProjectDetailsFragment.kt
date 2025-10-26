package com.example.acefx_app.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acefx_app.data.ProjectData
import com.example.acefx_app.databinding.FragmentClientProjectDetailsBinding
import com.example.acefx_app.retrofitServices.ApiClient
import com.example.acefx_app.retrofitServices.ApiService

class ClientProjectDetailsFragment : Fragment() {

    private var _binding: FragmentClientProjectDetailsBinding? = null
    private val binding get() = _binding!!

    private var projectId: String? = null
    private lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getString("projectId")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientProjectDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (projectId != null) {
            fetchProjectDetails(projectId!!)
        }
    }

    private fun fetchProjectDetails(id: String) {
        showLoading(true)

        apiService = ApiClient.getClient(requireContext()).create(ApiService::class.java)

        val sharedPref = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val token = sharedPref.getString("authToken", "") ?: ""
        if (token.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getProjectById("Bearer $token", id)
            .enqueue(object : retrofit2.Callback<ProjectData> {
                override fun onResponse(
                    call: retrofit2.Call<ProjectData>,
                    response: retrofit2.Response<ProjectData>
                ) {
                    showLoading(false)
                    if (!isAdded) return

                    val project = response.body()
                    if (response.isSuccessful && project != null) {
                        displayProjectDetails(project)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Project not found or failed to fetch!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ProjectData>, t: Throwable) {
                    showLoading(false)
                    if (!isAdded) return
                    Toast.makeText(requireContext(), "Network error!", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayProjectDetails(project: ProjectData) {
        binding.projectTitleText.text = project.title
        binding.projectDescriptionText.text = project.description
        binding.projectDeadlineText.text = project.deadline
        binding.projectStatusText.text = project.status
        binding.projectAmountText.text = "₹${project.expectedAmount}"

        binding.projectDataLink.setOnClickListener {
            // open project.dataLink
        }
        binding.projectAttachLink.setOnClickListener {
            // open project.attachLink
        }
    }

    /** Smooth fade-in/fade-out loading overlay */
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loadingOverlay.fadeIn()
            binding.progressBar.fadeIn()
        } else {
            binding.progressBar.fadeOut()
            binding.loadingOverlay.fadeOut()
        }
    }

    /** Fade-in extension */
    private fun View.fadeIn(duration: Long = 300) {
        this.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setDuration(duration).start()
        }
    }

    /** Fade-out extension */
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

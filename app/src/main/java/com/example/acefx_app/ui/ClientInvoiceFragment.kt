import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.acefx_app.data.InvoiceModel
import com.example.acefx_app.databinding.FragmentClientInvoiceBinding
import com.example.acefx_app.ui.adapter.ClientInvoiceAdapter

class ClientInvoiceFragment : Fragment() {

    private lateinit var binding: FragmentClientInvoiceBinding
    private lateinit var adapter: ClientInvoiceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentClientInvoiceBinding.inflate(inflater, container, false)

        setupRecyclerView()
        return binding.root
    }

    private fun setupRecyclerView() {
        val sampleInvoices = listOf(
            InvoiceModel("Website Development", "John Doe", "25 Oct 2025", "29,500", "Paid"),
            InvoiceModel("App Design UI/UX", "Priya Sharma", "20 Oct 2025", "18,000", "Pending"),
            InvoiceModel("Backend API Setup", "Rahul Singh", "18 Oct 2025", "35,000", "Overdue")
        )

        adapter = ClientInvoiceAdapter(sampleInvoices) { invoice ->
            Toast.makeText(requireContext(), "Clicked: ${invoice.projectName}", Toast.LENGTH_SHORT).show()
            val action = ClientInvoiceFragmentDirections
                .actionClientInvoiceFragmentToInvoiceDetailsFragment(
                    invoiceProject = invoice.projectName,
                    invoiceClient = invoice.clientName,
                    invoiceDate = invoice.date,
                    invoiceAmount = invoice.amount,
                    invoiceStatus = invoice.status
                )
            findNavController().navigate(action)
        }


        binding.recyclerInvoices.adapter = adapter
    }
}

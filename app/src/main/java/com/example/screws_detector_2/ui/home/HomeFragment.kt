package com.example.screws_detector_2.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.screws_detector_2.R
import com.example.screws_detector_2.databinding.FragmentHomeBinding
import com.example.screws_detector_2.data.model.Machine

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vm: HomeViewModel by viewModels {
        HomeVMFactory(requireContext())
    }

    private lateinit var adapter: MachineListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        adapter = MachineListAdapter(::openDetails)
        binding.rvMachines.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMachines.adapter = adapter

        vm.machines.observe(viewLifecycleOwner) { showMachines(it) }
        vm.fetch()
    }

    private fun showMachines(list: List<Machine>) {
        binding.progress.visibility = View.GONE
        adapter.submitList(list)
    }

    private fun openDetails(machine: Machine) =
        MachineDetailsActivity.start(requireContext(), machine)

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

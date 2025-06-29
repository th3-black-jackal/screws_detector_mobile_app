package com.example.screws_detector_2.ui.home

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.screws_detector_2.data.MachineRepository
import com.example.screws_detector_2.data.model.Machine
import kotlinx.coroutines.launch

class HomeViewModel(private val appCtx: Context) : ViewModel() {

    private val _machines = MutableLiveData<List<Machine>>()
    val machines: LiveData<List<Machine>> = _machines

    fun fetch() = viewModelScope.launch {
        runCatching { MachineRepository.getAll(appCtx) }
            .onSuccess { _machines.value = it }
            .onFailure { _machines.value = emptyList() }
    }
}

package com.example.screws_detector_2.ui.home


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.screws_detector.data.remote.NetworkModule
import com.example.screws_detector_2.data.model.Machine
import com.example.screws_detector_2.databinding.ActivityMachineDetailsBinding
import kotlinx.coroutines.launch


class MachineDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMachineDetailsBinding
    private val api by lazy { NetworkModule.apiService(this)}
    private lateinit var machine: Machine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        machine = intent.getParcelableExtra(EXTRA)!!


        Log.d("MACHINE_STATUS", machine.status)
        Log.d("MACHINE_NAME", machine.name)
        machine.description?.let { Log.d("MACHINE_DESC", it) }

        show(machine)
        initButtons()

    }

    private fun initButtons() = with(binding){
        btnOpen.setOnClickListener  { toggleMachine(true) }
        btnClose.setOnClickListener { toggleMachine(false) }
    }

    private fun toggleMachine(open: Boolean) = lifecycleScope.launch{
        try{
            setButtonsEnabled(false)
            if (open) api.openMachine(machine.id)
            else      api.closeMachine(machine.id)
            machine = machine.copy(status = if (open) "ON" else "OFF")
            show(machine)
            toast("Machine ${if (open) "opened" else "closed"} ✅")
        } catch(e: Exception){
            toast("Action failed: ${e.localizedMessage}")
        } finally {
            setButtonsEnabled(true)
        }
    }

    private fun setButtonsEnabled(enable: Boolean) = with(binding){
        btnOpen.isEnabled  = enable
        btnClose.isEnabled = enable

    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


    private fun show(m: Machine) = with(binding){
        tvName.text = m.name
        tvStatus.text = "Status: ${m.status}"
        tvHeartbeat.text = "Heartbeat: ${m.last_heartbeat}"
        tvId.text = "ID: ${m.id}"
        tvActive.text = "Active: ${m.is_active}"
        tvOp.text = "Param: ${m.operating_param}"
        tvDesc.text = m.description ?: "(no description)"
    }

    companion object {
        private const val EXTRA = "machine"
        fun start(ctx: Context, m: Machine) =
            ctx.startActivity(Intent(ctx, MachineDetailsActivity::class.java).putExtra(EXTRA, m))
    }
}

package com.example.screws_detector_2.ui.home


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.screws_detector_2.data.model.Machine
import com.example.screws_detector_2.databinding.ActivityMachineDetailsBinding


class MachineDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMachineDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMachineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val machine = intent.getParcelableExtra<Machine>(EXTRA)!!

        binding.tvName.text = machine.name
        binding.tvStatus.text = "Status: ${machine.status}"
        binding.tvHeartbeat.text = "Heartbeat: ${machine.last_heartbeat}"
        binding.tvId.text = "ID: ${machine.id}"
        binding.tvActive.text = "Active: ${machine.is_active}"
        binding.tvOp.text = "Param: ${machine.operating_param}"
        binding.tvDesc.text = machine.description ?: "(no description)"
    }

    companion object {
        private const val EXTRA = "machine"
        fun start(ctx: Context, m: Machine) =
            ctx.startActivity(Intent(ctx, MachineDetailsActivity::class.java).putExtra(EXTRA, m))
    }
}

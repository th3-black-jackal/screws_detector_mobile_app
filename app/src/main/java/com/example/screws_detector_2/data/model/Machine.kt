package com.example.screws_detector_2.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Machine(
    val id: Int,
    val name: String,
    val description: String?,
    val status: String,
    val last_heartbeat: String,
    val is_active: Boolean,
    val operating_param: Int
) : Parcelable

package com.example.persianalarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "یادآوری", // کاری، شخصی، جلسه، دارو، مناسبت
    val year: Int,
    val month: Int, // 1 تا 12
    val day: Int,
    val hour: Int,
    val minute: Int,
    val triggerTimeMillis: Long,
    val isEnabled: Boolean = true,
    val repeatType: String = "یک‌بار" // یک‌بار، روزانه، هفتگی
)

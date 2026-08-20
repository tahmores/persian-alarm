package com.example.persianalarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.persianalarm.data.AlarmEntity
import com.example.persianalarm.data.AppDatabase
import com.example.persianalarm.receiver.AlarmReceiver
import com.example.persianalarm.ui.screens.AddAlarmDialog
import com.example.persianalarm.ui.screens.HomeScreen
import com.example.persianalarm.ui.theme.PersianAlarmTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database = AppDatabase.getDatabase(this)

        requestPermissions()

        setContent {
            PersianAlarmTheme {
                var showAddDialog by remember { mutableStateOf(false) }
                val alarms by database.alarmDao().getAllAlarms().collectAsState(initial = emptyList())

                HomeScreen(
                    alarms = alarms,
                    onAddAlarmClick = { showAddDialog = true },
                    onToggleAlarm = { alarm, isEnabled ->
                        lifecycleScope.launch {
                            database.alarmDao().toggleAlarmState(alarm.id, isEnabled)
                            if (isEnabled) {
                                scheduleAlarm(alarm)
                            } else {
                                cancelAlarm(alarm.id)
                            }
                        }
                    },
                    onDeleteAlarm = { alarm ->
                        lifecycleScope.launch {
                            cancelAlarm(alarm.id)
                            database.alarmDao().deleteAlarm(alarm)
                        }
                    }
                )

                if (showAddDialog) {
                    AddAlarmDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { newAlarm ->
                            showAddDialog = false
                            lifecycleScope.launch {
                                val id = database.alarmDao().insertAlarm(newAlarm)
                                val savedAlarm = newAlarm.copy(id = id.toInt())
                                scheduleAlarm(savedAlarm)
                                Toast.makeText(this@MainActivity, "آلارم با موفقیت تنظیم شد", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    private fun scheduleAlarm(alarm: AlarmEntity) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_TITLE", alarm.title)
            putExtra("ALARM_DESC", alarm.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (alarm.triggerTimeMillis > System.currentTimeMillis()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarm.triggerTimeMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(alarmId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            alarmId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

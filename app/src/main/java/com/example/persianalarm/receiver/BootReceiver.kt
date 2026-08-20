package com.example.persianalarm.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.persianalarm.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val alarms = db.alarmDao().getAllAlarms().first()
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val now = System.currentTimeMillis()
                for (alarm in alarms) {
                    if (alarm.isEnabled && alarm.triggerTimeMillis > now) {
                        val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                            putExtra("ALARM_ID", alarm.id)
                            putExtra("ALARM_TITLE", alarm.title)
                            putExtra("ALARM_DESC", alarm.description)
                        }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context,
                            alarm.id,
                            alarmIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            alarm.triggerTimeMillis,
                            pendingIntent
                        )
                    }
                }
            }
        }
    }
}

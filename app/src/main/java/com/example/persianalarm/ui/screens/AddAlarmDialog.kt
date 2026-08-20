package com.example.persianalarm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.persianalarm.data.AlarmEntity
import com.example.persianalarm.utils.PersianCalendarHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (AlarmEntity) -> Unit
) {
    val (curYear, curMonth, curDay) = remember { PersianCalendarHelper.getCurrentPersianDate() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("یادآوری") }
    val categories = listOf("یادآوری", "کاری", "شخصی", "جلسه", "دارو", "مناسبت")

    var selectedYear by remember { mutableStateOf(curYear) }
    var selectedMonth by remember { mutableStateOf(curMonth) }
    var selectedDay by remember { mutableStateOf(curDay) }
    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تنظیم آلارم و یادآور جدید",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // عنوان
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان آلارم") },
                    placeholder = { Text("مثال: جلسه کاری، مصرف دارو") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // توضیحات
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("توضیحات و یادداشت (اختیاری)") },
                    placeholder = { Text("جزئیات مربوط به این یادآور را بنویسید...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // انتخاب ساعت و دقیقه
                Text(
                    text = "ساعت: ${String.format("%02d:%02d", selectedHour, selectedMinute)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("ساعت (${selectedHour})", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = selectedHour.toFloat(),
                            onValueChange = { selectedHour = it.toInt() },
                            valueRange = 0f..23f,
                            steps = 23
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("دقیقه (${selectedMinute})", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = selectedMinute.toFloat(),
                            onValueChange = { selectedMinute = it.toInt() },
                            valueRange = 0f..59f,
                            steps = 59
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // انتخاب تاریخ شمسی
                val monthName = PersianCalendarHelper.MONTH_NAMES.getOrElse(selectedMonth - 1) { "" }
                Text(
                    text = "تاریخ: $selectedDay $monthName $selectedYear",
                    style = MaterialTheme.typography.titleMedium
                )

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("روز ($selectedDay)", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = selectedDay.toFloat(),
                        onValueChange = { selectedDay = it.toInt() },
                        valueRange = 1f..31f,
                        steps = 30
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("ماه ($monthName)", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = selectedMonth.toFloat(),
                        onValueChange = { selectedMonth = it.toInt() },
                        valueRange = 1f..12f,
                        steps = 11
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // دسته‌بندی
                Text(
                    text = "دسته‌بندی",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.drop(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // دکمه‌ها
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val triggerMillis = PersianCalendarHelper.jalaliToMillis(
                                selectedYear,
                                selectedMonth,
                                selectedDay,
                                selectedHour,
                                selectedMinute
                            )
                            val finalTitle = if (title.trim().isEmpty()) "یادآور شمسی" else title.trim()
                            val newAlarm = AlarmEntity(
                                title = finalTitle,
                                description = description.trim(),
                                category = selectedCategory,
                                year = selectedYear,
                                month = selectedMonth,
                                day = selectedDay,
                                hour = selectedHour,
                                minute = selectedMinute,
                                triggerTimeMillis = triggerMillis,
                                isEnabled = true
                            )
                            onConfirm(newAlarm)
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("ذخیره آلارم")
                    }
                }
            }
        }
    }
}

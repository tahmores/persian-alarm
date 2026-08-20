package com.example.persianalarm.utils

import java.util.Calendar
import java.util.TimeZone

object PersianCalendarHelper {

    val MONTH_NAMES = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    fun getCurrentPersianDate(): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance(TimeZone.getDefault())
        return gregorianToJalali(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun getRemainingTimeString(triggerMillis: Long): String {
        val diff = triggerMillis - System.currentTimeMillis()
        if (diff <= 0) return "سررسید فرا رسیده"

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days روز و ${hours % 24} ساعت مانده"
            hours > 0 -> "$hours ساعت و ${minutes % 60} دقیقه مانده"
            minutes > 0 -> "$minutes دقیقه مانده"
            else -> "کمتر از یک دقیقه مانده"
        }
    }

    fun jalaliToMillis(jy: Int, jm: Int, jd: Int, hour: Int, minute: Int): Long {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        val cal = Calendar.getInstance(TimeZone.getDefault())
        cal.set(gy, gm - 1, gd, hour, minute, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jyAdjusted = jy - 979
        val jmAdjusted = jm - 1
        val jdAdjusted = jd - 1

        val jDayNo = 365 * jyAdjusted + (jyAdjusted / 33) * 8 + ((jyAdjusted % 33 + 3) / 4) +
                if (jmAdjusted < 6) jmAdjusted * 31 else (jmAdjusted - 6) * 30 + 186 + jdAdjusted

        var gDayNo = jDayNo + 79
        var gy = 1600 + 400 * (gDayNo / 146097)
        gDayNo %= 146097

        var leap = true
        if (gDayNo >= 36525) {
            gDayNo--
            gy += 100 * (gDayNo / 36524)
            gDayNo %= 36524
            if (gDayNo >= 365) gDayNo++ else leap = false
        }

        gy += 4 * (gDayNo / 1461)
        gDayNo %= 1461

        if (gDayNo >= 366) {
            leap = false
            gDayNo--
            gy += gDayNo / 365
            gDayNo %= 365
        }

        val gDaysInMonth = intArrayOf(
            31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
        )

        var gm = 0
        while (gm < 12 && gDayNo >= gDaysInMonth[gm]) {
            gDayNo -= gDaysInMonth[gm]
            gm++
        }
        val gd = gDayNo + 1
        return Triple(gy, gm + 1, gd)
    }

    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gyAdjusted = gy - 1600
        val gmAdjusted = gm - 1
        val gdAdjusted = gd - 1

        var gDayNo = 365 * gyAdjusted + (gyAdjusted + 3) / 4 - (gyAdjusted + 99) / 100 + (gyAdjusted + 399) / 400 + gdAdjusted + gDaysInMonth[gmAdjusted]
        if (gmAdjusted > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo++
        }

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        if (jDayNo < 186) {
            jm = 1 + jDayNo / 31
            jd = 1 + (jDayNo % 31)
        } else {
            jm = 7 + (jDayNo - 186) / 30
            jd = 1 + ((jDayNo - 186) % 30)
        }
        return Triple(jy, jm, jd)
    }
}

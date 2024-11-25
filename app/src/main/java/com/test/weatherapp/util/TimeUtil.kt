package com.test.weatherapp.util

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object TimeUtil {
    /**
     * エポックタイム（ミリ秒）を切り詰めて「00時00分00秒」の値にする。
     */
    fun startTimeOfDay(epochMillis: Long): Long {
        val zoned = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
        return zoned.truncatedTo(ChronoUnit.DAYS).toInstant().toEpochMilli()
    }
}
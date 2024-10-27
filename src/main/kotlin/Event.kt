package org.example

import java.time.LocalTime

data class Event(
    var startTime: LocalTime,
    var endTime: LocalTime,
    val project: String,
    var activity: String,
    var durationMinutes: Long = 0,
    var category: String = ""
)
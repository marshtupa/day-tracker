package org.example

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object Parser {
    private val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

    fun parseDayLogEvents(inputData: String): List<Event> {
        val lines = inputData.lines()
        if (lines.size <= 4) return emptyList()

        val dayStartTime = parseDayStartTime(lines)
        val contentLines = lines.drop(4)
        val events = parseTable(contentLines, dayStartTime)
        categorizeEvents(events)
        return events
    }

    private fun parseDayStartTime(lines: List<String>): LocalTime =
        LocalTime.parse(lines.first().trim(), timeFormatter)

    private fun parseTable(lines: List<String>, dayStartTime: LocalTime): List<Event> {
        val events = mutableListOf<Event>()

        var previousEndTime = dayStartTime
        var currentEvent: Event? = null

        for (line in lines) {
            val columns = parseColumns(line)

            if (isCurrentLineContinueForPreviousRow(columns, currentEvent)) {
                currentEvent?.appendActivityInfo(columns[0])
                continue
            }

            if (isRowValidNewEvent(columns)) {
                currentEvent = createEvent(columns, previousEndTime)
                events.add(currentEvent)
                previousEndTime = currentEvent.endTime
            }
        }

        return events
    }

    private fun isCurrentLineContinueForPreviousRow(columns: List<String>, currentEvent: Event?) =
        columns.size == 1 && currentEvent != null

    private fun isRowValidNewEvent(columns: List<String>): Boolean =
        columns.size >= 2

    private fun createEvent(columns: List<String>, previousEndTime: LocalTime): Event {
        val endTime = LocalTime.parse(columns[0], timeFormatter)
        val activity = if (columns.size == 3) columns[2] else ""

        return Event(
            startTime = previousEndTime,
            endTime = endTime,
            durationMinutes = previousEndTime.until(endTime, ChronoUnit.MINUTES),
            project = columns[1],
            activity = activity.trim()
        )
    }

    private fun categorizeEvents(events: List<Event>) {
        val sideActivities = setOf("подготовка планов на день", "помогал", "ревью", "стендап", "покер")
        val restActivities = setOf("перерыв", "обед")

        events.forEach { event ->
            event.category = when (event.project.lowercase()) {
                in sideActivities -> "Сторонняя активность"
                in restActivities -> "Отдых"
                else -> "Проекты"
            }
        }
    }

    private fun Event.appendActivityInfo(newActivity: String) {
        this.activity = this.activity + ". " + newActivity
    }

    private fun parseColumns(line: String): List<String> {
        return line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
package org.example

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TableParser {
    fun parseMarkdownTable(inputData: String, dayStartTime: LocalTime): List<Event> {
        val lines = inputData.lines()
        if (lines.size <= 2) return emptyList()

        val contentLines = lines.drop(2)
        val events = processContentLines(contentLines, dayStartTime)
        categorizeEvents(events)
        return events
    }

    private fun processContentLines(contentLines: List<String>, dayStartTime: LocalTime): List<Event> {
        val events = mutableListOf<Event>()
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

        var previousEndTime = dayStartTime
        var currentEvent: Event? = null

        for (line in contentLines) {
            val columns = parseColumns(line)
            if (columns.size >= 2) {
                val endTime = LocalTime.parse(columns[0], timeFormatter)
                val project = columns[1]
                val activity = if (columns.size == 3) columns[2] else ""

                currentEvent = Event(
                    startTime = previousEndTime,
                    endTime = endTime,
                    durationMinutes = previousEndTime.until(endTime, ChronoUnit.MINUTES),
                    project = project,
                    activity = activity.trim()
                )

                events.add(currentEvent)
                previousEndTime = endTime
            } else if (columns.size == 1 && currentEvent != null) {
                currentEvent.activity = currentEvent.activity + ". " + columns[0]
            }
        }

        return events
    }

    private fun categorizeEvents(events: List<Event>) {
        val sideActivities = setOf("Подготовка планов на день", "Помогал", "Ревью", "Стендап", "Покер")
        val restActivities = setOf("перерыв", "Обед")

        events.forEach { event ->
            event.category = when (event.project) {
                in sideActivities -> "Сторонняя активность"
                in restActivities -> "Отдых"
                else -> "Проект"
            }
        }
    }

    private fun parseColumns(line: String): List<String> {
        return line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
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
        val currentActivity = StringBuilder()

        for (line in contentLines) {
            val columns = parseColumns(line)
            if (columns.size >= 2) {
                currentEvent?.let {
                    finalizeCurrentEvent(it, currentActivity, previousEndTime)
                    currentActivity.clear()
                    events.add(it)
                }

                val endTime = LocalTime.parse(columns[0], timeFormatter)
                val project = columns[1]
                val activity = if (columns.size == 3) columns[2] else ""

                currentEvent = createNewEvent(previousEndTime, endTime, project, activity)
                previousEndTime = endTime

                if (activity.isNotEmpty()) {
                    currentActivity.append(activity)
                }
            } else if (columns.size == 1 && currentEvent != null) {
                appendToCurrentActivity(currentActivity, columns[0])
            }
        }

        currentEvent?.let { finalizeCurrentEvent(it, currentActivity, previousEndTime) }

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

    private fun createNewEvent(
        previousEndTime: LocalTime,
        endTime: LocalTime,
        project: String,
        activity: String
    ): Event {
        return Event(previousEndTime, endTime, project, activity)
    }

    private fun parseColumns(line: String): List<String> {
        return line.split("|").map { it.trim() }.filter { it.isNotEmpty() }
    }

    private fun appendToCurrentActivity(currentActivity: StringBuilder, additionalText: String) {
        currentActivity.append(". ").append(additionalText)
    }

    private fun finalizeCurrentEvent(
        currentEvent: Event,
        currentActivity: StringBuilder,
        previousEndTime: LocalTime?
    ) {
        currentEvent.durationMinutes = previousEndTime?.until(currentEvent.endTime, ChronoUnit.MINUTES) ?: 0
        currentEvent.activity = currentActivity.toString().trim()
    }
}
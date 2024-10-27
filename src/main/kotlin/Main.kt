package org.example

import java.time.Duration
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun main() {
    val startOfDay = LocalTime.parse(dayStartTime, DateTimeFormatter.ofPattern("H:mm"))
    val events = TableParser.parseMarkdownTable(inputData, startOfDay)
    printDetailedEventsTable(events)

    val summaries = calculateSummaries(events)
    printSummaries(summaries, events, startOfDay)
}

fun calculateSummaries(events: List<Event>): Map<String, Map<String, Pair<Long, String>>> {
    val eventsGroupedByCategory = events.groupBy { it.category }

    return eventsGroupedByCategory.mapValues { (_, categoryEvents) ->
        categoryEvents.groupBy { it.project }.mapValues { (_, projectEvents) ->
            val totalDuration = projectEvents.sumOf { it.durationMinutes }
            val descriptions = projectEvents.joinToString("; ") { it.activity }.trim()
            totalDuration to descriptions
        }
    }
}

fun printDetailedEventsTable(events: List<Event>) {
    println("\n### Детальная таблица событий\n")
    println("| Время начала | Время окончания | Продолжительность (мин) | Категория | Проект / Активность| Описание деятельности                                          |")
    println("|--------------|-----------------|-------------------------|-----------|--------------------|---------------|")
    events.forEach { event ->
        println("| ${event.startTime} | ${event.endTime}| ${event.durationMinutes} | ${event.category} | ${event.project} | ${event.activity} |")
    }
    println("---")
}

fun printSummaries(summaries: Map<String, Map<String, Pair<Long, String>>>, events: List<Event>, workdayStartTime: LocalTime) {
    summaries.forEach { (category, projects) ->
        println("\n### Сводная таблица для категории: $category\n")
        println("| Проект | Временные промежутки | Общее время (мин) | Суммарное описание деятельности |")
        println("|--------|----------------------|-------------------|---------------------------------|")
        projects.forEach { (project, data) ->
            val eventsByProject = events.filter { it.project == project }
            val timeIntervals = eventsByProject.joinToString("; ") { "${it.startTime}-${it.endTime} (${it.durationMinutes} мин)" }
            println("| $project | $timeIntervals | ${data.first} | ${data.second} |")
        }
    }

    val totalWorkTimeMinutes = Duration.between(workdayStartTime, events.last().endTime).toMinutes()
    val totalEventsDuration = events.sumOf { it.durationMinutes }
    println("\n### Общий отчёт по времени\n")
    println("- Общая продолжительность рабочего дня: $totalWorkTimeMinutes минут")
    println("- Суммарная продолжительность всех событий: $totalEventsDuration минут")
}

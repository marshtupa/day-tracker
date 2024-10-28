package org.example

import java.time.Duration

object Printer {
    fun printDetailedEventsTable(events: List<Event>) {
        println("\n### Детальная таблица событий\n")
        println("| Время начала | Время окончания | Продолжительность (мин) | Категория | Проект / Активность| Описание деятельности                                          |")
        println("|--------------|-----------------|-------------------------|-----------|--------------------|---------------|")
        events.forEach { event ->
            println("| ${event.startTime} | ${event.endTime} | ${event.durationMinutes} | ${event.category} | ${event.project} | ${event.activity} |")
        }
    }

    fun printSummaries(daySummary: DaySummary, events: List<Event>) {
        daySummary.summariesByCategory
            .entries
            .sortedByDescending { totalTime(it) }
            .forEach { (category, categorySummary) ->
                println("\n### ${category.name}\n")
                println("| Проект | Временные промежутки | Общее время (мин) | Суммарное описание деятельности |")
                println("|--------|----------------------|-------------------|---------------------------------|")
                categorySummary.summariesByProject.forEach { (project, projectSummary) ->
                    val eventsByProject = events.filter { it.project == project.name }
                    val timeIntervals = eventsByProject.joinToString("; ") { "${it.startTime}-${it.endTime} (${it.durationMinutes} мин)" }
                    println("| ${project.name} | $timeIntervals | ${projectSummary.totalDurationMinutes} | ${projectSummary.activitiesDescription} |")
                }
            }

        printTimeConsumes(daySummary, events)
    }

    private fun printTimeConsumes(daySummary: DaySummary, events: List<Event>) {
        val startTime = events.minOfOrNull { it.startTime } ?: return
        val endTime = events.maxOfOrNull { it.endTime } ?: return
        val totalWorkTimeMinutes = Duration.between(startTime, endTime).toMinutes()
        val totalEventsDuration = events.sumOf { it.durationMinutes }
        if (totalWorkTimeMinutes != totalEventsDuration) {
            throw IllegalStateException("Общая продолжительность рабочего дня не совпадает с суммарной продолжительностью всех событий")
        }

        println("\n### Общий отчёт по времени\n")
        println("- Начал в **$startTime**")
        println("- Закончил в **$endTime**")

        val totalHours = totalWorkTimeMinutes / 60
        val remainingMinutes = totalWorkTimeMinutes % 60
        println("- Общая продолжительность рабочего дня: **$totalHours часов и $remainingMinutes минут** ($totalWorkTimeMinutes минут)")

        daySummary.summariesByCategory
            .entries
            .sortedByDescending { totalTime(it) }
            .forEach { (category, categorySummary) ->
                val totalTimeForCategory = categorySummary.summariesByProject
                .map { it.value }
                .sumOf { it.totalDurationMinutes }
                println("- На **${category.name}** ушло **$totalTimeForCategory минут**")
            }

        val totalTimeForWorkingOnProjects = daySummary.summariesByCategory[Category("Проекты")]!!.summariesByProject
            .map { it.value.totalDurationMinutes }
            .sum()

        val percentageOfWorkTime = (totalTimeForWorkingOnProjects.toDouble() * 100 / totalWorkTimeMinutes).toInt()
        println("- На рабочие проекты ушло: **$totalTimeForWorkingOnProjects минут** = **$percentageOfWorkTime%**")
    }

    private fun totalTime(it: Map.Entry<Category, CategorySummary>) =
        it.value.summariesByProject.values.sumOf { it.totalDurationMinutes }
}
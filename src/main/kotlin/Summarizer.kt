package org.example


data class Project(
    val name: String
)

data class ProjectSummary(
    val totalDurationMinutes: Long,
    val activitiesDescription: String
)

data class Category(
    val name: String
)

data class CategorySummary(
    val summariesByProject: Map<Project, ProjectSummary>
)

data class DaySummary(
    val summariesByCategory: Map<Category, CategorySummary>
)

object Summarizer {
    fun calculateSummaries(events: List<Event>): DaySummary {
        val eventsGroupedByCategory = events.groupBy { it.category }

        val summariesByCategory = eventsGroupedByCategory
            .map { (category, categoryEvents) ->
                val summariesByProject = categoryEvents
                    .groupBy { it.project }
                    .map { (project, projectEvents) ->
                        val totalDuration = projectEvents.sumOf { it.durationMinutes }
                        val descriptions = projectEvents.joinToString("; ") { it.activity }.trim()
                        Project(project) to ProjectSummary(totalDuration, descriptions)
                    }.toMap()

                Category(category) to CategorySummary(summariesByProject)
            }.toMap()
        return DaySummary(summariesByCategory)
    }
}
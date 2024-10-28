package org.example

import org.example.Printer.printDetailedEventsTable
import org.example.Printer.printSummaries
import org.example.Summarizer.calculateSummaries
import org.example.Parser.parseDayLogEvents

fun main() {
    val events = parseDayLogEvents(inputData)
    if (false) {
        printDetailedEventsTable(events)
    }

    val summaries = calculateSummaries(events)
    printSummaries(summaries, events)
}

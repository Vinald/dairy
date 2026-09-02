package vinald.me.dairy.ui

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val fullDate: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

private val mediumDate: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

fun LocalDate.formatFull(): String = format(fullDate)

fun LocalDate.formatMedium(): String = format(mediumDate)

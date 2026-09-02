package vinald.me.dairy.data

/** Mood attached to a diary entry, stored in the database as [level] (1..5). */
enum class Mood(val level: Int, val emoji: String, val label: String) {
    AWFUL(1, "😢", "Awful"),
    BAD(2, "🙁", "Bad"),
    OKAY(3, "😐", "Okay"),
    GOOD(4, "🙂", "Good"),
    GREAT(5, "😄", "Great");

    companion object {
        fun fromLevel(level: Int): Mood = entries.firstOrNull { it.level == level } ?: OKAY
    }
}

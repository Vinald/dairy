package vinald.me.dairy.ui

import kotlinx.serialization.Serializable

/** Type-safe navigation destinations. */
sealed interface Route {

    @Serializable
    data object Lock : Route

    @Serializable
    data object PinSetup : Route

    @Serializable
    data object EntryList : Route

    @Serializable
    data object Calendar : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data class EntryDetail(val id: Long) : Route

    /** [id] null means "create a new entry". */
    @Serializable
    data class EntryEditor(val id: Long? = null) : Route
}

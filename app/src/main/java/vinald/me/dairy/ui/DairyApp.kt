package vinald.me.dairy.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import vinald.me.dairy.ui.calendar.CalendarScreen
import vinald.me.dairy.ui.entries.EntryDetailScreen
import vinald.me.dairy.ui.entries.EntryEditorScreen
import vinald.me.dairy.ui.entries.EntryListScreen
import vinald.me.dairy.ui.settings.SettingsScreen
import kotlin.reflect.KClass

private data class TopLevelDestination(
    val route: Route,
    val routeClass: KClass<out Route>,
    val label: String,
    val icon: ImageVector,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Route.EntryList, Route.EntryList::class, "Diary", Icons.AutoMirrored.Filled.MenuBook),
    TopLevelDestination(Route.Calendar, Route.Calendar::class, "Calendar", Icons.Default.CalendarMonth),
    TopLevelDestination(Route.Settings, Route.Settings::class, "Settings", Icons.Default.Settings),
)

@Composable
fun DairyApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showBottomBar = topLevelDestinations.any { dest ->
        currentDestination?.hierarchy?.any { it.hasRoute(dest.routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.hasRoute(dest.routeClass) } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.EntryList,
            modifier = Modifier.padding(padding),
        ) {
            composable<Route.EntryList> {
                EntryListScreen(
                    onCreateEntry = { navController.navigate(Route.EntryEditor()) },
                    onOpenEntry = { id -> navController.navigate(Route.EntryDetail(id)) },
                )
            }
            composable<Route.Calendar> {
                CalendarScreen(
                    onOpenEntry = { id -> navController.navigate(Route.EntryDetail(id)) },
                )
            }
            composable<Route.Settings> {
                SettingsScreen()
            }
            composable<Route.EntryDetail> { entry ->
                val args = entry.toRoute<Route.EntryDetail>()
                EntryDetailScreen(
                    entryId = args.id,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Route.EntryEditor(id)) },
                )
            }
            composable<Route.EntryEditor> { entry ->
                val args = entry.toRoute<Route.EntryEditor>()
                EntryEditorScreen(
                    entryId = args.id,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}

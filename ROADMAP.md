# Diary App — Learning Roadmap

A step-by-step plan for rebuilding this app **by hand, without AI**, to learn Kotlin and
modern Android. Each phase mirrors one or two commits in this repo's history, so the
existing code is your answer key — but try each phase yourself *first*, then compare.

## How to use this roadmap

1. Read the **Goal** and **Concepts** for a phase.
2. For each new Kotlin/Android concept, read the official docs or a tutorial until you
   can explain it in a sentence. Don't skip this — the concepts are the point.
3. Write the code yourself. Compile often. Expect red squiggles; fixing them is learning.
4. Only when you're stuck or done, open the reference file(s) in this repo and diff your
   version against it. Ask *why* it differs.
5. Hit the **Checkpoint** (a thing you can see working) before moving on.

To see how the original was built incrementally:

```
git log --oneline --reverse
git show <commit>          # see exactly what one step added
```

Build the replica in a **separate project folder** (e.g. `~/code/projects/diary-replica`)
so this repo stays a clean reference.

---

## The app in one paragraph

A private offline journal. You write dated entries with a title, body, a mood (1–5 emoji),
and optional photos. Entries show in a searchable list and on a month calendar. An
optional PIN + biometric lock protects the app, and diary content is hidden from
screenshots and the recents screen while locked. Settings toggle dynamic theme colours
and manage the lock.

## Architecture (the shape you're aiming for)

```
UI (Compose screens)  ──►  ViewModel (state + events)  ──►  Repository  ──►  Room DAO / DataStore / file storage
        ▲                          │
        └──────── StateFlow ◄──────┘
```

- **Single-module** Android app, package `vinald.me.dairy` (pick your own).
- **Manual dependency injection**: one `AppContainer` built in `Application.onCreate()`,
  no Hilt/Dagger.
- **MVVM**: each screen has a `@Composable` + a `ViewModel` exposing `StateFlow`.
- **Room** for entries/photos, **DataStore Preferences** for settings and the PIN hash.
- **Type-safe Navigation Compose** with a sealed `Route` interface.
- Photos are copied into `filesDir/photos` and referenced by filename in the DB.

---

## Phase 0 — Kotlin fundamentals (no Android yet)

**Goal:** be comfortable with the language before fighting the framework too.

**Concepts:**
- `val` vs `var`, type inference, basic types, string templates
- Null safety: `?`, `?:` (Elvis), `?.let { }`, `!!` (and why to avoid it)
- Functions, default & named arguments, single-expression functions
- Lambdas, higher-order functions, trailing-lambda syntax
- `data class`, `copy()`, destructuring
- `enum class` with properties and methods (you'll build `Mood` like this)
- `sealed interface` / `sealed class` (you'll build `Route` like this)
- `object` declarations (singletons), `companion object`
- Extension functions
- Collections: `map`, `filter`, `firstOrNull`, `minByOrNull`, `mapIndexed`, `sortedBy`
- Scope functions: `let`, `apply`, `also`, `run`, `with`, and `use` (for closeables)
- `by lazy` and delegated properties

**Checkpoint:** in a plain Kotlin file (or [play.kotlinlang.org](https://play.kotlinlang.org)),
write a `Mood` enum with `level`, `emoji`, `label` and a `fromLevel(Int): Mood` on its
companion object. Then compare with `app/.../data/Mood.kt`.

**Do not** move on until data classes, nullables, lambdas, and enums feel natural.

---

## Phase 1 — Project scaffold

Ref commit: `initial compose project scaffold`

**Goal:** an empty Compose app that launches to "Hello".

**Concepts:**
- Android Studio, the Gradle wrapper, `./gradlew assembleDebug`
- **Gradle version catalog** (`gradle/libs.versions.toml`) — `[versions]`, `[libraries]`,
  `[plugins]`, and `libs.` references in build files
- `build.gradle.kts` (project) vs `app/build.gradle.kts` (module)
- `AndroidManifest.xml`, `Application`, `Activity`, the `<intent-filter>` launcher entry
- Jetpack Compose basics: `setContent { }`, `@Composable`, `MaterialTheme`, `Surface`,
  `Text`, `@Preview`
- `compileSdk` / `minSdk` / `targetSdk` (this app: min 26, so `java.time` is available)
- `enableEdgeToEdge()`

**Build:**
- New project → "Empty Activity" (Compose) template. This gives you `MainActivity`,
  `ui/theme/{Color,Theme,Type}.kt`, manifest, resources — same as this repo's first commit.
- Set the app name in `res/values/strings.xml`.
- Skim `app/build.gradle.kts` here and note which plugins are applied
  (`android.application`, `kotlin.compose`, and later `ksp`, `kotlin.serialization`).

**Checkpoint:** app installs and shows a greeting on a device/emulator.

**Reference:** `app/build.gradle.kts`, `gradle/libs.versions.toml`, `MainActivity.kt`,
`ui/theme/*`.

---

## Phase 2 — The data layer: Room

Ref commit: `add room database, entry entity, dao and type converters`

**Goal:** a database that stores entries and photos, verified by tests.

**Concepts:**
- **Room**: `@Entity`, `@PrimaryKey(autoGenerate = true)`, `@Dao`, `@Database`
- **KSP** (Kotlin Symbol Processing) — the `ksp(...)` dependency for `room-compiler`
- `@Query`, `@Insert`, `@Upsert`, `@Delete`, `@Transaction`
- Relations: `@Embedded` + `@Relation` (one entry → many photos), `@ForeignKey` with
  `onDelete = CASCADE`, `@Index`
- **`@TypeConverters`** — Room can't store `LocalDate` / `Instant`, so you convert to
  `Long` (epoch day / epoch millis)
- Returning `Flow<List<T>>` from queries = the DB pushes updates to the UI automatically
- `suspend` functions for writes (they touch disk, so they must be off the main thread)
- `exportSchema = true` and the generated `schemas/*.json` (used for migration tests)
- Instrumented tests (`androidTest`), `Room.inMemoryDatabaseBuilder`, `runBlocking`

**Build, in order:**
1. `data/entity/DiaryEntry.kt` — `id, title, body, moodLevel, entryDate, createdAt, updatedAt`
2. `data/entity/EntryPhoto.kt` — `id, entryId, fileName, position` + foreign key
3. `data/entity/EntryWithPhotos.kt` — the `@Embedded`/`@Relation` pair
4. `data/Mood.kt` — the enum from Phase 0
5. `data/Converters.kt` — `LocalDate`/`Instant` ↔ `Long`
6. `data/DiaryDao.kt` — start with just `observeEntries()` and `upsert()`, add the rest
   (`search`, `observeEntriesBetween`, `observeEntryDates`, photo ops, `delete`) as later
   phases need them
7. `data/DiaryDatabase.kt` — `@Database`, `abstract fun diaryDao()`, a `build(context)`
   in the companion object
8. Add `room.schemaLocation` to `ksp { arg(...) }` in `app/build.gradle.kts`

**Checkpoint:** port `app/src/androidTest/.../data/DiaryDaoTest.kt` and make all five
tests pass (`upsert/observe`, `search`, `entriesBetween`, cascade delete). This is your
proof the data layer works before any UI exists.

**Reference:** everything under `app/src/main/java/.../data/` except `AppPreferences`,
`PhotoStorage`, `DiaryRepository`; plus `DiaryDaoTest.kt`.

---

## Phase 3 — Repository + manual DI

Ref commit: `add repository and app container dependency injection`

**Goal:** one clean entry point to data, wired up through a hand-built container.

**Concepts:**
- The **repository pattern** — the UI never touches the DAO directly
- Custom `Application` subclass, registered via `android:name` in the manifest
- **Manual DI**: `AppContainer` holds singletons, created once in `onCreate()`
- `by lazy` so each dependency is built on first use
- Constructor injection (pass dependencies in, don't reach for globals)

**Build:**
1. `data/DiaryRepository.kt` — wraps the DAO. Start with `entries()`, `entry(id)`,
   `save(entry)`, `delete(entry)`. Note `search()` returns all entries when the query is blank.
2. `di/AppContainer.kt` — `database by lazy { ... }`, `diaryRepository by lazy { ... }`
3. `DiaryApplication.kt` — `lateinit var container`, set it in `onCreate()`
4. Register `.DiaryApplication` in `AndroidManifest.xml`

**Checkpoint:** app still launches (the container builds without crashing). Add a
throwaway `LaunchedEffect` in `MainActivity` that inserts one entry and logs
`repository.entries().first()` — confirm it round-trips, then delete that code.

**Reference:** `data/DiaryRepository.kt`, `di/AppContainer.kt`, `DiaryApplication.kt`.

---

## Phase 4 — Navigation shell

Ref commit: `add navigation host and screen routes`

**Goal:** a bottom nav bar (Diary / Calendar / Settings) and empty destination screens.

**Concepts:**
- **Navigation Compose, type-safe**: `@Serializable` route classes, `NavHost`,
  `composable<Route.X> { }`, `navController.navigate(Route.X)`, `toRoute<>()` for args
- The `kotlin.serialization` plugin + `kotlinx-serialization-json` dependency
- `sealed interface Route` with `data object` (no args) and `data class` (with args, e.g.
  `EntryDetail(val id: Long)`, `EntryEditor(val id: Long? = null)`)
- `Scaffold`, `NavigationBar` / `NavigationBarItem`
- Back stack behaviour: `popUpTo`, `launchSingleTop`, `saveState`/`restoreState`
- Deciding when to show the bottom bar (only on top-level destinations)
- Hoisting navigation: screens take `onOpenEntry: (Long) -> Unit` lambdas, they don't
  know about `NavController`

**Build:**
1. `ui/Route.kt` — all seven routes
2. Add the `navigation-compose` and serialization deps/plugin
3. `ui/DairyApp.kt` — `NavHost` with a `composable` block per route; stub screens can be
   just `Text("Calendar")` etc.
4. Move `DairyApp()` into `MainActivity`'s `setContent`

**Checkpoint:** you can tap between the three tabs; tapping a stub "entry" navigates to a
detail stub and back.

**Reference:** `ui/Route.kt`, `ui/DairyApp.kt`, `MainActivity.kt`.

---

## Phase 5 — Entry list + search

Ref commit: `add entry list screen with search` (and later `add calendar month view`
splits `EntryRow` into its own file — you can do that split now)

**Goal:** the Diary tab shows real entries from the DB, with a working search box.

**Concepts:**
- **ViewModel**: `androidx.lifecycle.ViewModel`, `viewModelScope`
- **ViewModel factories** without Hilt: `viewModelFactory { initializer { } }` and the
  `CreationExtras.appContainer` extension (`APPLICATION_KEY` → your `Application`)
- **StateFlow**: `MutableStateFlow`, `asStateFlow()`, a `data class XUiState`
- Flow operators: `map`, `stateIn(scope, SharingStarted.WhileSubscribed(5000), initial)`
- `flatMapLatest` (swap to a new DB query when the search text changes) and `debounce`
- `collectAsStateWithLifecycle()` in the composable
- Compose lists: `LazyColumn`, `items(list, key = { it.id })`
- `OutlinedTextField` with leading/trailing icons, `KeyboardOptions(imeAction = Search)`
- `FloatingActionButton`, `TopAppBar` (`@OptIn(ExperimentalMaterial3Api::class)`)
- Empty / loading / content state handling with `when`
- Extension function `EntryWithPhotos.toListItem(...)` mapping DB model → UI model
- `ui/DateFormat.kt`: `DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM/FULL)` as
  `LocalDate` extensions

**Build:**
1. `ui/ViewModelExt.kt`, `ui/DateFormat.kt`
2. `ui/entries/EntryRow.kt` — `EntryListItem` data class, `toListItem`, `EntryRow` composable (`ListItem`)
3. `ui/entries/EntryListViewModel.kt` — `_query` StateFlow → debounce → `flatMapLatest` →
   `repository.search` → map to `EntryListUiState`
4. `ui/entries/EntryListScreen.kt` — search field + `LazyColumn` + empty states
5. Wire `onCreateEntry` / `onOpenEntry` in `DairyApp.kt`

**Checkpoint:** entries you inserted in tests (or via a temporary insert button) appear;
typing in the search box filters them live.

**Reference:** `ui/entries/{EntryListScreen,EntryListViewModel,EntryRow}.kt`,
`ui/{ViewModelExt,DateFormat}.kt`.

---

## Phase 6 — Entry detail

Ref commit: `add entry detail screen`

**Goal:** tap an entry → full-screen read view with edit/delete actions.

**Concepts:**
- Passing a nav argument into a ViewModel: a `factory(entryId: Long)` function
- `repository.entry(id): Flow<EntryWithPhotos?>` → nullable `StateFlow`
- Rendering a nullable state safely (`val current = entry ?: return@Scaffold`)
- `TopAppBar` `navigationIcon` + `actions` (edit, delete `IconButton`s)
- `AlertDialog` for delete confirmation, driven by
  `var showDialog by remember { mutableStateOf(false) }`
- `Modifier.verticalScroll(rememberScrollState())`
- Callback-after-work pattern: `fun delete(onDeleted: () -> Unit)`

**Build:**
1. `ui/entries/EntryDetailViewModel.kt`
2. `ui/entries/EntryDetailScreen.kt`
3. Wire `onEdit` / `onBack` in `DairyApp.kt`

**Checkpoint:** open an entry, see its date/mood/title/body; delete asks for confirmation
and returns you to the list with the entry gone.

**Reference:** `ui/entries/{EntryDetailScreen,EntryDetailViewModel}.kt`.

---

## Phase 7 — Create / edit entry

Ref commits: `add create and edit entry screen with mood picker`,
`fix mood picker layout to show all five moods`

**Goal:** one editor screen used for both "new" and "edit" (`entryId: Long?`).

**Concepts:**
- A single screen handling create vs edit based on a nullable id
- A rich `data class EditorState` with a computed `val canSave: Boolean get() = ...`
- `_state.update { it.copy(...) }` for every field change (`onTitleChange`, `onMoodChange`, …)
- `init { }` block: load the existing entry with `repository.entry(id).first()`
- Preserving `createdAt` on edit, setting `updatedAt = Instant.now()` always
- Custom radio-group UI: `Row` of `Surface` cells, `Modifier.weight(1f).aspectRatio(1f)`,
  `Modifier.selectable(selected, role = Role.RadioButton, onClick)`
- Material3 `DatePickerDialog` + `rememberDatePickerState`; converting
  `selectedDateMillis` ↔ `LocalDate` via `Instant` + `ZoneOffset.UTC`
- `AssistChip` for the date button
- Save-then-navigate: `fun save(onSaved: (Long) -> Unit)`

**Build:**
1. `ui/entries/EntryEditorViewModel.kt` — `EditorState`, all `onXChange`, `save()`
2. `ui/entries/EntryEditorScreen.kt` — date chip, `MoodPicker`, title/body fields, top-bar
   save/discard actions
3. Wire it as both `Route.EntryEditor()` (new) and `Route.EntryEditor(id)` (edit)

**Checkpoint:** create a new entry from the FAB; edit an existing one; the mood picker
shows all five and remembers the selection; the date picker changes the entry date.

**Reference:** `ui/entries/{EntryEditorScreen,EntryEditorViewModel}.kt`.

---

## Phase 8 — Photo attachments

Ref commit: `add photo attachments to entries`

**Goal:** attach one or more photos to an entry; show them on the row, detail, and editor.

**Concepts:**
- **File storage**: `File(context.filesDir, "photos")`, `mkdirs()`, copying an input
  stream with `.use { }` (auto-close) and `input.copyTo(output)`
- `UUID.randomUUID()` filenames
- `withContext(Dispatchers.IO)` for file work
- **Activity Result API in Compose**: `rememberLauncherForActivityResult` +
  `ActivityResultContracts.PickMultipleVisualMedia`, `PickVisualMediaRequest`
- **Coil 3** (`coil-compose`): `AsyncImage(model = File(...) / Uri, ...)`,
  `ContentScale.Crop`
- Modelling pending vs saved photos: `existingPhotos: List<EntryPhoto>` +
  `newPhotos: List<Uri>`, plus a `removedPhotos` buffer applied on save
- Photo ordering with a `position` column; new photos appended after the current count
- Repository coordinates DB + disk: `addPhotos`, `removePhoto` (delete row *and* file),
  and `delete(entry)` cleans up all photo files before the cascade

**Build:**
1. `data/PhotoStorage.kt` — `import(uri): String`, `fileFor(name): File`, `delete(name)`
2. Extend `DiaryRepository` — `photoFile`, `addPhotos`, `removePhoto`, photo-aware `delete`
3. Extend `AppContainer` to construct `PhotoStorage` and pass it to the repository
4. Extend `DiaryDao` — `insertPhotos`, `deletePhoto`, `photosFor`
5. `ui/PhotoThumbnails.kt` — `PhotoStrip(photos: List<Any>, onRemove: ((Any) -> Unit)?)`
   (a `LazyRow` of `AsyncImage`; `Any` so it takes both `File` and `Uri`)
6. Wire into editor (picker button + removable strip), detail (read-only strip), and
   `EntryRow` (thumbnail of the first photo)
7. Add the `coil` dependency

**Checkpoint:** add two photos to an entry, remove one before saving, save; reopen and
see the remaining photo; the list row shows a thumbnail; deleting the entry removes the
files from `filesDir/photos` (check with Device Explorer).

**Reference:** `data/PhotoStorage.kt`, `ui/PhotoThumbnails.kt`, the photo bits of
`DiaryRepository`, `EntryEditor*`, `EntryDetail*`, `EntryRow.kt`.

---

## Phase 9 — Calendar month view

Ref commit: `add calendar month view`

**Goal:** the Calendar tab: a month grid with dots on days that have entries, tap a day to
list its entries.

**Concepts:**
- `java.time`: `YearMonth`, `LocalDate`, `DayOfWeek`, `WeekFields.of(locale).firstDayOfWeek`,
  `month.atDay(1)`, `lengthOfMonth()`, `getDisplayName(TextStyle.FULL, locale)`
- Grid maths: leading blanks =
  `(firstOfMonth.dayOfWeek.value - firstDayOfWeek.value + 7) % 7`; rows = `ceil(cells/7)`
- Building a calendar grid with nested `Row`/`Column` + `Modifier.weight(1f).aspectRatio(1f)`
  (no `LazyVerticalGrid` needed)
- **`combine`** of multiple flows (`month`, `selectedDate`, `markedDays`,
  `entriesForSelectedDay`) into one `CalendarUiState`
- `flatMapLatest` again: re-query when the visible month or selected day changes
- New DAO queries: `observeEntryDates(start, end): Flow<List<LocalDate>>`,
  `observeEntriesBetween(start, end)`
- Reusing `EntryRow` with `showDate = false`

**Build:**
1. Add `observeEntryDates` / `observeEntriesBetween` to the DAO + repository
2. `ui/calendar/CalendarViewModel.kt` — `month` & `selectedDate` MutableStateFlows,
   `combine(...)`, `showPreviousMonth/showNextMonth/selectDate`
3. `ui/calendar/CalendarScreen.kt` — header with prev/next, weekday labels, `MonthGrid`,
   `DayCell` (selected / today / has-entry states), divider, entry list below

**Checkpoint:** days with entries show a dot; tapping a day lists that day's entries and
tapping one opens it; prev/next month navigation works.

**Reference:** `ui/calendar/{CalendarScreen,CalendarViewModel}.kt`.

---

## Phase 10 — App lock: PIN

Ref commit: `add pin setup and lock screen`

**Goal:** optional 4-digit PIN. When set, the app opens to a lock screen and re-locks
whenever it goes to the background.

**Concepts:**
- **PBKDF2 hashing** with `javax.crypto`: `SecretKeyFactory("PBKDF2WithHmacSHA256")`,
  `PBEKeySpec(pin, salt, iterations, keyLength)`, `SecureRandom` salt — never store the
  raw PIN
- Constant-time comparison (XOR-accumulate, no early return) — why timing matters
- `object PinHasher` — pure Kotlin, no Android imports, so it's unit-testable on the JVM
- **DataStore Preferences**: `preferencesDataStore(name)`, `stringPreferencesKey`,
  `dataStore.data.map { it[key] }`, `dataStore.edit { }`; storing bytes as Base64
- **`ProcessLifecycleOwner`** + `DefaultLifecycleObserver.onStop` to detect
  app-to-background
- A `LockManager` holding `isLocked: StateFlow<Boolean>`, started from `Application.onCreate()`
- `DairyApp()` shows `LockScreen()` and returns early when `isLocked`
- Two-stage PIN setup state machine (`ENTER` → `CONFIRM`, mismatch resets)
- Building a custom numeric keypad in Compose (`PinPad`, `PinDots`)
- `onPinJustSet()` so creating a PIN doesn't immediately lock you out

**Build:**
1. `security/PinHasher.kt` (+ port `PinHasherTest.kt` — pure JVM test, fast)
2. `security/PinManager.kt` — `hasPin`, `setPin`, `verify`, `clearPin`, `biometricEnabled`
3. `security/LockManager.kt` — `isLocked`, `start()`, `unlock()`, `onPinJustSet()`, `onStop`
4. Add `datastore-preferences` and `lifecycle-process` deps
5. Construct both in `AppContainer`; call `container.lockManager.start()` in `onCreate()`
6. `ui/lock/PinPad.kt` (`PinDots`, `PinPad`, `PIN_LENGTH = 4`)
7. `ui/lock/{LockViewModel,LockScreen}.kt`
8. `ui/lock/{PinSetupViewModel,PinSetupScreen}.kt`
9. In `DairyApp.kt`: early-return `LockScreen()` when locked; add `PinSetup` route
10. Temporary: a button somewhere to open `PinSetup` (real entry point comes in Phase 13)

**Checkpoint:** `PinHasherTest` passes; set a PIN, background the app, reopen → lock
screen; correct PIN unlocks, wrong PIN shows an error and clears.

**Reference:** `security/{PinHasher,PinManager,LockManager}.kt`,
`ui/lock/{PinPad,LockScreen,LockViewModel,PinSetupScreen,PinSetupViewModel}.kt`.

---

## Phase 11 — App lock: biometrics

Ref commit: `add biometric unlock`

**Goal:** if enabled, offer fingerprint/face unlock on the lock screen, falling back to PIN.

**Concepts:**
- `androidx.biometric`: `BiometricManager.canAuthenticate(BIOMETRIC_WEAK)`,
  `BiometricPrompt`, `PromptInfo.Builder`, `AuthenticationCallback`
- **Why `FragmentActivity`** (not `ComponentActivity`) — `BiometricPrompt` needs it;
  change `MainActivity`'s superclass
- `ContextCompat.getMainExecutor`
- `object BiometricAuth` wrapping the prompt with `onSuccess` / `onFallback` lambdas
- Storing the "biometric enabled" flag in `PinManager` (DataStore boolean)
- `LaunchedEffect(canUseBiometric)` to auto-show the prompt when the lock screen appears
- `USE_BIOMETRIC` permission in the manifest

**Build:**
1. Add `biometric-ktx` dep + `USE_BIOMETRIC` permission
2. Change `MainActivity : FragmentActivity`
3. `security/BiometricAuth.kt`
4. `PinManager`: `biometricEnabled` flow + `setBiometricEnabled`
5. `LockViewModel` / `LockScreen`: expose `biometricEnabled`, show the fingerprint key on
   `PinPad`, auto-prompt via `LaunchedEffect`

**Checkpoint:** on a device with biometrics enrolled, enabling the flag (temporarily
hard-code it or use the settings screen from Phase 13) shows the system biometric sheet on
the lock screen; "Use PIN" falls back to the keypad.

**Reference:** `security/BiometricAuth.kt`, `MainActivity.kt`, lock screen files.

---

## Phase 12 — Hide content when locked

Ref commit: `hide diary content from screenshots and recents when locked`

**Goal:** when a PIN is set, block screenshots and blank the recents/app-switcher preview.

**Concepts:**
- `WindowManager.LayoutParams.FLAG_SECURE` — `window.addFlags` / `clearFlags`
- Collecting a Flow tied to lifecycle in an `Activity`:
  `pinManager.hasPin.flowWithLifecycle(lifecycle, STARTED).onEach { }.launchIn(lifecycleScope)`
- Doing this in `MainActivity.onCreate`, reacting to `hasPin` changes live

**Build:** add the `hasPin` collector to `MainActivity.onCreate` that toggles `FLAG_SECURE`.

**Checkpoint:** with a PIN set, trying to screenshot fails ("can't take screenshot due to
security policy") and the recents thumbnail is blank; with no PIN, screenshots work
normally.

**Reference:** `MainActivity.kt`.

---

## Phase 13 — Settings screen

Ref commit: `add settings screen with theme and lock options`

**Goal:** the Settings tab — dynamic colours toggle, and the real entry point for
set/change/remove PIN and the biometric toggle.

**Concepts:**
- A second DataStore (`AppPreferences`, name `"app_preferences"`) for display prefs
- `dynamicColor: Flow<Boolean>` read in `MainActivity` with
  `collectAsStateWithLifecycle(initialValue = true)` and passed to `DairyTheme`
- **Material 3 dynamic colour**: `dynamicLightColorScheme` / `dynamicDarkColorScheme`
  (Android 12+), fallback to your hand-picked schemes; `isSystemInDarkTheme()`
- `Material3` `ListItem` with `headlineContent` / `supportingContent` / `leadingContent` /
  `trailingContent`, `Switch`
- Conditional rows: show "Set up app lock" vs "Change PIN / biometrics / Remove lock"
  based on `hasPin`
- `SettingsViewModel` exposing three `StateFlow`s and simple `viewModelScope.launch` setters

**Build:**
1. `data/AppPreferences.kt` — `dynamicColor` flow + setter
2. Add `appPreferences` to `AppContainer`
3. Read `dynamicColor` in `MainActivity`, thread it through `DairyTheme`
4. `ui/settings/{SettingsViewModel,SettingsScreen}.kt`
5. Point the Settings tab at the real screen; `onOpenPinSetup → Route.PinSetup`
6. Remove any temporary lock/dynamic-colour test buttons from earlier phases

**Checkpoint:** toggle dynamic colours and see the palette change (on Android 12+, against
your wallpaper); set a PIN from Settings; the rows switch to change/remove; toggle
biometrics; remove the lock and confirm the app opens without a lock screen.

**Reference:** `data/AppPreferences.kt`, `ui/settings/*`, `MainActivity.kt`,
`ui/theme/Theme.kt`.

---

## Phase 14 — Polish

Ref commit: `polish app name and show photo thumbnail in entry list`

**Goal:** name, icon, and the small touches.

**Concepts:**
- `res/values/strings.xml` app name, launcher label
- Adaptive launcher icons (`mipmap-anydpi/ic_launcher.xml`), Image Asset Studio
- `android:allowBackup="false"`, `dataExtractionRules`, `backupRules` — a private diary
  shouldn't sync to cloud backup
- `android:windowSoftInputMode="adjustResize"` so the keyboard doesn't cover the editor
- Final pass: empty states, content descriptions (accessibility), consistent spacing

**Checkpoint:** the app has a real name and icon in the launcher; entry rows show photo
thumbnails; no leftover debug code.

**Reference:** `AndroidManifest.xml`, `res/`, `EntryRow.kt`.

---

## After the replica

Once it matches, extend it with things this app *doesn't* do — you'll learn more from
these than from the copy:

- **Room migration**: add a `tags` column, write a `Migration(1, 2)`, keep the schema test green
- Export / import entries as JSON (you already have `kotlinx.serialization`)
- Full-screen photo viewer with pinch-zoom
- "On this day" — entries from the same date in past years
- Widget or a daily write reminder (`WorkManager` + notifications)
- Move manual DI to **Hilt** and compare
- UI tests with `createComposeRule()`
- Encrypt the database with SQLCipher, keyed by the PIN

---

## Concept index (where each idea first appears)

| Concept | Phase |
|---|---|
| Data classes, enums, sealed types, scope functions | 0 |
| Gradle version catalog, Compose basics | 1 |
| Room, KSP, TypeConverters, Flow queries, `@Relation` | 2 |
| Repository pattern, manual DI, custom `Application` | 3 |
| Type-safe Navigation Compose, state hoisting | 4 |
| ViewModel + factory, `StateFlow`, `stateIn`, `flatMapLatest`, `debounce`, `LazyColumn` | 5 |
| Nullable state, `AlertDialog`, `remember`/`mutableStateOf` | 6 |
| `data class` UI state + `copy`, `DatePickerDialog`, custom `selectable` group | 7 |
| File I/O, `.use`, `Dispatchers.IO`, Activity Result API, Coil | 8 |
| `java.time` calendar maths, `combine` of flows | 9 |
| PBKDF2, `object` singletons, DataStore, `ProcessLifecycleOwner` | 10 |
| BiometricPrompt, `FragmentActivity`, `LaunchedEffect` | 11 |
| `FLAG_SECURE`, `flowWithLifecycle` | 12 |
| Dynamic colour, dark theme, Material3 `ListItem`/`Switch` | 13 |
| Adaptive icons, backup rules | 14 |

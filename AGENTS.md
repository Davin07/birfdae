# AGENTS.md

## Build/Lint/Test Commands

### Build Commands
- **Full build**: `./gradlew build` (includes compilation, tests, and linting)
  - On Windows: `./gradlew.bat build`
- **Assemble only**: `./gradlew assemble` (compiles without running tests)
  - On Windows: `./gradlew.bat assemble`
- **Clean build**: `./gradlew clean build`
  - On Windows: `./gradlew.bat clean build`

### Lint Commands
- **Kotlin linting**: `./gradlew ktlintCheck`
- **Auto-fix Kotlin lint**: `./gradlew ktlintFormat`
- **Android linting**: `./gradlew lint` (or `lintDebug`/`lintRelease` for specific variants)

### Test Commands
- **All unit tests**: `./gradlew test`
- **Debug unit tests**: `./gradlew testDebugUnitTest`
- **Release unit tests**: `./gradlew testReleaseUnitTest`
- **Single unit test class**: `./gradlew test --tests "com.birthdayreminder.domain.usecase.AddBirthdayUseCaseSimpleTest"`
- **Single test method**: `./gradlew test --tests "com.birthdayreminder.domain.usecase.AddBirthdayUseCaseSimpleTest.methodName"`
- **Android instrumentation tests**: `./gradlew connectedAndroidTest` (requires device/emulator)
- **Specific instrumented test**: `./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.birthdayreminder.ui.screens.BirthdayListScreenTest`

## Code Style Guidelines

### Formatting (.editorconfig)
- **Indentation**: 4 spaces
- **Max line length**: 120 characters
- **Final newlines**: Required at end of files
- **Trailing commas**: Allowed and preferred for multi-line constructs
- **Kotlin style**: Official Kotlin style (`kotlin.code.style=official`)

### Naming Conventions
- **Classes/Interfaces**: PascalCase (e.g., `AddBirthdayUseCase`, `BirthdayRepository`)
- **Functions/Properties**: camelCase (e.g., `addBirthday()`, `birthdayId`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_NAME_LENGTH`, `ERROR_NAME_REQUIRED`)
- **Composable functions**: PascalCase (e.g., `BirthdayCard`, `DatePickerField`)
- **Sealed classes/data classes**: PascalCase with PascalCase subtypes (e.g., `AddBirthdayResult.Success`)

### Architecture Patterns
- **Clean Architecture**: Strict separation between Domain/Data/UI layers
- **Dependency injection**: Hilt with constructor injection (`@Inject`), use `@Singleton` for singletons
- **ViewModels**: Annotate with `@HiltViewModel`, use `viewModelScope` for coroutines
- **State management**: `MutableStateFlow` for internal state, expose as `StateFlow` via `asStateFlow()`
- **UI state**: Use data classes for immutable state snapshots (e.g., `AddEditBirthdayUiState`)
- **Repository pattern**: Interface + implementation, use `Flow<List<T>>` for reactive data streams

### Error Handling
- **Result types**: Use sealed classes for operation results (e.g., `AddBirthdayResult` with `Success`, `ValidationError`, `DatabaseError`)
- **Validation**: Centralized validators with detailed error messages, store error constants in companion objects
- **Exception handling**: Try-catch with meaningful error messages, never swallow exceptions
- **Error recovery**: Display user-friendly messages in UI, preserve valid form data

### Imports
- **Wildcard imports**: Avoid wildcard imports, use explicit imports
- **Ordering**: Android → Third-party → Project imports
- **Unused imports**: Remove automatically via IDE or lint
- **Static imports**: Use sparingly, prefer explicit references

### Testing Conventions
- **Framework**: JUnit 4 with Mockito, Mockito-Kotlin, and Turbine (for Flow testing)
- **Test naming**: Descriptive backtick names (e.g., `` `validateName returns error for blank name` ``)
- **Mocking**: Mockito for dependencies, mock at appropriate level (avoid over-mocking)
- **Simple tests**: Files ending with `SimpleTest.kt` for compilation/type-checking tests
- **Instrumentation tests**: `connectedAndroidTest` for UI and database tests, use `@HiltAndroidTest`

### Compose Guidelines
- **Composables**: Small, reusable components with clear single responsibility
- **Parameters**: Default modifiers first, then required parameters
- **State**: Prefer `remember` and `mutableStateOf`, hoist state to ViewModel when needed
- **Previews**: Include `@Preview` composables with showBackground=true
- **Performance**: Mark stable functions with `@Stable`, use `derivedStateOf` for computed values

### Database & Room
- **Entities**: Data classes with `@Entity` annotation
- **DAOs**: Use `@Dao` with suspend functions for writes, `Flow` for reactive reads
- **Type converters**: Use `@TypeConverter` annotations for complex types (e.g., `DateConverters`)
- **Schema**: Export schemas to `app/schemas/` directory

### Dependencies
- **Compose BOM**: `androidx.compose:compose-bom:2023.10.01`
- **Navigation**: `androidx.navigation:navigation-compose`
- **Hilt**: `com.google.dagger:hilt-android:2.48`
- **Room**: `androidx.room:room-runtime:2.6.1`
- **Coroutines**: `org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3`
- **WorkManager**: `androidx.work:work-runtime-ktx:2.9.0`

### Domain Layer Patterns
- **Use cases**: Annotate with `@Singleton` and `@Inject constructor`, use suspend functions
- **Input validation**: Always validate in use cases before calling repository
- **Error handling**: Wrap repository calls in try-catch, return sealed result types
- **Business logic**: Keep all business rules in use cases, not in ViewModels

### Data Layer Patterns
- **Repository interface**: Define in data layer, implement with concrete class
- **DAO queries**: Use `@Query` for complex SQL, prefer Room-generated queries when possible
- **Type converters**: Convert complex types (e.g., LocalDate) to primitive types for Room
- **Flow responses**: Return `Flow<T>` from DAO methods for reactive data streams

### UI Layer Patterns
- **Navigation**: Use `androidx.navigation:navigation-compose` with type-safe navigation
- **Composable signatures**: Required parameters first, optional parameters with defaults last
- **Side effects**: Use `LaunchedEffect` for one-time effects, `SideEffect` for synchronous effects
- **State hoisting**: Hoist state to the nearest common ancestor, prefer ViewModels for complex state

### Notification Patterns
- **WorkManager**: Use `PeriodicWorkRequest` for recurring notifications
- **Hilt worker integration**: Use `@HiltWorker` and `WorkerParameters` with `@AssistedInject`
- **Notification scheduling**: Schedule in background workers, not in UI thread
- **Time calculations**: Use domain layer utilities for date/time calculations

### Testing Best Practices
- **Unit tests**: Test business logic in isolation, mock repository dependencies
- **Flow testing**: Use Turbine's `test {}` coroutine to emit/test Flow values
- **ViewModel tests**: Use `TestDispatcher` and `runTest` for coroutine testing
- **Compose tests**: Use `ComposeTestRule` for UI testing, `assertIsDisplayed` for assertions

### Best Practices
- **Null safety**: Prefer non-null types, use `?` only when necessary
- **Collections**: Use immutable collections (`listOf`, `setOf`) when possible
- **Coroutines**: Use `suspend` for async operations, `viewModelScope` for ViewModel coroutines
- **KDoc**: Document all public APIs with `@param`, `@return`, and behavior descriptions
- **Extension functions**: For utility functions on existing types
- **Constants**: Store in companion objects with descriptive names
- **Sanitization**: Sanitize user input in validators (trim whitespace, normalize spaces)
- **Date handling**: Always use `java.time.LocalDate` for dates, never legacy `java.util.Date`
- **IDs**: Use `Long` for database IDs, handle auto-generated IDs appropriately

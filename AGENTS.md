# AGENTS.md

## Build/Lint/Test Commands

### Build Commands
- **Full build**: `./gradlew build` (includes compilation, tests, and linting)
- **Assemble only**: `./gradlew assemble` (compiles without running tests)

### Lint Commands
- **Kotlin linting**: `./gradlew ktlintCheck`
- **Android linting**: `./gradlew lint` (or `lintDebug`/`lintRelease` for specific variants)
- **Auto-fix lint issues**: `./gradlew lintFix`

### Test Commands
- **All unit tests**: `./gradlew test`
- **Debug variant tests**: `./gradlew testDebugUnitTest`
- **Release variant tests**: `./gradlew testReleaseUnitTest`
- **Run single test method**: Create a temporary test file with only that method, or modify build.gradle.kts to include test filtering
- **Android instrumentation tests**: `./gradlew connectedAndroidTest` (requires connected device/emulator)

## Code Style Guidelines

### Formatting (.editorconfig)
- **Indentation**: 4 spaces
- **Max line length**: 120 characters
- **Final newlines**: Required at end of files
- **Trailing commas**: Allowed and preferred for multi-line constructs

### Kotlin Style
- **Code style**: Official Kotlin style (`kotlin.code.style=official`)
- **Naming conventions**:
  - Classes/Interfaces: PascalCase (e.g., `AddBirthdayUseCase`)
  - Functions/Properties: camelCase (e.g., `addBirthday()`)
  - Constants: UPPER_SNAKE_CASE
- **Composable functions**: Follow standard Compose naming (PascalCase, no `function-naming` rule)

### Architecture Patterns
- **Clean Architecture**: Domain/Data/UI layers strictly separated
- **Dependency injection**: Hilt with `@Inject` constructors and `@HiltViewModel`/`@HiltAndroidApp`
- **State management**: StateFlow/MutableStateFlow in ViewModels
- **Coroutines**: suspend functions for async operations, viewModelScope for lifecycle-aware coroutines

### Documentation
- **KDoc**: Comprehensive documentation for all public APIs
- **Function documentation**: Include `@param`, `@return`, and behavior descriptions
- **Class documentation**: Purpose and responsibilities

### Error Handling
- **Result types**: Use sealed classes for operation results (e.g., `AddBirthdayResult.Success`/`ValidationError`/`DatabaseError`)
- **Validation**: Centralized validators with detailed error messages
- **Exception handling**: Try-catch with meaningful error messages, avoid swallowing exceptions

### Imports
- **Wildcard imports**: Avoid, use explicit imports
- **Ordering**: Android -> Third-party -> Project imports
- **Unused imports**: Remove automatically (IDE/Android Studio)

### Best Practices
- **Null safety**: Prefer non-null types, use `?` only when necessary
- **Collections**: Use immutable collections when possible (`listOf`, `setOf`)
- **Data classes**: Use for UI state and simple data transfer objects
- **Extension functions**: For utility functions on existing types
- **Type aliases**: For complex generic types to improve readability

### Testing
- **Framework**: JUnit 4 with Mockito and Mockito-Kotlin
- **Flow testing**: Turbine library for testing StateFlow/SharedFlow
- **Test naming**: Descriptive backtick names (e.g., `\`validateNotificationHour returns null for valid hours\``)
- **Mocking**: Mockito for dependencies, avoid over-mocking</content>
<parameter name="filePath">D:\Hmmm\Projects\Vibe\birfdae\AGENTS.md
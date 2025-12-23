package com.birthdayreminder.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/*
 * Performance optimization utilities for the Birthday Reminder app.
 * These utilities help prevent memory leaks and improve UI performance.
 */

/**
 * Lifecycle-aware flow collection that automatically starts and stops
 * collection based on the lifecycle state to prevent memory leaks.
 */
@Composable
fun <T> Flow<T>.collectAsStateWithLifecycle(
    initialValue: T,
    lifecycle: Lifecycle = LocalLifecycleOwner.current.lifecycle,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
): androidx.compose.runtime.State<T> {
    val state = remember { androidx.compose.runtime.mutableStateOf(initialValue) }

    LaunchedEffect(this, lifecycle, minActiveState) {
        lifecycle.repeatOnLifecycle(minActiveState) {
            this@collectAsStateWithLifecycle.collectLatest {
                state.value = it
            }
        }
    }

    return state
}

/*
 * Memory optimization tips for the Birthday Reminder app:
 *
 * 1. Use LazyColumn instead of Column for large lists
 * 2. Use remember() for expensive calculations
 * 3. Use derivedStateOf for computed values
 * 4. Avoid creating new objects in Composable functions
 * 5. Use stable parameters and immutable data classes
 * 6. Cancel coroutines in ViewModel.onCleared()
 * 7. Use distinctUntilChanged() on flows to prevent unnecessary emissions
 * 8. Use flowOn(Dispatchers.IO) for background operations
 * 9. Implement proper lifecycle awareness in ViewModels
 * 10. Use collectAsStateWithLifecycle instead of collectAsState for flows
 */

/**
 * Performance monitoring utilities for debugging
 */
object PerformanceMonitor {
    /**
     * Logs the time taken to execute a block of code.
     * Useful for identifying performance bottlenecks during development.
     */
    inline fun <T> measureTime(
        tag: String,
        block: () -> T,
    ): T {
        val startTime = System.currentTimeMillis()
        val result = block()
        val endTime = System.currentTimeMillis()
        println("[$tag] Execution time: ${endTime - startTime}ms")
        return result
    }

    /**
     * Logs memory usage information.
     * Useful for monitoring memory consumption during development.
     */
    fun logMemoryUsage(tag: String) {
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val maxMemory = runtime.maxMemory()
        val availableMemory = maxMemory - usedMemory

        println("[$tag] Memory Usage:")
        println("  Used: ${usedMemory / 1024 / 1024}MB")
        println("  Available: ${availableMemory / 1024 / 1024}MB")
        println("  Max: ${maxMemory / 1024 / 1024}MB")
    }
}

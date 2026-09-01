package com.sumit.simplemobileaisuite.core.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * A Flow operator that batches string chunks emitted within a specific time interval.
 * This is particularly useful for AI streaming to prevent UI flickering from 
 * high-frequency small updates.
 *
 * @param intervalMillis The minimum time between emissions.
 */
fun Flow<String>.chunkedByTime(intervalMillis: Long): Flow<String> = flow {
    var accumulated = ""
    var lastEmitTime = 0L

    collect { chunk ->
        accumulated += chunk
        val now = System.currentTimeMillis()

        if (now - lastEmitTime >= intervalMillis) {
            emit(accumulated)
            accumulated = ""
            lastEmitTime = now
        }
    }

    // Ensure final chunks are not lost when the source flow completes
    if (accumulated.isNotEmpty()) {
        emit(accumulated)
    }
}

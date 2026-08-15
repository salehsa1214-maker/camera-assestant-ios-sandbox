package app.dyrecto.ios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Swift-facing bridge for observing Kotlin [Flow]s without SKIE: each [watch] collects on the
 * main dispatcher and delivers values to a Swift closure; [close] cancels the collection.
 *
 * Usage from Swift:
 *   let watcher = FlowWatcher(flow: session.state)
 *   watcher.watch { state in ... }   // main-thread callbacks
 *   watcher.close()
 */
class FlowWatcher<T>(private val flow: Flow<T>) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var job: Job? = null

    fun watch(block: (T) -> Unit) {
        job?.cancel()
        job = scope.launch {
            flow.collect { block(it) }
        }
    }

    fun close() {
        scope.cancel()
    }
}

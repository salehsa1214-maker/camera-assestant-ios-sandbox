package app.dyrecto.ios

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Swift-facing factories: kotlinx's top-level `MutableStateFlow(value)` constructor function
 * doesn't export to ObjC/Swift, so the iOS implementations of shared seams (e.g. AlertFeedback's
 * `playing` flow) create their flows through these.
 */
fun mutableStateFlow(initial: Boolean): MutableStateFlow<Boolean> = MutableStateFlow(initial)

fun mutableStateFlowInt(initial: Int): MutableStateFlow<Int> = MutableStateFlow(initial)

fun mutableStateFlowAny(initial: Any?): MutableStateFlow<Any?> = MutableStateFlow(initial)

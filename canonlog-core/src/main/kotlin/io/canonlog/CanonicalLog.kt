package io.canonlog

import kotlin.coroutines.coroutineContext

public object CanonicalLog {
    public fun put(key: String, value: Any?) {
        threadLocalContext.get()?.put(key, value)
    }

    public fun increment(key: String, by: Long = 1L) {
        threadLocalContext.get()?.increment(key, by)
    }

    public fun markFailed(reason: String, vararg fields: Pair<String, Any>) {
        threadLocalContext.get()?.markFailed(reason, *fields)
    }

    public fun markDegraded(reason: String, vararg fields: Pair<String, Any>) {
        threadLocalContext.get()?.markDegraded(reason, *fields)
    }

    public suspend fun putSuspend(key: String, value: Any?) {
        coroutineContext[CanonicalLogElement]?.context?.put(key, value)
    }

    public suspend fun incrementSuspend(key: String, by: Long = 1L) {
        coroutineContext[CanonicalLogElement]?.context?.increment(key, by)
    }
}

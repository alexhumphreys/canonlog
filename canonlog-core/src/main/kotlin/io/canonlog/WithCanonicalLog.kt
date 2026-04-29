package io.canonlog

import kotlinx.coroutines.withContext

public typealias EmitFn = (CanonicalLogContext) -> Unit

public fun <T, R> withCanonicalLogBlocking(
    adapter: WorkUnitAdapter<T>,
    input: T,
    emit: EmitFn,
    block: (CanonicalLogContext) -> R,
): R {
    val ctx = CanonicalLogContext(adapter.describe(input))
    val previous = threadLocalContext.get()
    threadLocalContext.set(ctx)
    val startNs = System.nanoTime()
    try {
        val result = block(ctx)
        adapter.enrich(ctx, input, Outcome.Completed(elapsedMs(startNs)))
        return result
    } catch (t: Throwable) {
        adapter.enrich(ctx, input, Outcome.Threw(elapsedMs(startNs), t))
        throw t
    } finally {
        threadLocalContext.set(previous)
        emit(ctx)
    }
}

public suspend fun <T, R> withCanonicalLog(
    adapter: WorkUnitAdapter<T>,
    input: T,
    emit: EmitFn,
    block: suspend (CanonicalLogContext) -> R,
): R {
    val ctx = CanonicalLogContext(adapter.describe(input))
    val startNs = System.nanoTime()
    return withContext(CanonicalLogElement(ctx)) {
        try {
            val result = block(ctx)
            adapter.enrich(ctx, input, Outcome.Completed(elapsedMs(startNs)))
            result
        } catch (t: Throwable) {
            adapter.enrich(ctx, input, Outcome.Threw(elapsedMs(startNs), t))
            throw t
        } finally {
            emit(ctx)
        }
    }
}

private fun elapsedMs(startNs: Long): Long = (System.nanoTime() - startNs) / 1_000_000

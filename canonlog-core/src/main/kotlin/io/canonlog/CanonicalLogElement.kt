package io.canonlog

import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

internal val threadLocalContext: ThreadLocal<CanonicalLogContext?> = ThreadLocal()

public class CanonicalLogElement internal constructor(
    public val context: CanonicalLogContext,
) : AbstractCoroutineContextElement(Key), ThreadContextElement<CanonicalLogContext?> {

    public companion object Key : CoroutineContext.Key<CanonicalLogElement>

    override fun updateThreadContext(context: CoroutineContext): CanonicalLogContext? {
        val previous = threadLocalContext.get()
        threadLocalContext.set(this.context)
        return previous
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: CanonicalLogContext?) {
        threadLocalContext.set(oldState)
    }
}

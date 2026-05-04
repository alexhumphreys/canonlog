package io.github.alexhumphreys.canonicallog

public sealed class Outcome {
    public abstract val durationMs: Long

    /**
     * The block returned normally. Whether the work was successful in a business sense
     * is up to the adapter and handler to decide via [CanonicalLogContext.markFailed]
     * or status-code inspection.
     */
    public data class Completed(override val durationMs: Long) : Outcome()

    /**
     * The block threw. Always indicates failure at the lifecycle level.
     */
    public data class Threw(override val durationMs: Long, val cause: Throwable) : Outcome()
}

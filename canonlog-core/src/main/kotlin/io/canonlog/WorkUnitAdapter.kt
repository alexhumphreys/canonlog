package io.canonlog

public interface WorkUnitAdapter<T> {
    public fun describe(input: T): WorkUnit
    public fun enrich(ctx: CanonicalLogContext, input: T, outcome: Outcome)
}

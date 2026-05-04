package io.github.alexhumphreys.canonicallog

import java.time.Instant

public data class WorkUnit(
    val id: String,
    val kind: String,
    val startedAt: Instant,
)

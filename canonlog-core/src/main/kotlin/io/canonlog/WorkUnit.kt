package io.canonlog

import java.time.Instant

public data class WorkUnit(
    val id: String,
    val kind: String,
    val startedAt: Instant,
)

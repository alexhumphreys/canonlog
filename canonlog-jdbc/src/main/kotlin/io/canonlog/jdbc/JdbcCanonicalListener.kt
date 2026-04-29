package io.canonlog.jdbc

import io.canonlog.CanonicalLog
import net.ttddyy.dsproxy.ExecutionInfo
import net.ttddyy.dsproxy.QueryInfo
import net.ttddyy.dsproxy.listener.QueryExecutionListener

public class JdbcCanonicalListener(
    private val slowQueryThresholdMs: Long = DEFAULT_SLOW_QUERY_THRESHOLD_MS,
) : QueryExecutionListener {

    override fun beforeQuery(execInfo: ExecutionInfo, queryInfoList: MutableList<QueryInfo>) {
        // no-op
    }

    override fun afterQuery(execInfo: ExecutionInfo, queryInfoList: MutableList<QueryInfo>) {
        CanonicalLog.increment("db_query_count", queryInfoList.size.toLong())
        CanonicalLog.increment("db_query_duration_ms_total", execInfo.elapsedTime)
        if (execInfo.elapsedTime > slowQueryThresholdMs) {
            CanonicalLog.increment("db_slow_query_count")
        }
        if (!execInfo.isSuccess) {
            CanonicalLog.increment("db_query_error_count")
        }
    }

    public companion object {
        public const val DEFAULT_SLOW_QUERY_THRESHOLD_MS: Long = 100L
    }
}

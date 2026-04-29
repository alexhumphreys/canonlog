package io.canonlog.jdbc.spring

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@AutoConfiguration
@ConditionalOnClass(DataSource::class)
public open class JdbcCanonicalAutoConfiguration {
    public companion object {
        @Bean
        @JvmStatic
        @ConditionalOnMissingBean
        public fun jdbcCanonicalBeanPostProcessor(): JdbcCanonicalBeanPostProcessor =
            JdbcCanonicalBeanPostProcessor()
    }
}

package io.canonlog.jdbc.spring

import io.canonlog.jdbc.withCanonicalLogging
import net.ttddyy.dsproxy.support.ProxyDataSource
import org.springframework.beans.factory.config.BeanPostProcessor
import javax.sql.DataSource

public class JdbcCanonicalBeanPostProcessor : BeanPostProcessor {
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean !is DataSource) return bean
        if (bean is ProxyDataSource) return bean
        return bean.withCanonicalLogging(name = beanName)
    }
}

package io.canonlog.okhttp.spring

import io.canonlog.okhttp.OkHttpCanonicalInterceptor
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean

@AutoConfiguration
@ConditionalOnClass(OkHttpClient::class)
public open class OkHttpCanonicalAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public open fun okHttpCanonicalInterceptor(): OkHttpCanonicalInterceptor =
        OkHttpCanonicalInterceptor()
}

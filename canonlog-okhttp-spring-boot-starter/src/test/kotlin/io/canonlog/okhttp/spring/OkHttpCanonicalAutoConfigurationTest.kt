package io.canonlog.okhttp.spring

import io.canonlog.okhttp.OkHttpCanonicalInterceptor
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class OkHttpCanonicalAutoConfigurationTest : DescribeSpec({
    val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(OkHttpCanonicalAutoConfiguration::class.java))

    describe("OkHttpCanonicalAutoConfiguration") {
        it("registers the interceptor as a bean by default") {
            runner.run { ctx ->
                ctx.getBeansOfType(OkHttpCanonicalInterceptor::class.java).size shouldBe 1
            }
        }

        it("backs off if the user already supplied an interceptor bean") {
            runner.withBean("custom", OkHttpCanonicalInterceptor::class.java).run { ctx ->
                ctx.getBeansOfType(OkHttpCanonicalInterceptor::class.java).size shouldBe 1
                ctx.containsBean("custom") shouldBe true
                ctx.containsBean("okHttpCanonicalInterceptor") shouldBe false
            }
        }
    }
})

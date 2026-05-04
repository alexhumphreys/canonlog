plugins {
    alias(libs.plugins.spring.dep.mgmt)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    api(project(":canonical-log-core"))
    api(project(":canonical-log-okhttp-spring-boot-starter"))
    api(project(":canonical-log-jdbc-spring-boot-starter"))
    implementation(libs.spring.boot.starter.web)
    implementation(libs.logstash.logback.encoder)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotest.property)
}

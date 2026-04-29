plugins {
    alias(libs.plugins.spring.dep.mgmt)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    api(project(":canonlog-jdbc"))
    implementation(libs.spring.boot.autoconfigure)
    implementation(libs.datasource.proxy)

    testImplementation(project(":canonlog-core"))
    testImplementation(project(":canonlog-jdbc"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.jdbc)
    testImplementation(libs.h2)
}

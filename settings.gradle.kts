rootProject.name = "canonlog"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(
    "canonlog-core",
    "canonlog-okhttp",
    "canonlog-okhttp-spring-boot-starter",
    "canonlog-jdbc",
    "canonlog-jdbc-spring-boot-starter",
    "canonlog-spring-boot-starter",
    "samples:spring-demo",
)

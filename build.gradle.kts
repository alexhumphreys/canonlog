import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dep.mgmt) apply false
}

subprojects {
    group = "io.canonlog"
    version = "0.1.0-SNAPSHOT"

    apply(plugin = "org.jetbrains.kotlin.jvm")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    dependencies {
        "testImplementation"(rootProject.libs.kotest.runner.junit5)
        "testImplementation"(rootProject.libs.kotest.assertions.core)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    val isLibrary = path.startsWith(":canonlog-")
    if (isLibrary) {
        apply(plugin = "maven-publish")
        tasks.named<KotlinCompile>("compileKotlin").configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexplicit-api=strict")
            }
        }
        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    afterEvaluate { from(components["java"]) }
                }
            }
        }
    }
}

val Project.libs: org.gradle.accessors.dm.LibrariesForLibs
    get() = the()

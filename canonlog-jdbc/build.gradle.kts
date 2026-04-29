dependencies {
    implementation(project(":canonlog-core"))
    implementation(libs.datasource.proxy)

    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.postgresql)
}


val kotlin_version="1.7.0"
val prometeus_version="1.9.1"
val springboot_version="2.7.1"
val springkafka_version="2.8.7"
val springwebmvcpac4j_version = "6.0.3"
val springframeworkbom_version = "5.3.22"
val jacksonkotlin_version="2.13.2"
val slf4j_version="1.7.36"
val logstashlogback_version="7.2"
val tokensupport_version = "2.1.0"
val tokensupporttest_version = "2.0.0"
val hibernatrevalidator_version = "7.0.4.Final"
val mockk_version = "1.12.4"
val springmockk_version = "3.1.1"
val junitplatform_version = "1.8.2"

java.sourceCompatibility = JavaVersion.VERSION_17

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.spring") version "1.7.0"
    kotlin("plugin.jpa") version "1.7.0"
    id("org.springframework.boot") version "2.7.1"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    id("org.owasp.dependencycheck") version "7.1.1"
}

group = "no.nav.pensjon"

repositories {
    mavenCentral()
}

dependencies {

    // Spring Boot & Framework
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springboot_version"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "com.zaxxer", module = "HikariCP")
    }
    implementation(platform("org.springframework:spring-framework-bom:$springframeworkbom_version"))

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonkotlin_version")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonkotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka:$springkafka_version")
    // kafka-test/mock
    testImplementation("org.springframework.kafka:spring-kafka-test:$springkafka_version")


    // OIDC - AzureAd
    implementation("no.nav.security:token-validation-spring:$tokensupport_version")
    implementation("no.nav.security:token-validation-jaxrs:$tokensupport_version")
    implementation("no.nav.security:token-client-spring:$tokensupport_version")
    // Only used for starting up locally
    implementation("no.nav.security:token-validation-test-support:$tokensupporttest_version")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashlogback_version")
    configurations.implementation {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.slf4j:jcl-over-slf4j:$slf4j_version")

    // Micrometer
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")

    // DB
    implementation("javax.persistence:javax.persistence-api:2.2")
    implementation("com.oracle.database.jdbc:ojdbc11:21.6.0.0.1")

    //test
    testImplementation("com.ninja-squad:springmockk:$springmockk_version")
    testImplementation("org.pac4j:spring-webmvc-pac4j:$springwebmvcpac4j_version")

    // mock - test
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("org.junit.platform:junit-platform-suite-api:$junitplatform_version")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springboot_version") {
        exclude(module = "junit")
        exclude(module = "mockito-core")
        exclude(module = "junit-vintage-engine")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    failFast = true
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<Jar> {
    archiveBaseName.set("sporingslogg")
}

tasks.withType<Wrapper> {
    gradleVersion = "7.4.2"
}
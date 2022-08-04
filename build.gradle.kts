
val kotlinVersion="1.7.0"
val prometeusVersion="1.9.2"
val springbootVersion="2.7.1"
val springkafkaVersion="2.8.7"
val springwebmvcpac4jVersion = "6.0.3"
val springframeworkbomVersion = "5.3.22"
val jacksonkotlinVersion="2.13.2"
val slf4jVersion="1.7.36"
val logstashlogbackVersion="7.2"
val tokensupportVersion = "2.1.0"
val tokensupporttestVersion = "2.0.0"
val hibernatrevalidatorVersion = "7.0.4.Final"
val mockkVersion = "1.12.4"
val springmockkVersion = "3.1.1"
val junitplatformVersion = "1.8.2"

plugins {
    kotlin("jvm") version "1.7.0"
    kotlin("plugin.spring") version "1.7.0"
    kotlin("plugin.jpa") version "1.7.0"
    id("base")
    id("org.springframework.boot") version "2.7.1"
    id("io.spring.dependency-management") version "1.0.12.RELEASE"
    id("org.owasp.dependencycheck") version "7.1.1"
}

group = "no.nav.pensjon"

java.sourceCompatibility = JavaVersion.VERSION_17


kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17)) // 17
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // Spring Boot & Framework
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springbootVersion"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") {
        exclude(group = "com.zaxxer", module = "HikariCP")
    }
    implementation(platform("org.springframework:spring-framework-bom:$springframeworkbomVersion"))

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonkotlinVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonkotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka:$springkafkaVersion")
    // kafka-test/mock
    testImplementation("org.springframework.kafka:spring-kafka-test:$springkafkaVersion")


    // OIDC - AzureAd
    implementation("no.nav.security:token-validation-spring:$tokensupportVersion")
    implementation("no.nav.security:token-validation-jaxrs:$tokensupportVersion")
    implementation("no.nav.security:token-client-spring:$tokensupportVersion")
    // Only used for starting up locally
    implementation("no.nav.security:token-validation-test-support:$tokensupporttestVersion")

    implementation("no.nav.common:token-client:2.2022.06.30_14.40-a34f4016edd9")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashlogbackVersion")
    configurations.implementation {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")

    // Micrometer
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeusVersion")

    // DB
    implementation("javax.persistence:javax.persistence-api:2.2")
    implementation("com.oracle.database.jdbc:ojdbc11:21.6.0.0.1")

    //test
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("org.pac4j:spring-webmvc-pac4j:$springwebmvcpac4jVersion")

    // mock - test
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.platform:junit-platform-suite-api:$junitplatformVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springbootVersion") {
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
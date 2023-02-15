import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion="1.8.10"
val prometeusVersion= "1.10.2"
val springbootVersion= "3.0.2"
val springkafkaVersion="3.0.2"
val springwebmvcpac4jVersion = "7.0.0"
val springframeworkbomVersion = "6.0.4"
val jacksonkotlinVersion= "2.14.2"
val oracle11Version="21.8.0.0"
val slf4jVersion= "2.0.5"
val logstashlogbackVersion="7.2"
val tokensupportVersion = "3.0.2"
val tokensupporttestVersion = "2.0.0"
val mockOAuth2ServerVersion = "0.5.7"
val hibernatrevalidatorVersion = "7.0.4.Final"
val mockkVersion = "1.13.4"
val springmockkVersion = "4.0.0"
val junitplatformVersion = "1.9.2"

plugins {
    val pluginSpringBootVersion = "3.0.1"
    val pluginKotlinVersion = "1.8.0"

    kotlin("jvm") version pluginKotlinVersion
    kotlin("plugin.spring") version pluginKotlinVersion
    kotlin("plugin.jpa") version pluginKotlinVersion
    id("org.springframework.boot") version pluginSpringBootVersion
    id("io.spring.dependency-management") version "1.1.0"
    id("org.owasp.dependencycheck") version "7.1.1"
}

group = "no.nav.pensjon"
java.sourceCompatibility = JavaVersion.VERSION_18

repositories {
    mavenCentral()
}

dependencies {

    // Spring Boot & Framework
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springbootVersion"))
    implementation("org.springframework.boot:spring-boot-starter-web:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-aop:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-actuator:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-jdbc:$springbootVersion")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springbootVersion")
    implementation(platform("org.springframework:spring-framework-bom:$springframeworkbomVersion"))

    //spring boot 3.0 extra
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonkotlinVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonkotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka:$springkafkaVersion")
    // Kafka-test
    testImplementation("org.springframework.kafka:spring-kafka-test:$springkafkaVersion")

    // Token support Azuread, Oidc
    implementation("no.nav.security:token-validation-spring:$tokensupportVersion")
    implementation("no.nav.security:token-validation-jaxrs:$tokensupportVersion")
    implementation("no.nav.security:token-client-spring:$tokensupportVersion")
    // Only used for starting up locally
    implementation("no.nav.security:token-validation-test-support:$tokensupporttestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokensupportVersion")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashlogbackVersion")
    configurations.implementation {
        exclude(group = "commons-logging", module = "commons-logging")
    }
    implementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")

    // Micrometer
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeusVersion")

    // DB
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")
    implementation("com.oracle.database.jdbc:ojdbc11:$oracle11Version")
    implementation("org.hibernate:hibernate-core-jakarta:5.6.14.Final")

    // test
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

tasks {
    withType<Test> {
        useJUnitPlatform()
        failFast = true
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "18"
        }
    }

    withType<Jar> {
        archiveBaseName.set("sporingslogg")
    }

    withType<Wrapper> {
        gradleVersion = "7.6"
    }

}
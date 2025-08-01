import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val kotlinVersion= "2.2.0"
val prometeusVersion= "1.15.2"
val springbootVersion= "3.5.4"
val springkafkaVersion="3.3.8"
val springwebmvcpac4jVersion = "8.0.1"
val springframeworkbomVersion = "6.2.9"
val jacksonkotlinVersion= "2.19.2"
val oracle11Version="23.8.0.25.04"
val slf4jVersion= "2.0.17"
val logstashlogbackVersion="8.1"
val tokensupportVersion = "5.0.34"
val tokensupporttestVersion = "2.0.5"
val mockOAuth2ServerVersion = "2.2.1"
val hibernateCoreVersion = "7.0.6.Final"
val jakartaAnnotationApiVersion = "3.0.0"
val jakartaInjectApiVersion = "2.0.1"
val mockkVersion = "1.14.5"
val springmockkVersion = "4.0.2"
val junitplatformVersion = "1.13.4"
val h2DbVersion = "2.3.232"

plugins {
    val pluginSpringBootVersion = "3.5.4"
    val pluginKotlinVersion = "2.2.0"

    kotlin("jvm") version pluginKotlinVersion
    kotlin("plugin.spring") version pluginKotlinVersion
    kotlin("plugin.jpa") version pluginKotlinVersion
    id("org.springframework.boot") version pluginSpringBootVersion
    id("io.spring.dependency-management") version "1.1.7"
    id("org.owasp.dependencycheck") version "12.1.3"
}

group = "no.nav.pensjon"
java.sourceCompatibility = JavaVersion.VERSION_21

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

    // Kafka
    implementation("org.springframework.kafka:spring-kafka:$springkafkaVersion")
    testImplementation("org.springframework.kafka:spring-kafka-test:$springkafkaVersion")

    //spring boot 3.0 jakaera-api
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")
    implementation("jakarta.inject:jakarta.inject-api:$jakartaInjectApiVersion")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonkotlinVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonkotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

    // Token support Azuread, Oidc
    implementation("no.nav.security:token-validation-spring:$tokensupportVersion")
    implementation("no.nav.security:token-validation-jaxrs:$tokensupportVersion")
    implementation("no.nav.security:token-client-spring:$tokensupportVersion")
    // Only used for starting up locally testing
    implementation("no.nav.security:token-validation-test-support:$tokensupporttestVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOAuth2ServerVersion")
    testImplementation("no.nav.security:token-validation-spring-test:$tokensupportVersion")

    // Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashlogbackVersion")
    implementation("org.slf4j:jcl-over-slf4j:$slf4jVersion")
    // Micrometer
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeusVersion")

    // DB
    implementation("com.oracle.database.jdbc:ojdbc11:$oracle11Version")
    implementation("org.hibernate.orm:hibernate-core:$hibernateCoreVersion")
    // test
    testImplementation("com.ninja-squad:springmockk:$springmockkVersion")
    testImplementation("org.pac4j:spring-webmvc-pac4j:$springwebmvcpac4jVersion")

    // mock - test
    testImplementation("com.h2database:h2:$h2DbVersion")
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


    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }

    withType<Jar> {
        archiveBaseName.set("sporingslogg")
    }

    withType<Wrapper> {
        gradleVersion = "8.9"
    }

}
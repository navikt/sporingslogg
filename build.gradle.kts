import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile


val springkafkaVersion="3.3.10"
val prometeusVersion= "1.16.1"
val jacksonkotlinVersion= "2.20.1"
val slf4jVersion= "2.0.17"
val logstashlogbackVersion="9.0"
val tokensupportVersion = "5.0.40"
val tokensupporttestVersion = "2.0.5"
val oracle11Version="23.26.0.0.0"
val hibernateCoreVersion = "7.1.11.Final"
val jakartaAnnotationApiVersion = "3.0.0"
val jakartaInjectApiVersion = "2.0.1"
val mockOAuth2ServerVersion = "3.0.1"
val springwebmvcpac4jVersion = "8.0.1"
val mockkVersion = "1.14.7"
val springmockkVersion = "4.0.2"
val junitplatformVersion = "6.0.1"
val h2DbVersion = "2.4.240"
val commonsLang3Version = "3.18.0"

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.springframework.boot") version "3.5.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.owasp.dependencycheck") version "12.1.9"
}

group = "no.nav.pensjon"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.kafka:spring-kafka:${springkafkaVersion}")
    implementation("com.oracle.database.jdbc:ojdbc11:${oracle11Version}")
    implementation("org.hibernate.orm:hibernate-core:${hibernateCoreVersion}")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationApiVersion")
    implementation("jakarta.inject:jakarta.inject-api:$jakartaInjectApiVersion")
    implementation("jakarta.persistence:jakarta.persistence-api:3.2.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonkotlinVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonkotlinVersion}")
    implementation("net.logstash.logback:logstash-logback-encoder:${logstashlogbackVersion}")
    implementation("org.slf4j:jcl-over-slf4j:${slf4jVersion}")
    implementation("io.micrometer:micrometer-registry-prometheus:${prometeusVersion}")
    implementation("no.nav.security:token-validation-core")
    implementation("no.nav.security:token-validation-spring:$tokensupportVersion")
    implementation("no.nav.security:token-validation-jaxrs:$tokensupportVersion")
    implementation("no.nav.security:token-client-spring:$tokensupportVersion")
    // mock - test
    implementation("no.nav.security:token-validation-test-support:${tokensupporttestVersion}")
    implementation("org.apache.commons:commons-lang3:3.20.0")
    testImplementation("no.nav.security:mock-oauth2-server:${mockOAuth2ServerVersion}")
    testImplementation("no.nav.security:token-validation-spring-test:${tokensupportVersion}")
    testImplementation("com.ninja-squad:springmockk:${springmockkVersion}")
    testImplementation("org.pac4j:spring-webmvc-pac4j:${springwebmvcpac4jVersion}")
    testImplementation("org.springframework.kafka:spring-kafka-test:${springkafkaVersion}")
    testImplementation("com.h2database:h2:$h2DbVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.platform:junit-platform-suite-api:$junitplatformVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
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
        gradleVersion = "9.0.0"
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.apache.commons" && requested.module.toString() == "commons-lang") {
                useVersion(commonsLang3Version)
            }
        }
    }

}
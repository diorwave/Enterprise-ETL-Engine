import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21" apply false
    kotlin("plugin.jpa") version "2.0.21" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

group = "com.quantum.etl"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://packages.confluent.io/maven/") }
    }
}

subprojects {
    apply(plugin = "kotlin")
    
    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))
        
        // Logging
        implementation("org.slf4j:slf4j-api:2.0.16")
        implementation("ch.qos.logback:logback-classic:1.5.12")
        
        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
        testImplementation("io.mockk:mockk:1.13.13")
        testImplementation("org.assertj:assertj-core:3.26.3")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    }
    
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }
    
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

// Define version catalog
extra["springBootVersion"] = "3.3.5"
extra["springCloudVersion"] = "2024.0.0"
extra["kotlinVersion"] = "2.0.21"
extra["coroutinesVersion"] = "1.9.0"
extra["clickhouseVersion"] = "0.7.1"
extra["redisVersion"] = "3.4.0"
extra["mapstructVersion"] = "1.6.3"
extra["liquibaseVersion"] = "4.30.0"
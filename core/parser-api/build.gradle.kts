plugins {
    kotlin("jvm")
    `maven-publish`
    signing
    jacoco
}

description = "Core API module defining interfaces and contracts for data parsers"

// Configure source sets for proper JAR packaging
kotlin {
    jvmToolchain(21)
}

dependencies {
    // Kotlin standard library (inherited from parent)
    implementation(kotlin("stdlib"))
    
    // SLF4J for logging (inherited from parent)
    implementation("org.slf4j:slf4j-api")
    
    // Testing dependencies (inherited from parent)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk")
    testImplementation("org.assertj:assertj-core")
}

// Configure JAR manifest
tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Quantum Soft",
                "Built-By" to System.getProperty("user.name"),
                "Built-JDK" to System.getProperty("java.version"),
                "Created-By" to "Gradle ${gradle.gradleVersion}"
            )
        )
    }
}

// Create sources JAR
val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

// Create Javadoc JAR
val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get())
}

// Configure publishing
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            
            groupId = "com.quantum.etl"
            artifactId = "parser-api"
            
            pom {
                name.set("TIA ETL Parser API")
                description.set("Core API module defining interfaces and contracts for data parsers in the TIA ETL System")
                url.set("https://github.com/quantum-soft/tia-etl-system")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("quantum-soft")
                        name.set("Quantum Soft Development Team")
                        email.set("dev@quantum-soft.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/quantum-soft/tia-etl-system.git")
                    developerConnection.set("scm:git:ssh://github.com:quantum-soft/tia-etl-system.git")
                    url.set("https://github.com/quantum-soft/tia-etl-system/tree/main")
                }
            }
        }
    }
    
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/quantum-soft-dev/tia-etl-system")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: findProperty("gpr.user")?.toString()
                password = System.getenv("GITHUB_TOKEN") ?: findProperty("gpr.key")?.toString()
            }
        }
    }
}

// Configure signing (optional for GitHub Packages, but good practice)
signing {
    val signingKey: String? = System.getenv("SIGNING_KEY")
    val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
    
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }
}

// Configure test reporting and coverage
tasks.test {
    useJUnitPlatform()
    
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
    
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStandardStreams = false
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }
    
    // Enable parallel test execution
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    
    finalizedBy(tasks.jacocoTestReport)
}

// Configure JaCoCo coverage reporting
tasks.jacocoTestReport {
    dependsOn(tasks.test)
    
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// Configure coverage verification with reasonable thresholds
tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()  // 50% instruction coverage
            }
        }
        rule {
            limit {
                counter = "BRANCH" 
                value = "COVEREDRATIO"
                minimum = "0.40".toBigDecimal()  // 40% branch coverage
            }
        }
    }
}

// Ensure build reproducibility
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

// Custom task to check if ready for publishing
tasks.register("publishingCheck") {
    description = "Verifies that the module is ready for publishing"
    group = "verification"
    
    doLast {
        val version = project.version.toString()
        require(!version.contains("SNAPSHOT")) { 
            "Cannot publish SNAPSHOT versions to GitHub Packages" 
        }
        require(version.matches(Regex("""\d+\.\d+\.\d+"""))) { 
            "Version must follow semantic versioning (X.Y.Z)" 
        }
        
        val githubToken = System.getenv("GITHUB_TOKEN") ?: findProperty("gpr.key")?.toString()
        require(!githubToken.isNullOrBlank()) { 
            "GITHUB_TOKEN environment variable or gpr.key property must be set for publishing" 
        }
        
        println("✅ Publishing checks passed for version $version")
    }
}

// Make publish depend on publishing checks
tasks.publish {
    dependsOn("publishingCheck")
}

// Custom task for creating release-ready build
tasks.register("releaseBuild") {
    description = "Creates a release-ready build with all artifacts"
    group = "build"
    
    dependsOn(
        tasks.clean,
        tasks.build,
        tasks.test,
        tasks.jacocoTestReport,
        sourcesJar,
        javadocJar
    )
    
    doLast {
        println("✅ Release build completed successfully")
        println("📦 Artifacts created:")
        println("   - ${tasks.jar.get().archiveFile.get().asFile}")
        println("   - ${sourcesJar.get().archiveFile.get().asFile}")  
        println("   - ${javadocJar.get().archiveFile.get().asFile}")
        println("📊 Test report: ${tasks.test.get().reports.html.outputLocation.get()}")
        println("📈 Coverage report: ${tasks.jacocoTestReport.get().reports.html.outputLocation.get()}")
    }
}

// Task to run without coverage verification for development
tasks.register("buildForDev") {
    description = "Build without strict coverage requirements for development"
    group = "build"
    
    dependsOn(
        tasks.clean,
        tasks.compileKotlin,
        tasks.compileTestKotlin,
        tasks.test,
        tasks.jar
    )
}

// Override test task to not fail on coverage by default during development
if (!project.hasProperty("strictCoverage")) {
    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
        // Don't finalize with coverage verification by default
    }
    
    // Only add coverage verification when explicitly requested
    tasks.register("testWithCoverage") {
        description = "Run tests with strict coverage verification"
        group = "verification"
        
        dependsOn(tasks.test)
        finalizedBy(tasks.jacocoTestCoverageVerification)
    }
}
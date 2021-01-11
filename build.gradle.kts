// set properties of gradle.yml

plugins {
    // for idea tool (intellij)
    idea

    // for publish
    `maven-publish`

    // use spring framework
    id("org.springframework.boot") version "2.4.1"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("org.jlleitschuh.gradle.ktlint") version "9.4.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "9.4.1"

    // use kotlin
    kotlin("jvm") version "1.4.21"
    kotlin("plugin.spring") version "1.4.21"
    kotlin("plugin.jpa") version "1.4.21"
    kotlin("plugin.serialization") version "1.4.21"

    // etc
    // for gradle.yml (read yml instead of gradle properties)
    id("pl.softmate.gradle-properties-yaml-plugin") version "0.0.2"
}

val koofreeProjectGroup: String by project
val koofreeProjectVersion: String by project
val koofreeDeveloperId: String by project
val koofreeDeveloperName: String by project
val koofreeDeveloperEmail: String by project
val koofreeLicenseName: String by project
val koofreeLicenseUrl: String by project
val koofreeGithubUsername: String by project
val koofreeGithubRepository: String by project
val javaVersion: Int by project

configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }

    dependencies {
        dependency("$koofreeProjectGroup:${project.name}:$koofreeProjectVersion")
    }
}

group = koofreeProjectGroup
version = koofreeProjectVersion
java.sourceCompatibility = javaVersion.let { JavaVersion.toVersion(it) }

repositories {
    mavenCentral()
}

dependencies {
    // spring library
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-security")
    api("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("io.jsonwebtoken:jjwt-api:0.11.2")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.2")
    // implementation("org.springframework.security:spring-security-jwt:1.1.1.RELEASE")
    // implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // kotlin
    api("org.jetbrains.kotlin:kotlin-reflect")
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

    // test library
    testApi("org.springframework.boot:spring-boot-starter-test")

    // dev tools
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

ktlint {
    version.set("0.40.0")
    debug.set(false)
    verbose.set(false)
    android.set(false)
}

tasks {
    compileKotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict", "-Xadd-light-debug=disable")
            jvmTarget = javaVersion.toString()
        }
    }

    test {
        useJUnitPlatform()
    }

    jar {
        manifest.attributes["Specification-Title"] = project.name
        manifest.attributes["Specification-Version"] = project.version
        manifest.attributes["Implementation-Title"] = project.name
        manifest.attributes["Implementation-Version"] = project.version
        manifest.attributes["Automatic-Module-Name"] = project.name.replace('-', '.')
        manifest.attributes["Created-By"] =
            "${System.getProperty("java.version")} (${System.getProperty("java.specification.vendor")})"
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            credentials {
                username = project.findProperty("gpr.user") as? String ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as? String ?: System.getenv("TOKEN")
            }
            url = uri("https://maven.pkg.github.com/$koofreeGithubRepository")
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = project.group as String
            artifactId = project.name
            version = project.version as String
            artifact(tasks.getByName("bootJar"))

            from(components["java"])

            pom {
                licenses {
                    license {
                        name.set(koofreeLicenseName)
                        url.set(koofreeLicenseUrl)
                    }
                }
                developers {
                    developer {
                        id.set(koofreeDeveloperId)
                        name.set(koofreeDeveloperName)
                        email.set(koofreeDeveloperEmail)
                    }
                }
            }
        }
    }
}

springBoot {
    buildInfo()
}

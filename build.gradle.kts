plugins {
    `java-library`
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("tech.harmonysoft.oss.custom-gradle-dist-plugin") version "1.19.0"
    id("tech.harmonysoft.oss.gradle.release.paperwork") version "1.8.0"
    if (System.getenv("CI_ENV").isNullOrBlank()) {
        signing
    }
}

group = "tech.harmonysoft"
version = "1.12.0"

gradleDist {
    gradleVersion = "8.7"
    customDistributionVersion = project.version.toString()
    customDistributionName = "harmonysoft-kotlin-spring"
}

repositories {
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

java {
    withSourcesJar()
}

tasks.jar {
    archiveClassifier = ""
}

val docJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc)
}

val gradleDistFile = layout.buildDirectory.getAsFileTree().files.find {
    it.name.contains("-harmonysoft-") && it.name.startsWith("gradle-") && it.name.endsWith(".zip")
}

val gradleDistArtifact = gradleDistFile?.let {
    artifacts.add("archives", it)
}

publishing {
    publications {
        create<MavenPublication>("main") {
            if (gradleDistFile != null && gradleDistArtifact != null) {
                artifactId = gradleDistFile.name.let {
                    val i = it.lastIndexOf("-")
                    val j = it.substring(0, i).lastIndexOf("-")
                    it.substring(0, j)
                }
                version = gradleDistFile.name.let {
                    val j = it.indexOf("-bin")
                    val i = it.substring(0, j).lastIndexOf("-")
                    it.substring(i + 1, j)
                }
                artifact(gradleDistArtifact)
                artifact(docJar)
                pom {
                    name.set(project.name)
                    description.set("gradle distribution with common kotlin and spring setup")
                    url.set("https://github.com/harmonhsoft-tech/kotlin-spring-gradle-dist.git")

                    licenses {
                        license {
                            name.set("The MIT License (MIT)")
                            url.set("http://opensource.org/licenses/MIT")
                        }
                    }

                    developers {
                        developer {
                            id.set("denis")
                            name.set("Denis Zhdanov")
                            email.set("denzhdanov@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://https://github.com/harmonhsoft-tech/kotlin-spring-gradle-dist.git")
                        developerConnection.set("scm:git:git://https://github.com/harmonhsoft-tech/kotlin-spring-gradle-dist.git")
                        url.set("https://github.com/harmonhsoft-tech/kotlin-spring-gradle-dist.git")
                    }
                }
            }
        }
    }
}

if (System.getenv("CI_ENV").isNullOrBlank()) {
    signing {
        sign(publishing.publications["main"])
    }
}
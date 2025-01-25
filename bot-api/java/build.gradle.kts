import java.nio.file.Files
import java.nio.file.Paths

description = "Robocode Tank Royale Bot API for Java"

val javadocTitle = "Robocode Tank Royale Bot API"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

base {
    archivesName = "robocode-tankroyale-bot-api" // renames _all_ archive names
}

plugins {
    `java-library`
    alias(libs.plugins.shadow.jar)
    `maven-publish`
    signing
}

dependencies {
    implementation(project(":schema:jvm"))

    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.nv.i18n)
    implementation(libs.batik.svggen)
    implementation(libs.batik.dom)

    testImplementation(testLibs.junit.api)
    testImplementation(testLibs.junit.params)
    testImplementation(testLibs.junit.engine)
    testImplementation(testLibs.assertj)
    testImplementation(testLibs.system.stubs)
    testImplementation(libs.java.websocket) // for mocked server
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

tasks {
    jar {
        enabled = false
        dependsOn(
            shadowJar
        )
    }

    shadowJar {
        manifest {
            attributes["Implementation-Title"] = javadocTitle
            attributes["Implementation-Version"] = project.version
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = project.group
        }
        minimize()
        archiveClassifier.set("")
    }

    javadoc {
        title = "$javadocTitle $version"
        source(sourceSets.main.get().allJava)

        (options as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PROTECTED
            overview = "src/main/javadoc/overview.html"

            addFileOption("-add-stylesheet", File(projectDir, "src/main/javadoc/themes/prism.css"))
            addBooleanOption("-allow-script-in-comments", true)
            addStringOption("Xdoclint:none", "-quiet")
        }
        exclude(
            "**/dev/robocode/tankroyale/botapi/internal/**",
            "**/dev/robocode/tankroyale/botapi/mapper/**",
            "**/dev/robocode/tankroyale/botapi/util/**",
        )
        doLast {
            Files.copy(
                Paths.get("${layout.projectDirectory}/src/main/javadoc/prism.js"),
                Paths.get("${layout.buildDirectory.get()}/docs/javadoc/prism.js")
            )
        }
    }

    test {
        useJUnitPlatform()
        failFast = true
//        testLogging.showStandardStreams = true
    }

    val uploadDocs by registering(Copy::class) {
        dependsOn(javadoc)

        val javadocDir = "../../docs/api/java"

        delete(javadocDir)
        mkdir(javadocDir)

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from("build/docs/javadoc")
        into(javadocDir)
    }

    val javadocJar = named("javadocJar")
    val sourcesJar = named("sourcesJar")

    publishing {
        publications {
            create<MavenPublication>("bot-api") {
                artifact(shadowJar)
                artifact(javadocJar)
                artifact(sourcesJar)

                groupId = group as String?
                artifactId = base.archivesName.get()
                version

                pom {
                    name.set(javadocTitle)
                    description.set(project.description)
                    url.set("https://github.com/robocode-dev/tank-royale")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("fnl")
                            name.set("Flemming Nørnberg Larsen")
                            organization.set("flemming-n-larsen")
                            organizationUrl.set("https://github.com/flemming-n-larsen")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/robocode-dev/tank-royale.git")
                        developerConnection.set("scm:git:ssh://github.com:robocode-dev/tank-royale.git")
                        url.set("https://github.com/robocode-dev/tank-royale/tree/master")
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["bot-api"])
}
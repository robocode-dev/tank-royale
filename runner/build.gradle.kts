import build.tasks.FatJar

description = "Robocode Tank Royale Battle Runner API"

val archiveTitle = "Robocode Tank Royale Battle Runner"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()

base {
    archivesName = "robocode-tankroyale-runner"
}

val artifactBasePath = "${base.libsDirectory.get()}/${base.archivesName.get()}-${project.version}"
val fatJarPath = "$artifactBasePath-all.jar"

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

dependencies {
    api(project(":lib:client"))
    implementation(project(":lib:intent-diagnostics"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(testLibs.kotest.junit5)
    testImplementation(testLibs.bundles.junit)
    testImplementation(testLibs.assertj)
}

tasks {
    val copyBooterJar by registering(Copy::class) {
        dependsOn(":booter:r8ShrinkTask")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":booter").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-booter-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-booter.jar")
    }

    val copyServerJar by registering(Copy::class) {
        dependsOn(":server:r8ShrinkTask")

        duplicatesStrategy = DuplicatesStrategy.FAIL
        from(project(":server").file("./build/libs"))
        into(file("./build/classes/kotlin/main"))
        include("robocode-tankroyale-server-*.jar")
        exclude("*-javadoc.jar", "*-sources.jar", "*-all.jar")
        rename(".*", "robocode-tankroyale-server.jar")
    }

    val copyJars = register("copyJars") {
        dependsOn(copyBooterJar, copyServerJar)
        outputs.dir(file("./build/classes/kotlin/main"))
    }

    val fatJar by registering(FatJar::class) {
        dependsOn(classes, copyJars)

        inputs.files(copyJars)
        inputs.files(configurations.runtimeClasspath)

        destinationDirectory.set(layout.buildDirectory.dir("libs"))
        archiveFileName.set(fatJarPath)

        title.set(archiveTitle)
    }

    jar {
        enabled = false
        dependsOn(fatJar)
    }

    test {
        useJUnitPlatform {
            excludeTags("integration")
        }
        failFast = true
    }

    val integrationTest by registering(Test::class) {
        description = "Runs integration tests that require a full build (embedded server, booter, sample bots)."
        group = "verification"

        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath

        useJUnitPlatform {
            includeTags("integration")
        }
        failFast = true

        dependsOn(jar, ":sample-bots:java:build", ":sample-bots:csharp:build", ":bot-api:tests:build")

        systemProperty("sampleBots.java.dir", project(":sample-bots:java").layout.buildDirectory.dir("archive").get().asFile.absolutePath)
        systemProperty("sampleBots.csharp.dir", project(":sample-bots:csharp").layout.buildDirectory.dir("archive").get().asFile.absolutePath)
        systemProperty("testBots.java.dir", project(":bot-api:tests").layout.buildDirectory.dir("archive/java").get().asFile.absolutePath)
        systemProperty("testBots.csharp.dir", project(":bot-api:tests").layout.buildDirectory.dir("archive/csharp").get().asFile.absolutePath)
    }

    withType<AbstractPublishToMaven> {
        dependsOn(jar)
    }

    javadoc {
        dependsOn("copyJars")
        title = "$archiveTitle $version"
        source(sourceSets.main.get().allJava)

        (options as StandardJavadocDocletOptions).apply {
            memberLevel = JavadocMemberLevel.PROTECTED
            addStringOption("Xdoclint:none", "-quiet")
        }
    }

    register<Copy>("copyRunnerApiDocs") {
        dependsOn(javadoc)

        onlyIf {
            gradle.startParameter.taskNames.any {
                it.contains("upload-docs") ||
                it == "copyRunnerApiDocs" ||
                it.endsWith(":copyRunnerApiDocs")
            }
        }

        val javadocDir = layout.projectDirectory.dir("../docs/api/runner")

        duplicatesStrategy = DuplicatesStrategy.FAIL

        from(layout.buildDirectory.dir("docs/javadoc"))
        into(javadocDir)

        doFirst {
            delete(javadocDir)
            mkdir(javadocDir)
        }
    }

    val javadocJar = named<Jar>("javadocJar") { dependsOn("copyJars") }
    val sourcesJar = named<Jar>("sourcesJar") { dependsOn("copyJars") }

    val copyRunnerJar by registering(Copy::class) {
        description = "Copies the runner fat JAR to examples/lib/ for source-file execution."
        group = "examples"

        dependsOn(fatJar)

        from(file(fatJarPath))
        into(file("examples/lib"))
        rename(".*", "robocode-tankroyale-runner.jar")
    }

    publishing {
        publications {
            named<MavenPublication>("maven") {
                artifact(file(fatJarPath)) {
                    builtBy(fatJar)
                }
                artifact(javadocJar)
                artifact(sourcesJar)

                pom.name.set(archiveTitle)
            }
        }
    }
}

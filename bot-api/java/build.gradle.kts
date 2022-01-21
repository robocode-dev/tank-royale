import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.nio.file.*

apply(from = "../../groovy.gradle")


val javadocTitle = "Robocode Tank Royale Bot API"
description = "Bot API for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
version = "0.9.11"

val artifactBaseName = "robocode-tankroyale-bot-api"


plugins {
    `java-library`
    `maven-publish`
    idea
    alias(libs.plugins.hidetake.ssh)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("dev.robocode.tankroyale:robocode-tankroyale-schema:0.8.1")
    implementation(libs.gson)
    implementation(libs.gson.extras)
    implementation(libs.nv.i18n)
}

val javadoc = tasks.withType<Javadoc> {
    title = "$javadocTitle $version"
    source(sourceSets.main.get().allJava)

    (options as StandardJavadocDocletOptions).apply {
        memberLevel = JavadocMemberLevel.PUBLIC
        overview = "src/main/javadoc/overview.html"

        addFileOption("-add-stylesheet", File(projectDir, "src/main/javadoc/themes/prism.css"))
        addBooleanOption("-allow-script-in-comments", true)
        addStringOption("Xdoclint:none", "-quiet")
    }

    exclude(
        "**/dev/robocode/tankroyale/botapi/internal/**",
        "**/dev/robocode/tankroyale/botapi/mapper/**",
        "**/dev/robocode/tankroyale/sample/**"
    )
    doLast {
        Files.copy(
            Paths.get("$projectDir/src/main/javadoc/prism.js"),
            Paths.get("$buildDir/docs/javadoc/prism.js"))
    }
}


val fatJar = task<Jar>("fatJar") {
    archiveBaseName.set(artifactBaseName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = javadocTitle
        attributes["Implementation-Version"] = archiveVersion
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    with(tasks["jar"] as CopySpec)
}

tasks {

    named("build") {
        dependsOn(fatJar)
    }

    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveBaseName.set(artifactBaseName)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        archiveBaseName.set(artifactBaseName)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }

    register("uploadDocs") {
        dependsOn(javadocJar)

        doLast {
            ssh.run (delegateClosureOf<RunHandler> {
                session(remotes["sshServer"], delegateClosureOf<SessionHandler> {
                    print("Uploading Javadoc...")

                    val filename = "$artifactBaseName-$version-javadoc.jar"

                    put(hashMapOf("from" to "${project.projectDir}/build/libs/$filename", "into" to "tmp"))

                    execute("rm -rf ~/public_html/tankroyale/api/java_new")
                    execute("rm -rf ~/public_html/tankroyale/api/java_old")

                    execute("unzip ~/tmp/$filename -d ~/public_html/tankroyale/api/java_new")

                    execute("mkdir -p ~/public_html/tankroyale/api/java")
                    execute("mv ~/public_html/tankroyale/api/java ~/public_html/tankroyale/api/java_old")
                    execute("mv ~/public_html/tankroyale/api/java_new ~/public_html/tankroyale/api/java")
                    execute("rm -f ~/tmp/$filename")

                    println("done")
                })
            })
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(fatJar)
            groupId = group as String?
            artifactId = rootProject.name
            version
        }
    }
}
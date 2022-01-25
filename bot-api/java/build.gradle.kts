import dev.robocode.tankroyale.tasks.FatJar
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.nio.file.*

apply(from = "../../groovy.gradle")

group = "dev.robocode.tankroyale"
version = "0.9.11"
description = "Bot API for Robocode Tank Royale"

val javadocTitle = "Robocode Tank Royale Bot API"

val artifactBaseName = "robocode-tankroyale-bot-api"

val archiveFileName = "$buildDir/libs/${artifactBaseName}-$version.jar"

val javadocArchiveFilename = "$artifactBaseName-$version-javadoc.jar"

val buildArchiveDirProvider: Provider<Directory> = layout.buildDirectory.dir("libs")
val buildArchivePath = buildArchiveDirProvider.get().toString()

val apiPath: String by rootProject.extra
val javaApiPath = "$apiPath/java"


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

tasks {
    val fatJar by registering(FatJar::class) {
        dependsOn(classes)

        title.set(javadocTitle)
        outputFilename.set("${artifactBaseName}-${project.version}.jar")
    }

    jar { // Replace jar task
        actions = emptyList()
        finalizedBy(fatJar)
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

                    put(hashMapOf("from" to "$buildArchivePath/$javadocArchiveFilename", "into" to "tmp"))

                    val oldJavaApiPath = javaApiPath + "_old_" + System.currentTimeMillis()
                    val tmpJavaApiPath = javaApiPath + "_tmp"

                    execute("unzip ~/tmp/$javadocArchiveFilename -d $tmpJavaApiPath")
                    execute("rm -f ~/tmp/$javadocArchiveFilename")

                    execute("mv $javaApiPath $oldJavaApiPath")
                    execute("mv $tmpJavaApiPath $javaApiPath")

                    println("done")
                })
            })
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(archiveFileName)
            groupId = group as String?
            artifactId = rootProject.name
            version
        }
    }
}
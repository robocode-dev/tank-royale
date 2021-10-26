import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.session.SessionHandler
import java.nio.file.*

val title = "Robocode Tank Royale Bot API"
description = "Bot API for Robocode Tank Royale"

group = "dev.robocode.tankroyale"
version = "0.9.10"

val artifactBaseName = "robocode-tankroyale-bot-api"

plugins {
    `java-library`
    `maven-publish`
    idea
    id("org.hidetake.ssh") version "2.10.1"
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
    implementation("com.neovisionaries:nv-i18n:1.28")
    implementation("com.google.code.gson:gson:2.8.7")
    implementation("org.danilopianini:gson-extras:0.2.2")
}

tasks.withType<JavaCompile>().configureEach {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(11))
    })
}

val javadoc = tasks.withType<Javadoc> {
    title = "Java Bot API for Robocode Tank Royale $version"
    source(sourceSets.main.get().allJava)
    options.memberLevel = JavadocMemberLevel.PUBLIC
    options.overview = "src/main/javadoc/overview.html"
    (options as StandardJavadocDocletOptions).addFileOption("-add-stylesheet", File(projectDir, "src/main/javadoc/themes/prism.css"))
    (options as StandardJavadocDocletOptions).addBooleanOption("-allow-script-in-comments", true)
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
    exclude(
        "**/dev/robocode/tankroyale/botapi/internal/**",
        "**/dev/robocode/tankroyale/botapi/mapper/**",
        "**/dev/robocode/tankroyale/sample/**"
    )
    doLast {
        Files.copy(Paths.get("$projectDir/src/main/javadoc/prism.js"), Paths.get("$buildDir/docs/javadoc/prism.js"))
    }
}

val fatJar = task<Jar>("fatJar") {
    archiveBaseName.set(artifactBaseName)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Implementation-Title"] = title
        attributes["Implementation-Version"] = archiveVersion
    }
    from(
        configurations.compileClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) },
        configurations.runtimeClasspath.get().filter { it.name.endsWith(".jar") }.map { zipTree(it) }
    )
    with(tasks["jar"] as CopySpec)
}

tasks.named("build") {
    dependsOn(fatJar)
}

lateinit var javadocJar: Any

tasks {
    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveBaseName.set(artifactBaseName)
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }

    val javadocJarLocal by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        archiveBaseName.set(artifactBaseName)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJarLocal)
    }

    javadocJar = javadocJarLocal
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

val sshServer = remotes.create("sshServer") {
    withGroovyBuilder {
        setProperty("host", project.properties["tankroyale.ssh.host"])
        setProperty("port", (project.properties["tankroyale.ssh.port"] as String).toInt())
        setProperty("user", project.properties["tankroyale.ssh.user"])
        setProperty("password", project.properties["tankroyale.ssh.pass"])
    }
}

val uploadJavadoc by tasks.registering {
    dependsOn(javadocJar)

    doLast {
        ssh.run (delegateClosureOf<RunHandler> {
            session(sshServer, delegateClosureOf<SessionHandler> {
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
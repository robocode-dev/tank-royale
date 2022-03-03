import org.jsonschema2pojo.AnnotationStyle
import org.jsonschema2pojo.SourceType
import org.jsonschema2pojo.gradle.JsonSchemaExtension
import java.util.Collections.singletonList

val title = "Robocode Tank Royale Schema"
group = "dev.robocode.tankroyale"
version = libs.versions.tankroyale.get()
description = "Schema for Robocode Tank Royale"

val artifactBaseName = "robocode-tankroyale-schema"
val archiveFileName = "$buildDir/libs/$artifactBaseName-$version.jar"

val schemaPackage = "dev.robocode.tankroyale.schema"

buildscript {
    dependencies {
        classpath(libs.jsonschema2pojo)
    }
}

@Suppress("DSL_SCOPE_VIOLATION") // remove later when IntelliJ supports the `libs.` DSL
plugins {
    `java-library`
    alias(libs.plugins.shadow.jar)
    `maven-publish`
    signing
}

apply(plugin = "jsonschema2pojo")

dependencies {
    implementation(libs.gson)
}

// https://github.com/joelittlejohn/jsonschema2pojo/blob/master/jsonschema2pojo-gradle-plugin/src/main/groovy/org/jsonschema2pojo/gradle/JsonSchemaExtension.groovy
configure<JsonSchemaExtension> {
    setSourceType(SourceType.YAMLSCHEMA.toString())
    setSource(singletonList(File("$projectDir/../schemas")))
    setAnnotationStyle(AnnotationStyle.GSON.toString())
    targetPackage = schemaPackage
}

tasks {
    jar {
        manifest {
            attributes["Implementation-Title"] = title
            attributes["Implementation-Version"] = archiveVersion
            attributes["Implementation-Vendor"] = "robocode.dev"
            attributes["Package"] = schemaPackage
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact("${buildDir}/libs/java-$version-all.jar") {
                    builtBy(shadowJar)
                }
                groupId = group as String?
                artifactId = artifactBaseName
                version

                pom {
                    name.set(title)
                    description
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
    sign(publishing.publications["mavenJava"])
}
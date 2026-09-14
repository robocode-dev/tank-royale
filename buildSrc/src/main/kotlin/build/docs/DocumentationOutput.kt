package build.docs

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import java.nio.file.Path

private const val PAGES_STAGING_DIRECTORY = "pages"

fun Project.documentationOutputDirectory(relativePath: String = ""): Provider<Directory> =
    rootProject.layout.buildDirectory.dir(documentationOutputRelativePath(relativePath))

internal fun documentationOutputRelativePath(relativePath: String): String {
    val stagingRoot = Path.of(PAGES_STAGING_DIRECTORY)
    val outputPath = stagingRoot.resolve(relativePath).normalize()
    require(outputPath.startsWith(stagingRoot)) {
        "Generated documentation output must stay under the root build/$PAGES_STAGING_DIRECTORY directory"
    }
    return outputPath.toString().replace('\\', '/')
}

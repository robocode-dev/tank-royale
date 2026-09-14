package build.docs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DocumentationOutputTest {

    @Test
    @Tag("UD-008")
    fun testUD008_IntegrationPositive_stagesGeneratedDocumentationUnderBuildPages() {
        val publishedDestinations = listOf(
            "" to "pages",
            ".nojekyll" to "pages/.nojekyll",
            "api/dotnet" to "pages/api/dotnet",
            "api/java" to "pages/api/java",
            "api/python" to "pages/api/python",
            "api/runner" to "pages/api/runner",
            "api/typescript" to "pages/api/typescript",
        )

        publishedDestinations.forEach { (relativePath, expectedPath) ->
            assertEquals(expectedPath, documentationOutputRelativePath(relativePath))
        }
    }

    @Test
    @Tag("UD-008")
    fun testUD008_IntegrationNegative_rejectsOutputThatEscapesThePagesStagingRoot() {
        assertThrows(IllegalArgumentException::class.java) {
            documentationOutputRelativePath("../docs")
        }
    }
}

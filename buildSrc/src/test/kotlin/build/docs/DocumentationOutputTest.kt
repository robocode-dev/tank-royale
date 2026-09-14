package build.docs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DocumentationOutputTest {

    @Test
    @Tag("UD-008")
    fun testUD008_IntegrationPositive_stagesGeneratedDocumentationUnderBuildPages() {
        assertEquals("pages/api/java", documentationOutputRelativePath("api/java"))
    }

    @Test
    @Tag("UD-008")
    fun testUD008_IntegrationNegative_rejectsOutputThatEscapesThePagesStagingRoot() {
        assertThrows(IllegalArgumentException::class.java) {
            documentationOutputRelativePath("../docs")
        }
    }
}

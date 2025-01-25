package dev.robocode.tankroyale.gui.ui.svg

object SvgEntityExpander {

    // Regex to find XML entities reference in the format `&reference;`
    private val resolveRegex = Regex("""&(\w+);""")

    // Regex to extract entity definitions from !DOCTYPE section
    // Matches patterns like <ENTITY entityName 'entityValue'>
    private val entityRegex = Regex("""<!ENTITY\s+(\w+)\s+'([^']*)'""")

    // Regex to find the entire DOCTYPE section in an SVG
    private val docTypeRegex = Regex("""<!DOCTYPE[^>]*\[(?:[^]]*])*>""")

    /**
     * Expand XML entities in an SVG string by replacing entity references with their defined values from the
     * DOCTYPE section.
     *
     * @param svg The input SVG string containing potential entity references
     * @return SVG string with entities replaced by their corresponding values
     */
    fun expandEntities(svg: String): String {
        // If no DOCTYPE section is found, return the original SVG
        val docTypeMatch = docTypeRegex.find(svg) ?: return svg

        // Create a map of entity names to their defined values
        val entityMap = mutableMapOf<String, String>()
        entityRegex.findAll(docTypeMatch.value).forEach {
            // First group is entity name, second is entity value
            entityMap[it.groupValues[1]] = it.groupValues[2]
        }

        // Replace all entity references with their corresponding values
        // If an entity is not found in the map, keep the original reference
        return resolveRegex.replace(svg) { match ->
            // First captured group is the entity name
            entityMap[match.groupValues[1]] ?: match.value
        }
    }
}

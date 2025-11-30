package dev.robocode.tankroyale.diagramgen.mermaid

class SequenceDiagramBuilder {
    private val lines = mutableListOf<String>()
    private var indentLevel = 0

    fun message(
        from: String,
        to: String,
        text: String,
        arrow: ArrowStyle = ArrowStyle.SYNCHRONOUS,
        activateTarget: Boolean = false,
        deactivateTarget: Boolean = false
    ) {
        val suffix = buildString {
            append(arrow.symbol)
            if (activateTarget) append("+")
            if (deactivateTarget) append("-")
        }
        addLine("$from$suffix$to: $text")
    }

    fun selfMessage(target: String, text: String) {
        addLine("$target->>$target: $text")
    }

    fun noteOver(vararg participants: String, text: String) {
        require(participants.isNotEmpty()) { "At least one participant is required for a note" }
        val scope = participants.joinToString(",")
        addLine("Note over $scope: $text")
    }

    fun opt(label: String, block: SequenceDiagramBuilder.() -> Unit) {
        addLine("opt $label")
        withinBlock(block)
        addLine("end")
    }

    fun alt(vararg branches: ConditionalBranch) {
        require(branches.isNotEmpty()) { "At least one branch is required" }

        val firstLabel = branches.first().label
        addLine(listOf("alt", firstLabel).filter { !it.isNullOrBlank() }.joinToString(" ") { it.orEmpty() })
        withinBlock(branches.first().block)
        branches.drop(1).forEach { branch ->
            val header = branch.label?.takeIf { it.isNotBlank() }?.let { "else $it" } ?: "else"
            addLine(header)
            withinBlock(branch.block)
        }
        addLine("end")
    }

    fun line(text: String) {
        addLine(text)
    }

    fun build(): String =
        (listOf("sequenceDiagram") + lines).joinToString(System.lineSeparator())

    private fun withinBlock(block: SequenceDiagramBuilder.() -> Unit) {
        indentLevel++
        block()
        indentLevel--
    }

    private fun addLine(text: String) {
        val indent = "    ".repeat(indentLevel)
        lines += indent + text
    }
}

enum class ArrowStyle(val symbol: String) {
    SYNCHRONOUS("->>"),
    ASYNCHRONOUS("-->>")
}

data class ConditionalBranch(val label: String?, val block: SequenceDiagramBuilder.() -> Unit)

fun branch(label: String? = null, block: SequenceDiagramBuilder.() -> Unit) =
    ConditionalBranch(label, block)


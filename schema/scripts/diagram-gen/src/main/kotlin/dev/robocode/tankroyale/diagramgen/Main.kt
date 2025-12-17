package dev.robocode.tankroyale.diagramgen

import dev.robocode.tankroyale.diagramgen.flows.Flows
import dev.robocode.tankroyale.diagramgen.generator.FlowRenderer
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    val parser = ArgParser("diagram-gen")
    val targetOption by parser.option(
        ArgType.String,
        fullName = "output",
        shortName = "o",
        description = "Output file path"
    ).default("-")
    val readmeOption by parser.option(
        ArgType.String,
        fullName = "readme",
        shortName = "r",
        description = "Path to schemas README to update"
    )

    parser.parse(args)

    val renderer = FlowRenderer()

    readmeOption?.let { readmePathString ->
        val readmePath = Path.of(readmePathString)
        var content = Files.readString(readmePath)

        Flows.all.forEach { flow ->
            val replacement = renderBlock(flow.id, renderer.render(flow))
            val regex = Regex("<!-- BEGIN:${flow.id} -->.*?<!-- END:${flow.id} -->", RegexOption.DOT_MATCHES_ALL)
            // If there is no marker present at all, that's an error.
            if (!regex.containsMatchIn(content)) {
                error("Markers for flow '${flow.id}' were not found in ${readmePath.toAbsolutePath()}")
            }
            // Perform the replacement. It's okay if the replacement results in no change
            // (the README was already up-to-date); we only want to fail when markers are missing.
            val newContent = regex.replace(content, replacement)
            content = newContent
        }

        Files.writeString(readmePath, content)
    }

    val output = buildString {
        Flows.all.forEach { flow ->
            appendLine("<!-- BEGIN:${flow.id} -->")
            appendLine("### ${flow.title}")
            appendLine(flow.description)
            appendLine()
            appendLine("```mermaid")
            appendLine(renderer.render(flow))
            appendLine("```")
            appendLine("<!-- END:${flow.id} -->")
            appendLine()
        }
    }

    when {
        targetOption != "-" -> Files.writeString(Path.of(targetOption), output)
        readmeOption == null -> print(output)
    }
}

private fun renderBlock(flowId: String, mermaid: String) = buildString {
    appendLine("<!-- BEGIN:$flowId -->")
    appendLine("```mermaid")
    appendLine(mermaid)
    appendLine("```")
    append("<!-- END:$flowId -->")
}

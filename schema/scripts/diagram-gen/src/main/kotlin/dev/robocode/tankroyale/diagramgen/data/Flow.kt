package dev.robocode.tankroyale.diagramgen.data

data class Flow(
    val id: String,
    val title: String,
    val description: String,
    val participants: List<String>,
    val steps: List<FlowStep>
)

sealed interface FlowStep

data class NoteStep(val participants: List<String>, val text: String) : FlowStep

data class MessageStep(
    val from: String,
    val to: String,
    val text: String,
    val arrow: Arrow = Arrow.SYNC,
    val activateTarget: Boolean = false,
    val deactivateTarget: Boolean = false
) : FlowStep

data class SelfMessageStep(val participant: String, val text: String) : FlowStep

data class ConditionStep(
    val branches: List<Branch>
) : FlowStep

data class OptionalStep(
    val label: String,
    val steps: List<FlowStep>
) : FlowStep

data class Branch(
    val label: String?,
    val steps: List<FlowStep>
)

enum class Arrow { SYNC, ASYNC }

fun optional(label: String, vararg steps: FlowStep) = OptionalStep(label, steps.toList())

fun branch(label: String? = null, vararg steps: FlowStep) = Branch(label, steps.toList())

package dev.robocode.tankroyale.diagramgen.generator

import dev.robocode.tankroyale.diagramgen.data.*
import dev.robocode.tankroyale.diagramgen.mermaid.ArrowStyle
import dev.robocode.tankroyale.diagramgen.mermaid.SequenceDiagramBuilder
import dev.robocode.tankroyale.diagramgen.mermaid.branch
import dev.robocode.tankroyale.diagramgen.mermaid.ConditionalBranch

class FlowRenderer {

    fun render(flow: Flow): String {
        val builder = SequenceDiagramBuilder()
        flow.steps.forEach { step ->
            when (step) {
                is NoteStep -> builder.noteOver(*step.participants.toTypedArray(), text = step.text)
                is MessageStep -> builder.message(
                    from = step.from,
                    to = step.to,
                    text = step.text,
                    arrow = when (step.arrow) {
                        Arrow.SYNC -> ArrowStyle.SYNCHRONOUS
                        Arrow.ASYNC -> ArrowStyle.ASYNCHRONOUS
                    },
                    activateTarget = step.activateTarget,
                    deactivateTarget = step.deactivateTarget
                )
                is SelfMessageStep -> builder.selfMessage(step.participant, step.text)
                is ConditionStep -> builder.alt(
                    *step.branches.map { branch ->
                        ConditionalBranch(branch.label) {
                            branch.steps.forEach { nestedStep -> handleNestedStep(this, nestedStep) }
                        }
                    }.toTypedArray()
                )
                is OptionalStep -> builder.opt(step.label) {
                    step.steps.forEach { nestedStep -> handleNestedStep(this, nestedStep) }
                }
            }
        }
        return builder.build()
    }

    private fun handleNestedStep(builder: SequenceDiagramBuilder, step: FlowStep) {
        when (step) {
            is NoteStep -> builder.noteOver(*step.participants.toTypedArray(), text = step.text)
            is MessageStep -> builder.message(
                from = step.from,
                to = step.to,
                text = step.text,
                arrow = when (step.arrow) {
                    Arrow.SYNC -> ArrowStyle.SYNCHRONOUS
                    Arrow.ASYNC -> ArrowStyle.ASYNCHRONOUS
                },
                activateTarget = step.activateTarget,
                deactivateTarget = step.deactivateTarget
            )
            is SelfMessageStep -> builder.selfMessage(step.participant, step.text)
            is ConditionStep -> builder.alt(
                *step.branches.map { branch ->
                    ConditionalBranch(branch.label) {
                        branch.steps.forEach { nested -> handleNestedStep(this, nested) }
                    }
                }.toTypedArray()
            )
            is OptionalStep -> builder.opt(step.label) {
                step.steps.forEach { nested -> handleNestedStep(this, nested) }
            }
        }
    }
}

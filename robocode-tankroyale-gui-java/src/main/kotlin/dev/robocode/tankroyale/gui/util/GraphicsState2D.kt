package dev.robocode.tankroyale.gui.util

import java.awt.*
import java.awt.geom.AffineTransform

/**
 * This class is used for storing the state of a java.awt.Graphics2D object, which can be restored later.
 *
 * @author Flemming N. Larsen
 */
class Graphics2DState(
    val paint: Paint?,
    val font: Font?,
    val stroke: Stroke?,
    val transform: AffineTransform?,
    val composite: Composite?,
    val clip: Shape?,
    val renderingHints: RenderingHints?,
    val color: Color?,
    val background: Color?
) {

    constructor(g: Graphics2D) : this(
        g.paint,
        g.font,
        g.stroke,
        g.transform,
        g.composite,
        g.clip,
        g.renderingHints,
        g.color,
        g.background
    )

    fun restore(g: Graphics2D) {
        g.paint = paint
        g.font = font
        g.stroke = stroke
        g.transform = transform
        g.composite = composite
        g.clip = clip
        g.setRenderingHints(renderingHints)
        g.color = color
        g.background = background
    }
}
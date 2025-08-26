package dev.robocode.tankroyale.gui.ui.components

import java.awt.*
import java.awt.geom.*
import javax.swing.JComponent

/**
 * Custom JComponent that renders a skull using Java2D primitives.
 * The component is sized to fit the skull exactly without extra padding.
 * @param opacity The opacity level for rendering the skull (0.0f - 1.0f), default is 1.0f (100%)
 */
class SkullComponent(private val opacity: Float = 1.0f) : JComponent() {

    // The actual dimensions of the skull
    private val skullWidth = 14
    private val skullHeight = 16
    
    init {
        // Set the preferred size to match the actual skull dimensions
        preferredSize = Dimension(skullWidth, skullHeight)
        
        // Make the component transparent
        isOpaque = false
    }
    
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        
        val g2d = g.create() as Graphics2D
        
        try {
            // Enable anti-aliasing for smoother rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            
            // Apply opacity to all rendering operations
            g2d.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
            
            // Scale to fit the component size
            val scaleX = width.toDouble() / skullWidth
            val scaleY = height.toDouble() / skullHeight
            g2d.scale(scaleX, scaleY)
            
            // Draw the skull shape (translated to start at 0,0 instead of 5,3)
            drawSkull(g2d)
            
            // Draw the eye sockets
            drawEyeSockets(g2d)
            
            // Draw the nose
            drawNose(g2d)
            
            // Draw the teeth
            drawTeeth(g2d)
            
        } finally {
            g2d.dispose()
        }
    }
    
    private fun drawSkull(g2d: Graphics2D) {
        // Create the skull path (adjusted to start at 0,0 instead of 5,3)
        val skullPath = Path2D.Float()
        skullPath.moveTo(7.0, 0.0)  // 12,3 -> 7,0 (adjusted)
        skullPath.curveTo(2.0, 0.0, 0.0, 3.0, 0.0, 7.0)  // 7,3 5,6 5,10 -> 2,0 0,3 0,7
        skullPath.curveTo(0.0, 9.0, 1.0, 11.0, 3.0, 12.0)  // 5,12 6,14 8,15 -> 0,9 1,11 3,12
        skullPath.lineTo(3.0, 15.0)  // 8,18 -> 3,15
        skullPath.lineTo(5.0, 15.0)  // 10,18 -> 5,15
        skullPath.lineTo(5.0, 16.0)  // 10,19 -> 5,16
        skullPath.lineTo(9.0, 16.0)  // 14,19 -> 9,16
        skullPath.lineTo(9.0, 15.0)  // 14,18 -> 9,15
        skullPath.lineTo(11.0, 15.0)  // 16,18 -> 11,15
        skullPath.lineTo(11.0, 12.0)  // 16,15 -> 11,12
        skullPath.curveTo(13.0, 11.0, 14.0, 9.0, 14.0, 7.0)  // 18,14 19,12 19,10 -> 13,11 14,9 14,7
        skullPath.curveTo(14.0, 3.0, 12.0, 0.0, 7.0, 0.0)  // 19,6 17,3 12,3 -> 14,3 12,0 7,0
        skullPath.closePath()
        
        // Create gradient paint for the skull
        val skullGradient = GradientPaint(
            0f, 0f, Color.WHITE,
            skullWidth.toFloat(), skullHeight.toFloat(), Color(0xDD, 0xDD, 0xDD)
        )
        
        // Fill with gradient
        g2d.paint = skullGradient
        g2d.fill(skullPath)
        
        // Draw outline
        g2d.stroke = BasicStroke(0.5f)
        g2d.color = Color(0x33, 0x33, 0x33)
        g2d.draw(skullPath)
    }
    
    private fun drawEyeSockets(g2d: Graphics2D) {
        // Left eye socket (adjusted from cx=9,cy=10 to cx=4,cy=7)
        g2d.color = Color.BLACK
        g2d.fill(Ellipse2D.Float(2.0f, 4.5f, 4.0f, 5.0f))
        
        // Right eye socket (adjusted from cx=15,cy=10 to cx=10,cy=7)
        g2d.fill(Ellipse2D.Float(8.0f, 4.5f, 4.0f, 5.0f))
    }
    
    private fun drawNose(g2d: Graphics2D) {
        // Nose (adjusted from 12,12 11,14 13,14 to 7,9 6,11 8,11)
        val nosePath = Path2D.Float()
        nosePath.moveTo(7.0, 9.0)
        nosePath.lineTo(6.0, 11.0)
        nosePath.lineTo(8.0, 11.0)
        nosePath.closePath()
        
        g2d.color = Color(0x33, 0x33, 0x33)
        g2d.fill(nosePath)
    }
    
    private fun drawTeeth(g2d: Graphics2D) {
        // Draw teeth (adjusted to start at 5,13 instead of 10,16)
        g2d.color = Color.WHITE
        
        for (i in 0..3) {
            val x = 5.0f + i
            val y = 13.0f
            
            // Fill tooth
            g2d.fillRect(x.toInt(), y.toInt(), 1, 2)
            
            // Draw tooth outline
            g2d.color = Color(0x33, 0x33, 0x33)
            g2d.stroke = BasicStroke(0.2f)
            g2d.drawRect(x.toInt(), y.toInt(), 1, 2)
            g2d.color = Color.WHITE
        }
    }
    
    override fun getMinimumSize(): Dimension = preferredSize
    
    override fun getMaximumSize(): Dimension = preferredSize
}

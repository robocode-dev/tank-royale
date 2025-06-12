package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.graphics.IGraphics;
import dev.robocode.tankroyale.botapi.graphics.SvgGraphics;

/**
 * Manages graphics state for bot visualization.
 */
final class GraphicsState {
    private final SvgGraphics svgGraphics = new SvgGraphics();

    /**
     * Returns the graphics interface for drawing operations.
     *
     * @return The graphics interface.
     */
    public IGraphics getGraphics() {
        return svgGraphics;
    }

    /**
     * Gets the SVG output from the graphics operations.
     *
     * @return String containing SVG markup.
     */
    public String getSvgOutput() {
        return svgGraphics.toSvg();
    }

    /**
     * Clears all drawing operations.
     */
    public void clear() {
        svgGraphics.clear();
    }
}

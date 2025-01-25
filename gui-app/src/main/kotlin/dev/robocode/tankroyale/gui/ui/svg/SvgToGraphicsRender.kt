package dev.robocode.tankroyale.gui.ui.svg

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.parser.DefaultParserProvider
import com.github.weisj.jsvg.parser.LoaderContext
import com.github.weisj.jsvg.parser.SVGLoader
import java.awt.Graphics2D

object SvgToGraphicsRender {

    private const val MIRROR_TEXT_CSS =
        "<style>text {transform-box: fill-box; transform-origin: 50% 50%; transform: scaleY(-1);}</style>"

    private val svgLoader = SVGLoader()
    private val svgLoaderContext = LoaderContext
        .builder()
        .parserProvider(DefaultParserProvider())
        .build()

    fun renderSvgToGraphics(svg: String, g: Graphics2D) {
        // Referenced ENTITY references like &E2; and &E3; are replaced (expanded) with the named ELEMENT definitions
        val svgWithExpandedEntities = SvgEntityExpander.expandEntities(svg)

        // Render the SVG to the Graphics object
        val svgDocument = loadSvg(svgWithExpandedEntities)
        renderSvgWithTransform(g, svgDocument, shouldDisableAutoTransform(svg))
    }

    private fun loadSvg(svg: String): SVGDocument? {
        val svgContent = when (shouldDisableAutoTransform(svg)) {
            true -> svg
            false -> svg.replace(Regex("<\\s*/\\s*svg\\s*>"), "$MIRROR_TEXT_CSS</svg>")
        }

        return svgContent.byteInputStream().buffered().use { inputStream ->
            svgLoader.load(inputStream, null, svgLoaderContext)
        }
    }

    private fun renderSvgWithTransform(g: Graphics2D, svgDocument: SVGDocument?, isAutoTransformOff: Boolean) {
        // By default, origin is already at bottom-left due to previous transforms in drawArena()
        val oldTransform = g.transform
        try {
            if (isAutoTransformOff) {
                g.transform = g.deviceConfiguration.defaultTransform
            }

            // Render SVG scaled to arena dimensions
            svgDocument?.render(null, g)

        } finally {
            g.transform = oldTransform
        }
    }

    private fun shouldDisableAutoTransform(svg: String) =
        svg.contains("<!-- auto-transform: off -->")
}
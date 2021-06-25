import java.awt.*
import java.awt.Transparency.OPAQUE
import java.awt.Transparency.TRANSLUCENT
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.ColorModel
import java.awt.image.Raster

class RoundGradientPaint(
    x: Double,
    y: Double,
    private var radius: Double,
    private var pointColor: Color,
    private var bgColor: Color
) : Paint {
    private var point: Point2D

    override fun getTransparency(): Int {
        return if (pointColor.alpha and bgColor.alpha == 0xff) OPAQUE else TRANSLUCENT
    }

    init {
        if (radius <= 0)
            throw IllegalArgumentException("Radius must be greater than 0")
        point = Point2D.Double(x, y)
    }

    override fun createContext(
        cm: ColorModel,
        deviceBounds: Rectangle,
        userBounds: Rectangle2D,
        xform: AffineTransform,
        hints: RenderingHints
    ): PaintContext {
        val transformedPoint = xform.transform(point, null)
        val transformedRadius = xform.deltaTransform(Point2D.Double(radius, radius), null)
        return RoundGradientContext(transformedPoint, transformedRadius, pointColor, bgColor)
    }
}

internal class RoundGradientContext(
    private var point: Point2D,
    private var radius: Point2D,
    private var color1: Color,
    private var color2: Color
) : PaintContext {
    override fun dispose() {}

    override fun getColorModel(): ColorModel { return ColorModel.getRGBdefault() }

    override fun getRaster(x: Int, y: Int, w: Int, h: Int): Raster {
        val raster = colorModel.createCompatibleWritableRaster(w, h)

        val data = IntArray(w * h * 4)
        for (j in 0 until h) {
            for (i in 0 until w) {
                val distance = point.distance((x + i).toDouble(), (y + j).toDouble())
                val radius = radius.distance(0.0, 0.0)
                var ratio = distance / radius
                if (ratio > 1.0) ratio = 1.0
                val base = (j * w + i) * 4
                data[base + 0] = (color1.red + ratio * (color2.red - color1.red)).toInt()
                data[base + 1] = (color1.green + ratio * (color2.green - color1.green)).toInt()
                data[base + 2] = (color1.blue + ratio * (color2.blue - color1.blue)).toInt()
                data[base + 3] = (color1.alpha + ratio * (color2.alpha - color1.alpha)).toInt()
            }
        }
        raster.setPixels(0, 0, w, h, data)

        return raster
    }
}
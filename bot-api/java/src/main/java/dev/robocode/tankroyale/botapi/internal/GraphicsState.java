
package dev.robocode.tankroyale.botapi.internal;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.StringWriter;
import java.io.Writer;

public class GraphicsState {
    private SVGGraphics2D graphics;
    private Document document;

    public GraphicsState() {
        init();
    }

    void init() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        document = domImpl.createDocument("http://www.w3.org/2000/svg", "svg", null);
        graphics = new SVGGraphics2D(document);
        graphics.setSVGCanvasSize(new Dimension(800, 600)); // Default size
        graphics.setBackground(new Color(0, 0, 0, 0));
    }

    public Graphics2D getGraphics() {
        return graphics;
    }

    public String getSVGOutput() {
        try {
            Writer writer = new StringWriter();
            graphics.stream(writer, true);
            return writer.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public void clear() {
        graphics.dispose();
        init();
    }
}
import { describe, it, expect, beforeEach } from "vitest";
import { SvgGraphics } from "../src/graphics/SvgGraphics";
import { Color } from "../src/graphics/Color";
import { Point } from "../src/graphics/Point";

describe("LEGACY", () => {

describe("SvgGraphics", () => {
  let g: SvgGraphics;

  beforeEach(() => {
    g = new SvgGraphics();
  });

  describe("toSvg (empty)", () => {
    it("wraps in svg element with viewBox", () => {
      expect(g.toSvg()).toBe(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 5000 5000">\n</svg>\n'
      );
    });
  });

  describe("drawLine", () => {
    it("generates correct SVG line element", () => {
      g.setStrokeColor(Color.RED);
      g.setStrokeWidth(2);
      g.drawLine(10, 20, 30, 40);
      expect(g.toSvg()).toContain(
        '<line x1="10" y1="20" x2="30" y2="40" stroke="#FF0000" stroke-width="2" />'
      );
    });

    it("formats decimals with up to 3 places", () => {
      g.drawLine(1.5, 2.25, 3.125, 4.1);
      expect(g.toSvg()).toContain('x1="1.5" y1="2.25" x2="3.125" y2="4.1"');
    });
  });

  describe("drawRectangle", () => {
    it("uses default stroke #000000 and width 1 when none set", () => {
      g.drawRectangle(5, 10, 100, 50);
      expect(g.toSvg()).toContain(
        '<rect x="5" y="10" width="100" height="50" fill="none" stroke="#000000" stroke-width="1" />'
      );
    });

    it("uses set stroke color", () => {
      g.setStrokeColor(Color.BLUE);
      g.setStrokeWidth(3);
      g.drawRectangle(0, 0, 10, 10);
      expect(g.toSvg()).toContain('stroke="#0000FF" stroke-width="3"');
    });
  });

  describe("fillRectangle", () => {
    it("uses fill color", () => {
      g.setFillColor(Color.GREEN);
      g.fillRectangle(0, 0, 20, 20);
      expect(g.toSvg()).toContain('fill="#008000"');
    });
  });

  describe("drawCircle", () => {
    it("uses default stroke when none set", () => {
      g.drawCircle(50, 50, 25);
      expect(g.toSvg()).toContain(
        '<circle cx="50" cy="50" r="25" fill="none" stroke="#000000" stroke-width="1" />'
      );
    });
  });

  describe("fillCircle", () => {
    it("uses fill color", () => {
      g.setFillColor(Color.YELLOW);
      g.fillCircle(10, 10, 5);
      expect(g.toSvg()).toContain('fill="#FFFF00"');
    });
  });

  describe("drawPolygon", () => {
    it("generates polygon with points", () => {
      g.drawPolygon([new Point(0, 0), new Point(10, 0), new Point(5, 10)]);
      expect(g.toSvg()).toContain('points="0,0 10,0 5,10"');
      expect(g.toSvg()).toContain('fill="none"');
    });

    it("ignores polygon with fewer than 3 points", () => {
      g.drawPolygon([new Point(0, 0), new Point(10, 0)]);
      expect(g.toSvg()).not.toContain("<polygon");
    });
  });

  describe("fillPolygon", () => {
    it("generates filled polygon", () => {
      g.setFillColor(Color.RED);
      g.fillPolygon([new Point(0, 0), new Point(10, 0), new Point(5, 10)]);
      expect(g.toSvg()).toContain('fill="#FF0000"');
      expect(g.toSvg()).toContain("<polygon");
    });
  });

  describe("drawText", () => {
    it("generates text element with escaped content", () => {
      g.setStrokeColor(Color.BLACK);
      g.drawText("Hello & <World>", 10, 20);
      expect(g.toSvg()).toContain("Hello &amp; &lt;World&gt;");
      expect(g.toSvg()).toContain('font-family="Arial"');
      expect(g.toSvg()).toContain('font-size="12"');
    });

    it("escapes quotes in text", () => {
      g.drawText('say "hi"', 0, 0);
      expect(g.toSvg()).toContain("say &quot;hi&quot;");
    });
  });

  describe("setFont", () => {
    it("changes font family and size", () => {
      g.setFont("Courier", 16);
      g.drawText("test", 0, 0);
      expect(g.toSvg()).toContain('font-family="Courier"');
      expect(g.toSvg()).toContain('font-size="16"');
    });
  });

  describe("clear", () => {
    it("removes all elements", () => {
      g.drawLine(0, 0, 10, 10);
      g.clear();
      expect(g.toSvg()).toBe(
        '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 5000 5000">\n</svg>\n'
      );
    });
  });

  describe("decimal formatting", () => {
    it("strips trailing zeros", () => {
      g.drawLine(1.0, 2.0, 3.0, 4.0);
      expect(g.toSvg()).toContain('x1="1" y1="2" x2="3" y2="4"');
    });

    it("keeps up to 3 decimal places", () => {
      g.drawLine(1.1234, 0, 0, 0);
      expect(g.toSvg()).toContain('x1="1.123"');
    });
  });
});
});

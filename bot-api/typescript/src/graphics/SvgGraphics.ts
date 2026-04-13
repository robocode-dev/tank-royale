import { Color } from "./Color.js";
import { IGraphics } from "./IGraphics.js";
import { Point } from "./Point.js";

/**
 * Implementation of IGraphics that generates SVG markup.
 */
export class SvgGraphics implements IGraphics {
  private readonly elements: string[] = [];
  private strokeColor = "none";
  private fillColor = "none";
  private strokeWidth = 0;
  private fontFamily = "Arial";
  private fontSize = 12;

  drawLine(x1: number, y1: number, x2: number, y2: number): void {
    this.elements.push(
      `<line x1="${fmt(x1)}" y1="${fmt(y1)}" x2="${fmt(x2)}" y2="${fmt(y2)}" stroke="${this.strokeColor}" stroke-width="${fmt(this.strokeWidth)}" />\n`
    );
  }

  drawRectangle(x: number, y: number, width: number, height: number): void {
    const stroke = this.strokeColor === "none" ? "#000000" : this.strokeColor;
    const sw = this.strokeWidth === 0 ? 1 : this.strokeWidth;
    this.elements.push(
      `<rect x="${fmt(x)}" y="${fmt(y)}" width="${fmt(width)}" height="${fmt(height)}" fill="none" stroke="${stroke}" stroke-width="${fmt(sw)}" />\n`
    );
  }

  fillRectangle(x: number, y: number, width: number, height: number): void {
    this.elements.push(
      `<rect x="${fmt(x)}" y="${fmt(y)}" width="${fmt(width)}" height="${fmt(height)}" fill="${this.fillColor}" stroke="${this.strokeColor}" stroke-width="${fmt(this.strokeWidth)}" />\n`
    );
  }

  drawCircle(x: number, y: number, radius: number): void {
    const stroke = this.strokeColor === "none" ? "#000000" : this.strokeColor;
    const sw = this.strokeWidth === 0 ? 1 : this.strokeWidth;
    this.elements.push(
      `<circle cx="${fmt(x)}" cy="${fmt(y)}" r="${fmt(radius)}" fill="none" stroke="${stroke}" stroke-width="${fmt(sw)}" />\n`
    );
  }

  fillCircle(x: number, y: number, radius: number): void {
    this.elements.push(
      `<circle cx="${fmt(x)}" cy="${fmt(y)}" r="${fmt(radius)}" fill="${this.fillColor}" stroke="${this.strokeColor}" stroke-width="${fmt(this.strokeWidth)}" />\n`
    );
  }

  drawPolygon(points: Point[]): void {
    if (!points || points.length < 3) return;
    const pts = points.map((p) => `${fmt(p.x)},${fmt(p.y)}`).join(" ");
    const stroke = this.strokeColor === "none" ? "#000000" : this.strokeColor;
    const sw = this.strokeWidth === 0 ? 1 : this.strokeWidth;
    this.elements.push(
      `<polygon points="${pts}" fill="none" stroke="${stroke}" stroke-width="${fmt(sw)}" />\n`
    );
  }

  fillPolygon(points: Point[]): void {
    if (!points || points.length < 3) return;
    const pts = points.map((p) => `${fmt(p.x)},${fmt(p.y)}`).join(" ");
    this.elements.push(
      `<polygon points="${pts}" fill="${this.fillColor}" stroke="${this.strokeColor}" stroke-width="${fmt(this.strokeWidth)}" />\n`
    );
  }

  drawText(text: string, x: number, y: number): void {
    const escaped = escapeXml(text);
    this.elements.push(
      `<text x="${fmt(x)}" y="${fmt(y)}" font-family="${this.fontFamily}" font-size="${fmt(this.fontSize)}" fill="${this.strokeColor}">${escaped}</text>\n`
    );
  }

  setStrokeColor(color: Color): void {
    this.strokeColor = color.toHexColor();
  }

  setFillColor(color: Color): void {
    this.fillColor = color.toHexColor();
  }

  setStrokeWidth(width: number): void {
    this.strokeWidth = width;
  }

  setFont(fontFamily: string, fontSize: number): void {
    this.fontFamily = fontFamily;
    this.fontSize = fontSize;
  }

  toSvg(): string {
    let svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 5000 5000">\n`;
    for (const el of this.elements) {
      svg += el;
    }
    svg += `</svg>\n`;
    return svg;
  }

  clear(): void {
    this.elements.length = 0;
  }
}

function fmt(value: number): string {
  // Format with up to 3 decimal places, no trailing zeros
  const s = value.toFixed(3).replace(/\.?0+$/, "");
  return s === "-0" ? "0" : s;
}

function escapeXml(s: string): string {
  if (s == null) return s;
  return s
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

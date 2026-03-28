import { Color } from "./Color.js";
import { Point } from "./Point.js";

/**
 * Interface for graphics context that provides methods for drawing graphics primitives.
 */
export interface IGraphics {
  drawLine(x1: number, y1: number, x2: number, y2: number): void;
  drawRectangle(x: number, y: number, width: number, height: number): void;
  fillRectangle(x: number, y: number, width: number, height: number): void;
  drawCircle(x: number, y: number, radius: number): void;
  fillCircle(x: number, y: number, radius: number): void;
  drawPolygon(points: Point[]): void;
  fillPolygon(points: Point[]): void;
  drawText(text: string, x: number, y: number): void;
  setStrokeColor(color: Color): void;
  setFillColor(color: Color): void;
  setStrokeWidth(width: number): void;
  setFont(fontFamily: string, fontSize: number): void;
  toSvg(): string;
  clear(): void;
}

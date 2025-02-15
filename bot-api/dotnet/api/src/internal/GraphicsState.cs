using System;
using System.Drawing;
using System.Text.RegularExpressions;
using SvgNet;
using SvgNet.Interfaces;

namespace Robocode.TankRoyale.BotApi.Internal;

public class GraphicsState
{
    private SvgGraphics _svgGraphics = new();

    public IGraphics Graphics => _svgGraphics;

    public string GetSvgOutput()
    {
        var str = _svgGraphics.WriteSVGString(5000, 5000); // 5000x5000 is the maximum battlefield size
        str = Regex.Replace(str, @"<rect[^>]*id=""background""[^>]*>", ""); // Remove background rectangle
        return str;
    }

    public void Clear()
    {
        _svgGraphics.Dispose();
        _svgGraphics = new();
    }
}
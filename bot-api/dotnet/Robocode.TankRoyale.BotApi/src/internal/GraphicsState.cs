using System.Drawing;
using System.Text.RegularExpressions;
using SvgNet;
using SvgNet.Interfaces;

namespace Robocode.TankRoyale.BotApi.Internal;

public class GraphicsState
{
    private SvgGraphics _svgGraphics;

    public GraphicsState()
    {
        Init();
    }

    private void Init()
    {
        _svgGraphics = new SvgGraphics();
    }

    public IGraphics Graphics => _svgGraphics;

    public string GetSvgOutput() {
        var str = _svgGraphics.WriteSVGString();
        str = Regex.Replace(str, @"<rect[^>]*id=""background""[^>]*>", ""); // Remove background rectangle
        return str;
    }

    public void Clear()
    {
        _svgGraphics.Dispose();
        Init();
    }
}
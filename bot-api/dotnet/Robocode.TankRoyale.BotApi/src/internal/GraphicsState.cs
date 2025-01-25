using System.Drawing;
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
        _svgGraphics = new SvgGraphics(Color.Transparent);
    }
    
    public IGraphics Graphics => _svgGraphics;

    public string GetSvgOutput() => _svgGraphics.WriteSVGString();

    public void Clear()
    {
        _svgGraphics.Dispose();
        Init();
    }
}
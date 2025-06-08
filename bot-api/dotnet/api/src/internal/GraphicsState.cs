using Robocode.TankRoyale.BotApi.Graphics;

namespace Robocode.TankRoyale.BotApi.Internal;

sealed class GraphicsState
{
    private SvgGraphics _svgGraphics = new();

    public IGraphics Graphics => _svgGraphics;

    public string GetSvgOutput()
    {
        return _svgGraphics.ToSvg();
    }

    public void Clear()
    {
        _svgGraphics.Clear();
    }
}
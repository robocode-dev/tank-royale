using System.IO;
using System.Text;

namespace Robocode.TankRoyale.BotApi.Internal;

class RecordingTextWriter : TextWriter
{
    private readonly TextWriter _textWriter;
    private readonly StringWriter _stringWriter = new();

    public RecordingTextWriter(TextWriter textWriter)
    {
        _textWriter = textWriter;
    }

    public override void Write(char value)
    {
        _textWriter.Write(value);
        _stringWriter.Write(value);
    }

    public override Encoding Encoding => _textWriter.Encoding;

    public string ReadNext()
    {
        var output = _stringWriter.ToString();

        _stringWriter.GetStringBuilder().Clear();

        return output;
    }
}

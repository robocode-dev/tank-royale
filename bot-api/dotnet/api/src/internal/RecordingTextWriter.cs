using System.IO;
using System.Text;

namespace Robocode.TankRoyale.BotApi.Internal;

class RecordingTextWriter : TextWriter
{
    private readonly TextWriter _textWriter;
    private readonly StringWriter _stringWriter = new();
    private readonly object _lock = new();

    public RecordingTextWriter(TextWriter textWriter)
    {
        _textWriter = textWriter;
    }

    public override void Write(char value)
    {
        lock (_lock)
        {
            _textWriter.Write(value);
            _stringWriter.Write(value);
        }
    }

    public override void Write(string value)
    {
        lock (_lock)
        {
            _textWriter.Write(value);
            _stringWriter.Write(value);
        }
    }

    public override void Write(char[] buffer, int index, int count)
    {
        lock (_lock)
        {
            _textWriter.Write(buffer, index, count);
            _stringWriter.Write(buffer, index, count);
        }
    }

    public override void WriteLine(string value)
    {
        lock (_lock)
        {
            _textWriter.WriteLine(value);
            _stringWriter.WriteLine(value);
        }
    }

    public override void Flush()
    {
        lock (_lock)
        {
            _textWriter.Flush();
            _stringWriter.Flush();
        }
    }

    public override void Write(string value)
    {
        _textWriter.Write(value);
        _stringWriter.Write(value);
    }

    public override void Write(char[] buffer, int index, int count)
    {
        _textWriter.Write(buffer, index, count);
        _stringWriter.Write(buffer, index, count);
    }

    public override void WriteLine(string value)
    {
        _textWriter.WriteLine(value);
        _stringWriter.WriteLine(value);
    }

    public override Encoding Encoding => _textWriter.Encoding;

    public string ReadNext()
    {
        lock (_lock)
        {
            var output = _stringWriter.ToString();
            _stringWriter.GetStringBuilder().Clear();
            return output;
        }
    }
}

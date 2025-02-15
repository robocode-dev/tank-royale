using System.IO;
using System.Text;
using System.Web;

namespace Robocode.TankRoyale.BotApi.Internal;

public class RecordingTextWriter : TextWriter
{
    private readonly TextWriter textWriter;
    private readonly StringWriter stringWriter = new();

    public RecordingTextWriter(TextWriter textWriter)
    {
        this.textWriter = textWriter;
    }

    public override void Write(char value)
    {
        textWriter.Write(value);
        stringWriter.Write(value);
    }

    public override Encoding Encoding => textWriter.Encoding;

    public string ReadNext()
    {
        var output = stringWriter.ToString();
        output = HttpUtility.JavaScriptStringEncode(output);

        stringWriter.GetStringBuilder().Clear();

        return output;
    }
}
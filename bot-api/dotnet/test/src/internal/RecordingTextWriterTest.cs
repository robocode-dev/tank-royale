using System;
using System.IO;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

[TestFixture]
public class RecordingTextWriterTest
{
    [Test]
    public void ShouldPreserveNewlineCharactersWithoutEscaping()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("Hello\nWorld\n");
        recordingWriter.Flush();
        var output = recordingWriter.ReadNext();

        // Assert - should contain actual newline characters, not escaped \n
        Assert.That(output, Is.EqualTo("Hello\nWorld\n"));
        Assert.That(output, Does.Contain("\n"));
        Assert.That(output, Does.Not.Contain("\\n"));
    }

    [Test]
    public void ShouldPreserveCarriageReturnCharacters()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("Hello\rWorld");
        recordingWriter.Flush();
        var output = recordingWriter.ReadNext();

        // Assert - should contain actual \r character
        Assert.That(output, Is.EqualTo("Hello\rWorld"));
        Assert.That(output, Does.Contain("\r"));
        Assert.That(output, Does.Not.Contain("\\r"));
    }

    [Test]
    public void ShouldPreserveTabCharactersWithoutEscaping()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("Hello\tWorld");
        recordingWriter.Flush();
        var output = recordingWriter.ReadNext();

        // Assert - should contain actual tab character
        Assert.That(output, Is.EqualTo("Hello\tWorld"));
        Assert.That(output, Does.Contain("\t"));
        Assert.That(output, Does.Not.Contain("\\t"));
    }

    [Test]
    public void ShouldPreserveBackslashes()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("Path: C:\\temp\\file.txt");
        recordingWriter.Flush();
        var output = recordingWriter.ReadNext();

        // Assert - backslashes should be preserved as-is
        Assert.That(output, Is.EqualTo("Path: C:\\temp\\file.txt"));
    }

    [Test]
    public void ShouldPreserveQuotes()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("He said \"Hello\"");
        recordingWriter.Flush();
        var output = recordingWriter.ReadNext();

        // Assert - quotes should be preserved without escaping
        Assert.That(output, Is.EqualTo("He said \"Hello\""));
        Assert.That(output, Does.Not.Contain("\\\""));
    }

    [Test]
    public void ShouldResetOutputAfterReading()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("First");
        recordingWriter.Flush();
        var first = recordingWriter.ReadNext();

        recordingWriter.Write("Second");
        recordingWriter.Flush();
        var second = recordingWriter.ReadNext();

        // Assert
        Assert.That(first, Is.EqualTo("First"));
        Assert.That(second, Is.EqualTo("Second"));
    }

    [Test]
    public void ShouldWriteToUnderlyingWriter()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("Test");
        recordingWriter.Flush();

        // Assert - the underlying writer should also receive the output
        Assert.That(stringWriter.ToString(), Is.EqualTo("Test"));
    }
}

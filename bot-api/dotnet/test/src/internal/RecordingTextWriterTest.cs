using System;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
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

    [Test]
    public void ShouldHandleConcurrentWrites()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);
        const int threadCount = 10;
        const int writesPerThread = 100;

        // Act - multiple threads writing concurrently
        var tasks = Enumerable.Range(0, threadCount).Select(i => Task.Run(() =>
        {
            for (int j = 0; j < writesPerThread; j++)
            {
                recordingWriter.WriteLine($"Thread{i}-Write{j}");
            }
        })).ToArray();

        Task.WaitAll(tasks);
        recordingWriter.Flush();

        var output = recordingWriter.ReadNext();

        // Assert - all lines should be present (order may vary due to concurrency)
        var lines = output.Split(new[] { Environment.NewLine }, StringSplitOptions.RemoveEmptyEntries);
        Assert.That(lines.Length, Is.EqualTo(threadCount * writesPerThread),
            "All writes should be captured without corruption");

        // Each thread should have contributed its writes
        for (int i = 0; i < threadCount; i++)
        {
            var threadLines = lines.Where(l => l.StartsWith($"Thread{i}-")).ToArray();
            Assert.That(threadLines.Length, Is.EqualTo(writesPerThread),
                $"Thread {i} should have all its writes captured");
        }
    }

    [Test]
    public void ShouldHandleWriteLineEfficiently()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.WriteLine("Test message");
        var output = recordingWriter.ReadNext();

        // Assert - WriteLine should write the string and newline
        Assert.That(output, Is.EqualTo("Test message" + Environment.NewLine));
    }

    [Test]
    public void ShouldHandleWriteStringEfficiently()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);

        // Act
        recordingWriter.Write("Test string");
        var output = recordingWriter.ReadNext();

        // Assert - Write(string) should write the entire string at once
        Assert.That(output, Is.EqualTo("Test string"));
    }

    [Test]
    public void ShouldHandleWriteCharArrayEfficiently()
    {
        // Arrange
        using var stringWriter = new StringWriter();
        var recordingWriter = new RecordingTextWriter(stringWriter);
        var chars = new[] { 'T', 'e', 's', 't' };

        // Act
        recordingWriter.Write(chars, 0, chars.Length);
        var output = recordingWriter.ReadNext();

        // Assert - Write(char[], int, int) should write the char array efficiently
        Assert.That(output, Is.EqualTo("Test"));
    }
}

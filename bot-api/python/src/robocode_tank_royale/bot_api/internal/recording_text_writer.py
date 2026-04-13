"""
RecordingTextWriter - Captures stdout/stderr output for sending to server.
This matches the functionality of RecordingPrintStream (Java) and RecordingTextWriter (C#/.NET).
"""

import sys
from io import StringIO
from threading import Lock
from typing import TextIO


class RecordingTextWriter:
    """
    A text writer wrapper that records all written text and forwards it to an underlying writer.
    Thread-safe for concurrent writes (e.g., from print statements in event handlers).
    """

    def __init__(self, text_writer: TextIO):
        """
        Initialize the recording text writer.

        Args:
            text_writer: The underlying text writer to forward output to (e.g., sys.stdout)
        """
        self._text_writer = text_writer
        self._string_writer = StringIO()
        self._lock = Lock()

    def write(self, text: str) -> int:
        """
        Write text to both the underlying writer and the recording buffer.
        Thread-safe.

        Args:
            text: The text to write

        Returns:
            The number of characters written
        """
        with self._lock:
            self._text_writer.write(text)
            self._string_writer.write(text)
            return len(text)

    def flush(self) -> None:
        """Flush both the underlying writer and the recording buffer. Thread-safe."""
        with self._lock:
            self._text_writer.flush()
            self._string_writer.flush()

    def read_next(self) -> str:
        """
        Read and clear the recorded output since the last read.
        Thread-safe.

        Returns:
            The recorded output as a string
        """
        with self._lock:
            output = self._string_writer.getvalue()
            # Clear the buffer
            self._string_writer.close()
            self._string_writer = StringIO()
            return output

    def isatty(self) -> bool:
        """Return whether the underlying writer is a TTY."""
        return self._text_writer.isatty()

    @property
    def encoding(self) -> str:
        """Return the encoding of the underlying writer."""
        return getattr(self._text_writer, 'encoding', 'utf-8')

    @property
    def errors(self):
        """Return the error handling mode of the underlying writer."""
        return getattr(self._text_writer, 'errors', None)


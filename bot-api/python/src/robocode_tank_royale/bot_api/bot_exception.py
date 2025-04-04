class BotException(Exception):
    """
    Represents errors that occur with bot execution.
    """

    def __init__(self, message, cause=None):
        """
        Initializes a new instance of the BotException class with a specified error message
        and an optional cause.

        :param message: The error message that describes the error.
        :param cause: The exception that is the cause of this exception (optional).
        """
        super().__init__(message)
        self.cause = cause

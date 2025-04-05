class BotException(Exception):
    """
    Represents errors that occur with bot execution.
    """

    def __init__(self, message, cause=None):
        """
        Initializes a new instance of the BotException class.

        Args:
            message: The error message that describes the error.
            cause: The exception that is the cause of this exception. Defaults to None.
        """
        super().__init__(message)
        self.cause = cause

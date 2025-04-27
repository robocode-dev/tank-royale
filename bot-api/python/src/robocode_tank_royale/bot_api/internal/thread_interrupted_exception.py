class ThreadInterruptedException(BaseException):
    """
    Exception used for interrupting event handlers.

    This exception is thrown to signal that the current
    event handler has been interrupted deliberately and
    processing should stop so another event can take place.
    """
    pass

from typing import Callable, Optional


class Condition:
    """
    A class used to test whether a specific condition is met.

    This class can be utilized in multiple ways:
    1. To block program execution by using methods like `IBot.wait_for`, which halts
       until a condition is satisfied.
    2. To trigger a custom event by adding a handler using `IBaseBot.add_custom_event`.
       When the condition is fulfilled, the `IBaseBot.on_custom_event` method is triggered.

    ### Example 1: Using a Condition subclass
    Here's an implementation where a condition is defined as a reusable subclass:
    ```python
    class MyBot(Bot):
        def run(self):
            while self.is_running():
                # ...
                self.set_turn_right(90)
                self.wait_for(TurnCompleteCondition(self))
                # ...

    class TurnCompleteCondition(Condition):
        def __init__(self, bot: Bot):
            self.bot = bot

        def test(self) -> bool:
            return self.bot.get_turn_remaining() == 0
    ```

    ### Example 2: Using a lambda expression
    The same behavior can also be achieved using a lambda expression instead
    of a reusable class:
    ```python
    class MyBot(Bot):
        def run(self):
            while self.is_running():
                # ...
                self.set_turn_right(90)
                self.wait_for(Condition(lambda: self.get_turn_remaining() == 0))
                # ...
    ```
    """

    def __init__(self, name: Optional[str] = None, callable: Optional[Callable[[], bool]] = None):
        """
        Initializes a new instance of the Condition class.

        Args:
            name (Optional[str]): The name of the condition (optional). This is useful
                for identifying the condition in custom events handled by
                `IBaseBot.on_custom_event`.
            callable (Optional[Callable[[], bool]]): A callable (e.g., lambda or function)
                that returns `True` if the condition is met, and `False` otherwise.
        """
        self._name = name  # Stores the condition name; private to avoid collisions.
        self._callable = callable  # Stores the callable for evaluating the condition.

    def get_name(self) -> Optional[str]:
        """
        Retrieves the name of the condition, if one has been provided.

        Returns:
            Optional[str]: The name of the condition, or `None` if no name was set.
        """
        return self._name

    def test(self) -> bool:
        """
        Evaluates whether the condition is met.

        This method should be overridden in subclasses to implement custom
        condition logic. Alternatively, a callable can be provided during
        initialization to evaluate the condition.

        Returns:
            bool: `True` if the condition is met; `False` otherwise.

        Note:
            If a callable is provided and raises an exception during execution,
            the method will return `False`.
        """
        if self._callable:
            try:
                return self._callable()
            except Exception:
                # Gracefully handle errors in the callable's execution.
                return False
        return False
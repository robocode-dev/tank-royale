from dataclasses import dataclass
from typing import Callable, Optional


@dataclass(frozen=True, repr=True)
class Condition:
    """
    A class used to test whether a specific condition is met.

    This class can be utilized in multiple ways:

    1. To block program execution by using methods like `IBot.wait_for`, which halts until a condition is satisfied.
    2. To trigger a custom event by adding a handler using `IBaseBot.add_custom_event`. When the condition is fulfilled, the `IBaseBot.on_custom_event` method is triggered.

    Example 1: Using a Condition subclass

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
                return self.bot.turn_remaining == 0

    Example 2: Using a lambda expression

        class MyBot(Bot):
            def run(self):
                while self.is_running():
                    # ...
                    self.set_turn_right(90)
                    self.wait_for(lambda: self.turn_remaining == 0)
                    # ...

    Attributes:
        name (Optional[str]): The name of the condition (optional). This is useful
            for identifying the condition in custom events handled by
            `IBaseBot.on_custom_event`.
        callable (Optional[Callable[[], bool]]): A callable (e.g., lambda or function)
            that returns True if the condition is met, and False otherwise.
    """

    name: Optional[str] = None
    callable: Optional[Callable[[], bool]] = None

    def test(self) -> bool:
        """
        Evaluates whether the condition is met.

        This method should be overridden in subclasses to implement custom
        condition logic. Alternatively, a callable can be provided during
        initialization to evaluate the condition.

        Returns:
            bool: True if the condition is met; False otherwise.

        Note:
            If a callable is provided and raises an exception during execution,
            the method will return False.
        """
        if self.callable:
            try:
                return self.callable()
            except Exception:
                # Gracefully handle errors in the callable's execution.
                return False
        return False
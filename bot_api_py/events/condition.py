from typing import Callable, Optional

class Condition:
    """
    The Condition class is used for testing if a specific condition is met.

    For example, program execution can be blocked by using the
    `IBot.wait_for` method, which will wait until a condition is met. A
    condition can also be used to trigger a custom event by adding a custom
    event handler using the method `IBaseBot.add_custom_event` that will
    trigger `IBaseBot.on_custom_event` when the condition is fulfilled.

    Here is an example of how to use the condition:

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

    Here is another example using the same condition using a lambda expression
    instead of a (reusable) class:

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
        Constructor for initializing a new instance of the Condition class.

        Args:
            name: The name of the condition (optional). Used for identifying
                a specific condition between multiple conditions with the
                `IBaseBot.on_custom_event` event handler.
            callable: A callable (e.g., a lambda function or a function)
                that returns `True` if the condition is met, and `False`
                otherwise.
        """
        self._name = name  # Use _name to avoid name collision with getName()
        self._callable = callable  # Use _callable to avoid name collision with test()

    def get_name(self) -> Optional[str]:
        """
        Returns the name of this condition, if a name has been provided.

        Returns:
            The name of this condition or `None` if no name has been provided.
        """
        return self._name

    def test(self) -> bool:
        """
        Tests if the condition is met.

        You can override this method to let the game use it for testing your
        condition each turn. Alternatively, you can use one of the
        constructors that take a `Callable` instead.

        Returns:
            `True` if the condition is met; `False` otherwise.
        """
        if self._callable:
            try:
                return self._callable()
            except Exception: # Catch exceptions during callable execution
                return False
        return False
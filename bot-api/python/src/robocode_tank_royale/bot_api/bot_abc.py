from typing import Union, Callable
from abc import abstractmethod

from .base_bot_abc import BaseBotABC

class BotABC(BaseBotABC):
    """
    Interface for a bot that extends the core API with convenient methods for movement,
    turning, and firing the gun.
    """

    async def run(self) -> None:
        """
        Used for running a program for the bot.

        Example:
            def run(self):
                while self.is_running():
                    self.forward(100)
                    self.turn_gun_left(360)
                    self.back(100)
                    self.turn_gun_right(360)

        Notes:
            - In this example, the program runs in a loop (as long as the bot is running),
              meaning that it will start moving forward as soon as `turn_gun_right` has completed.
            - When running a loop that could run forever, it is best practice to check if the
              bot is still running (`is_running`) to stop the loop. This ensures the game can
              stop the thread correctly, preventing strange behavior in new rounds.

        References:
            - `is_running`
        """
        pass

    @abstractmethod
    def is_running(self) -> bool:
        """
        Checks if this bot is running.

        Returns:
            bool: True when the bot is running, False otherwise.
        """
        pass

    @abstractmethod
    def set_forward(self, distance: float) -> None:
        """
        Sets the bot to move forward until it has traveled a specific distance from its
        current position or reached an obstacle.

        The movement speed is limited by `set_max_speed`.

        Acceleration/Deceleration:
            - The bot's acceleration and deceleration are controlled by constants:
                - `Constants.ACCELERATION`: Adds 1 unit to the speed per turn while accelerating.
                - `Constants.DECELERATION`: Subtracts 2 units from speed per turn while decelerating.

        Behavior:
            - The method is executed when `go()` is called, allowing you to combine other
              methods (e.g., turning, firing) to occur in parallel in a single turn.
            - For multiple calls to this method, the most recent call before `go()` is executed.
            - Cancels the effect of prior calls to `set_target_speed`, as this method adjusts the
              target speed for each turn until `distance_remaining` is 0.

        Args:
            distance (float): Distance to move forward.
                - If negative, the bot will move backward.
                - If `float('inf')`, the bot will move forward infinitely.
                - If `float('-inf')`, the bot will move backward infinitely.

        References:
            - `forward`, `set_back`, `back`, `distance_remaining`, `set_target_speed`
        """
        pass

    @abstractmethod
    async def forward(self, distance: float) -> None:
        """
        Moves the bot forward until it has traveled a specific distance or reached an obstacle.

        The movement speed is limited by `set_max_speed`.

        Acceleration/Deceleration:
            - The bot's acceleration and deceleration are controlled by constants:
                - `Constants.ACCELERATION`: Adds 1 unit to the speed per turn while accelerating.
                - `Constants.DECELERATION`: Subtracts 2 units from speed per turn while decelerating.

        Behavior:
            - This method executes immediately by internally calling `go()` and blocks execution
              until the movement is complete. Subsequent commands will execute only after this.
              Use setter methods if parallel execution is needed.
            - Cancels the effect of previous calls to `set_target_speed`, `set_forward`, and `set_back`.

        Args:
            distance (float): Distance to move forward.
                - If negative, the bot will move backward.
                - If `float('inf')`, the bot will move forward infinitely.
                - If `float('-inf')`, the bot will move backward infinitely.

        References:
            - `set_forward`, `set_back`, `back`, `distance_remaining`, `set_target_speed`
        """
        pass

    @abstractmethod
    def set_back(self, distance: float) -> None:
        """
        Sets the bot to move backward until it has traveled a specific distance from its
        current position or reached an obstacle.

        The movement speed is limited by `set_max_speed`.

        Acceleration/Deceleration:
            - The bot's acceleration and deceleration are controlled by constants:
                - `Constants.ACCELERATION`: Adds 1 unit to the speed per turn while accelerating.
                - `Constants.DECELERATION`: Subtracts 2 units from speed per turn while decelerating.

        Behavior:
            - The method is executed when `go()` is called, allowing you to combine other
              methods (e.g., turning, firing) to occur in parallel in a single turn.
            - For multiple calls to this method, the most recent call before `go()` is executed.
            - Cancels the effect of prior calls to `set_target_speed`, as this method adjusts the
              target speed for each turn until `distance_remaining` is 0.

        Args:
            distance (float): Distance to move backward.
                - If negative, the bot will move forward.
                - If `float('inf')`, the bot will move backward infinitely.
                - If `float('-inf')`, the bot will move forward infinitely.

        References:
            - `back`, `set_forward`, `forward`, `distance_remaining`, `set_target_speed`
        """
        pass

    @abstractmethod
    async def back(self, distance: float) -> None:
        """
        Moves the bot backward until it has traveled a specific distance from its current position,
        or it is moving into an obstacle. The speed is limited by `setMaxSpeed`.

        When the bot is moving forward, the `Constants.ACCELERATION` determines the acceleration of the
        bot that adds 1 additional unit to the speed per turn while accelerating. However, the bot is
        faster at braking. The `Constants.DECELERATION` determines the deceleration of the bot that
        subtracts 2 units from the speed per turn.

        This call is executed immediately by calling `go` in the code behind. This method will block
        until it has been completed, which can take one to several turns. New commands will first take
        place after this method is completed. If you need to execute multiple commands in parallel, use
        setter methods instead of this blocking method.

        This method will cancel the effect of prior calls to `setTargetSpeed`, `setForward`, and `setBack`
        methods.

        Args:
            distance (float): The distance to move backward. If negative, the bot will move forward.
                If `float('inf')`, the bot will move backward infinitely.
                If `float('-inf')`, the bot will move forward infinitely.

        See Also:
            setForward: Method to set the bot to move forward.
            setBack: Method to set the bot to move backward.
            forward: Method to immediately move the bot forward.
            getDistanceRemaining: Method to get the remaining travel distance.
            setTargetSpeed: Method to set the target speed of the bot.
        """
        pass

    @property
    def distance_remaining(self) -> float:
        """
        Returns the distance remaining until the bot has finished moving after calling set_forward,
        set_back, forward, or back. When the distance remaining reaches 0, the bot has finished its
        current movement.

        When the distance remaining is positive, the bot is moving forward. When the distance
        remaining is negative, the bot is moving backward.

        Returns:
            float: The remaining distance to move before the current movement is completed.
                - If float('inf'), the bot will move forward infinitely.
                - If float('-inf'), the bot will move backward infinitely.

        See Also:
            set_forward, set_back, forward, back
        """
        raise NotImplementedError('distance_remaining not implemented')

    def set_turn_left(self, degrees: float) -> None:
        """
        Set the bot to turn to the left (following the increasing degrees of the
        `unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_) until it has turned the specified
        amount of degrees. This completes when `turn_remaining` returns 0. The amount of degrees
        to turn each turn is limited by `set_max_turn_rate()`.

        This method will first be executed when `go()` is called, making it possible to call
        other set methods after execution. This allows the bot to move, turn the body, radar, gun, and
        fire the gun in parallel during a single turn when `go()` is called. Note that executing multiple
        methods in parallel is only possible by using **setter** methods prior to calling `go()`.

        If this method is called multiple times, only the last call before `go()` is executed is honored.

        This method cancels the effect of prior calls to `set_turn_right()`.

        Args:
            degrees (float): The amount of degrees to turn left. If negative, the bot will turn right.
                If `float('inf')`, the bot will turn left infinitely.
                If `float('-inf')`, the bot will turn right infinitely.

        See:
            - `unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_
            - `set_turn_right`
            - `turn_right`
            - `turn_left`
            - `turn_remaining`
            - `set_turn_rate`
        """
        pass

    async def turn_left(self, degrees: float) -> None:
        """
        Turn the bot to the left (following the increasing degrees of the
        `unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_) until it has turned
        the specified amount of degrees. That is, when `turn_remaining` is 0.
        The amount of degrees to turn each turn is limited by `set_max_turn_rate()`.

        This call is executed immediately by invoking `go()` in the code behind.
        This method will block until it has been completed, which can take one to several turns.
        New commands will first take place after this method is completed.

        If you need to execute multiple commands in parallel, use *setter* methods
        instead of this blocking method.

        This method will cancel the effect of prior calls to `set_turn_left()`
        and `set_turn_right()`.

        Args:
            degrees (float): The amount of degrees to turn left.
                - If negative, the bot will turn right.
                - If `float('inf')`, the bot will turn left infinitely.
                - If `float('-inf')`, the bot will turn right infinitely.

        See Also:
            - `Unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_
            - `set_turn_left()`
            - `set_turn_right()`
            - `turn_right()`
            - `turn_remaining`
            - `set_turn_rate()`
        """
        pass

    def set_turn_right(self, degrees: float) -> None:
        """
        Set the bot to turn to the right (following the decreasing degrees of the
        `unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_) until it turned the specified
        amount of degrees. That is, when `turn_remaining` returns 0. The amount of degrees to
        turn each turn is limited by `set_max_turn_rate()`.

        This method will first be executed when `go()` is called, making it possible to call
        other set methods after execution. This makes it possible to set the bot to move, turn the
        body, radar, gun, and fire the gun in parallel in a single turn when calling `go()`.
        But notice that this is only possible to execute multiple methods in parallel by using
        setter methods only prior to calling `go()`.

        If this method is called multiple times, the last call before `go()` is executed counts.

        This method will cancel the effect of prior calls to `set_turn_left()`.

        Args:
            degrees (float): The amount of degrees to turn right. If negative, the bot will turn left.
                If `float('inf')`, the bot will turn right infinitely.
                If `float('-inf')`, the bot will turn left infinitely.

        See Also:
            set_turn_left
            turn_right
            turn_left
            turn_remaining
            set_turn_rate
        """
        pass

    async def turn_right(self, degrees: float) -> None:
        """
        Turn the bot to the right (following the increasing degrees of the
        `unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_) until it turned the specified
        amount of degrees. That is, when `turn_remaining` returns 0. The amount of degrees to
        turn each turn is limited by `set_max_turn_rate()`.

        This call is executed immediately, and it will block until it has been completed, which can
        take one to several turns. New commands will first take place after this method is completed.
        If you need to execute multiple commands in parallel, use setter methods instead of this
        blocking method.

        This method will cancel the effect of prior calls to `set_turn_left()` and `set_turn_right()`.

        Args:
            degrees (float): The amount of degrees to turn right. If negative, the bot will turn left.
                If `float('inf')`, the bot will turn right infinitely.
                If `float('-inf')`, the bot will turn left infinitely.

        See Also:
            `Unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_
            set_turn_left
            set_turn_right
            turn_left
            turn_remaining
            set_turn_rate
        """
        pass

    @property
    def turn_remaining(self) -> float:
        """
        Returns the remaining turn in degrees until the bot has finished turning after having called
        `set_turn_left()`, `set_turn_right()`, `turn_left()`, or `turn_right()`.
        When the turn remaining has reached 0, the bot has finished turning.

        When the turn remaining is positive, the bot is turning to the left (along the unit circle).
        When the turn remaining is negative, the bot is turning to the right.

        Returns:
            float: The remaining degrees to turn before its current turning is completed.
            If `float('inf')`, the bot will turn left infinitely.
            If `float('-inf')`, the bot will turn right infinitely.

        See Also:
            set_turn_left
            set_turn_right
            turn_left
            turn_right
        """
        raise NotImplementedError('turn_remaining not implemented')

    def set_turn_gun_left(self, degrees: float) -> None:
        """
        Set the gun to turn to the left (following the increasing degrees of the
        `unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_) until it turned the specified
        amount of degrees. That is, when `gun_turn_remaining` returns 0. The amount of degrees
        to turn each turn is limited by `set_max_gun_turn_rate()`.

        This method will first be executed when `go()` is called, making it possible to call
        other set methods after execution. This makes it possible to set the bot to move, turn the
        body, radar, gun, and fire the gun in parallel in a single turn when calling `go()`.
        But notice that this is only possible to execute multiple methods in parallel by using
        setter methods only prior to calling `go()`.

        If this method is called multiple times, the last call before `go()` is executed counts.

        This method will cancel the effect of prior calls to `set_turn_gun_right()`.

        Args:
            degrees (float): The amount of degrees to turn left. If negative, the gun will turn right.
                If `float('inf')`, the gun will turn left infinitely.
                If `float('-inf')`, the gun will turn right infinitely.

        See Also:
            `Unit circle <https://en.wikipedia.org/wiki/Unit_circle>`_
            set_turn_gun_right
            turn_gun_right
            turn_gun_left
            gun_turn_remaining
            set_gun_turn_rate
        """
        pass

    async def turn_gun_left(self, degrees: float) -> None:
        """
        Rotates the gun to the left (increasing degrees of the unit circle) until
        it has turned the specified number of degrees.

        This method is executed immediately and will block until the action is
        completed, which might take several turns. During this time, new commands
        will not take effect. To execute multiple commands in parallel, use the
        corresponding "set" methods instead of this blocking method.

        The turn rate per tick is limited by `set_max_gun_turn_rate`.

        If called, this method cancels any prior calls to `set_turn_gun_left` or
        `set_turn_gun_right`.

        Args:
            degrees (float): The number of degrees to turn the gun left.
                             - If negative, the gun will turn right.
                             - If `float('inf')`, the gun will turn left indefinitely.
                             - If `float('-inf')`, the gun will turn right indefinitely.
        """
        pass

    def set_turn_gun_right(self, degrees: float) -> None:
        """
        Schedules the gun to turn to the right (decreasing degrees of the unit
        circle) until it has turned the specified number of degrees.

        This method will execute when `go()` is called, allowing you to chain
        multiple commands such as moving, turning, and firing together for
        simultaneous execution in a single turn.

        The turn rate per tick is limited by `set_max_gun_turn_rate`.

        If this method is called multiple times before `go()`, only the last call
        will be executed. It also cancels any prior calls to `set_turn_gun_left`.

        Args:
            degrees (float): The number of degrees to turn the gun right.
                             - If negative, the gun will turn left.
                             - If `float('inf')`, the gun will turn right indefinitely.
                             - If `float('-inf')`, the gun will turn left indefinitely.
        """
        pass

    async def turn_gun_right(self, degrees: float) -> None:
        """
        Rotates the gun to the right (decreasing degrees of the unit circle) until
        it has turned the specified number of degrees.

        This method is executed immediately and will block until the action is
        completed, which might take several turns. During this time, new commands
        will not take effect. To execute multiple commands in parallel, use the
        corresponding "set" methods instead of this blocking method.

        The turn rate per tick is limited by `set_max_gun_turn_rate`.

        If called, this method cancels any prior calls to `set_turn_gun_left` or
        `set_turn_gun_right`.

        Args:
            degrees (float): The number of degrees to turn the gun right.
                             - If negative, the gun will turn left.
                             - If `float('inf')`, the gun will turn right indefinitely.
                             - If `float('-inf')`, the gun will turn left indefinitely.
        """
        pass

    @property
    def gun_turn_remaining(self) -> float:
        """
        Gets the remaining turn angle in degrees for the gun to finish its
        current turning action.

        Positive values indicate the gun is turning left (increasing degrees of
        the unit circle), while negative values indicate it is turning right
        (decreasing degrees of the unit circle).

        Returns:
            float: The remaining degrees for the gun to complete the turn.
                   - If `float('inf')`, the gun is turning left indefinitely.
                   - If `float('-inf')`, the gun is turning right indefinitely.
        """
        raise NotImplementedError('gun_turn_remaining not implemented')

    def set_turn_radar_left(self, degrees: float) -> None:
        """
        Schedules the radar to turn to the left (increasing degrees of the unit
        circle) until it has turned the specified number of degrees.

        This method will execute when `go()` is called, allowing you to chain
        multiple commands such as moving, turning, and scanning together for
        simultaneous execution in a single turn.

        The turn rate per tick is limited by `set_max_radar_turn_rate`.

        If this method is called multiple times before `go()`, only the last call
        will be executed. It also cancels any prior calls to `set_turn_radar_right`.

        Args:
            degrees (float): The number of degrees to turn the radar left.
                             - If negative, the radar will turn right.
                             - If `float('inf')`, the radar will turn left indefinitely.
                             - If `float('-inf')`, the radar will turn right indefinitely.
        """
        pass

    async def turn_radar_left(self, degrees: Union[float, int]) -> None:
        """Turn the radar to the left (following the increasing degrees of the
        unit circle) until it has turned the specified amount of degrees. This
        method will block until completed.

        Args:
            degrees (Union[float, int]): The amount of degrees to turn left. If negative, the
                radar will turn right. If positive infinity, the radar will turn left
                infinitely. If negative infinity, the radar will turn right infinitely.

        Notes:
            - The amount of degrees to turn per turn is limited by the maximum radar
              turn rate.
            - New commands will only be processed after this call completes.
            - This will cancel any prior calls to `set_turn_radar_left()` or
              `set_turn_radar_right()`.
        """
        pass

    def set_turn_radar_right(self, degrees: Union[float, int]) -> None:
        """Set the radar to turn right (following the decreasing degrees of the
        unit circle) until it has turned the specified amount of degrees. This
        method does not block but will execute when `go()` is called.

        Args:
            degrees (Union[float, int]): The amount of degrees to turn right. If negative,
                the radar will turn left. If positive infinity, the radar will turn
                right infinitely. If negative infinity, the radar will turn left
                infinitely.

        Notes:
            - The amount of degrees turned per turn is limited by the maximum radar
              turn rate.
            - The last call to this method before `go()` is executed will override any
              previous ones.
            - This cancels prior calls to `set_turn_radar_left()` and itself.
        """
        pass

    async def turn_radar_right(self, degrees: Union[float, int]) -> None:
        """Turn the radar to the right (following the increasing degrees of the
        unit circle) until it has turned the specified amount of degrees. This
        method will block until completed.

        Args:
            degrees (Union[float, int]): The amount of degrees to turn right. If negative, the
                radar will turn left. If positive infinity, the radar will turn right
                infinitely. If negative infinity, the radar will turn left infinitely.

        Notes:
            - The amount of degrees to turn per turn is limited by the maximum radar
              turn rate.
            - New commands will only be processed after this call completes.
            - This will cancel any prior calls to `set_turn_radar_left()` or
              `set_turn_radar_right()`.
        """
        pass

    @property
    def radar_turn_remaining(self) -> float:
        """Get the remaining turn in degrees until the radar has finished its current turn.

        Returns:
            float: The remaining degrees to turn the radar. Positive values indicate
            the radar is turning left (along the unit circle), and negative values
            indicate it is turning right. If positive infinity, the radar will
            turn left infinitely. If negative infinity, the radar will turn right
            infinitely.

        Notes:
            - This applies to the current turn initiated by methods like
              `set_turn_radar_left()`, `set_turn_radar_right()`,
              `turn_radar_left()`, or `turn_radar_right()`.
        """
        raise NotImplementedError('radar_turn_remaining not implemented')

    async def fire(self, firepower: float) -> None:
        """
        Fires the gun in the direction the gun is pointing.

        Note:
            - Firing the gun spends energy. The energy loss is equal to the firepower used.
            - If the bullet hits an opponent bot, the bot gains energy from the bullet hit. Specifically,
              the bot is rewarded with an energy boost of 3 times the firepower.

        Rules:
            - The gun will only fire if the firepower is at or above the minimum (Constants.MIN_FIREPOWER).
            - If the firepower exceeds the maximum (Constants.MAX_FIREPOWER), it is truncated to the maximum allowed value.

        Gun Heat:
            - The gun heats up whenever it is fired and must cool down to zero heat before it can fire again.
            - The heat generated is calculated as `1 + (firepower / 5)`, meaning higher firepower increases cooling time.
            - The gun's cooling rate can be retrieved via `get_gun_cooling_rate()`.

        Energy Usage and Damage:
            - The energy used for firing is subtracted from the bot’s total energy.
            - Damage dealt by the bullet is `4 * firepower`. Additionally, if firepower exceeds 1,
              extra damage is calculated as `2 * (firepower - 1)`.

        Execution:
            - This method is executed immediately and blocks until completion, taking one or several turns.
            - New commands will only take effect after the completion of this method.
            - For executing multiple commands in parallel, it is recommended to use setter methods instead of this blocking method.

        Side Effects:
            - Cancels prior calls to `set_fire`.

        Args:
            firepower (float): The amount of energy spent on firing the gun.
                - Must be greater than Constants.MIN_FIREPOWER.
                - Cannot exceed the energy available to the bot.

        See Also:
            - `on_bullet_fired()`
            - `set_fire()`
            - `get_gun_heat()`
            - `get_gun_cooling_rate()`
        """
        pass

    @abstractmethod
    async def stop(self, overwrite: bool = False) -> None:
        """
        Immediately stops all motion, including the robot's movement, gun rotation,
        and radar movement. Any remaining movement is preserved for future execution
        via `set_resume` or `resume`.

        This is a blocking call and will only return after the stop operation is
        completed. New commands will take effect after this method finishes. To
        perform multiple commands concurrently, use the non-blocking setter methods.

        Args:
            overwrite (bool): If True, overwrites previously saved movement data
                              from a prior call to `stop_with_overwrite` or `set_stop`.
                              If False, behaves equivalently to `stop_with_overwrite(False)`.
        """
        pass

    @abstractmethod
    async def resume(self) -> None:
        """
        Resume the movement prior to calling the `set_stop` or `stop` method.
        This method has no effect if it has already been called.

        This call is executed immediately and blocks until completed. New commands
        will take effect only after this method is completed. To execute multiple
        commands in parallel, use non-blocking setter methods.
        """
        pass

    @abstractmethod
    async def rescan(self) -> None:
        """
        Scan again with the radar. This method is useful if the radar has
        not been turning and is unable to scan bots automatically.

        The last radar direction and sweep angle will be used for rescanning for bots.
        """
        pass

    @abstractmethod
    async def wait_for(self, condition: Callable[[], bool]) -> None:
        """
        Blocks until a condition is met, i.e., when `Condition.test()` returns True.

        Args:
            condition: The condition to be met before this method stops waiting.
        """
        pass

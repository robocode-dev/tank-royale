"""
Builder/factory for creating configurable test bots.

This module provides a TestBotBuilder class that allows tests to create bots
with specific behaviors without creating separate bot classes for each test
scenario. It supports:

- Predefined behavior profiles (passive, aggressive, scanning)
- Custom event handler callbacks for targeted testing
- Fluent API for easy configuration

Example usage:
    bot = (TestBotBuilder()
        .with_behavior(BotBehavior.PASSIVE)
        .on_tick(lambda e: bot.set_turn_rate(5))
        .build(server_url))
"""

from enum import Enum
from typing import Callable, Optional, Any
from robocode_tank_royale.bot_api import Bot, BotInfo
from robocode_tank_royale.bot_api.events import (
    TickEvent,
    ScannedBotEvent,
    HitBotEvent,
    HitWallEvent,
    BulletFiredEvent,
    HitByBulletEvent,
    BulletHitBotEvent,
    DeathEvent,
    RoundStartedEvent,
    RoundEndedEvent,
    GameStartedEvent,
    GameEndedEvent,
)
from robocode_tank_royale.bot_api.constants import MAX_RADAR_TURN_RATE
from tests.test_utils.mocked_server import MockedServer


class BotBehavior(Enum):
    """Predefined bot behavior profiles."""

    PASSIVE = "passive"
    """Does nothing - just calls go() each tick."""

    AGGRESSIVE = "aggressive"
    """Moves forward and fires continuously."""

    SCANNING = "scanning"
    """Continuously rotates radar to scan for enemies."""

    CUSTOM = "custom"
    """Custom behavior defined by callbacks only."""


class TestBotBuilder:
    """
    Builder/factory for creating configurable test bots.

    This builder allows tests to create bots with specific behaviors without
    creating separate bot classes for each test scenario.
    """

    def __init__(self):
        """Initialize the builder with default values."""
        # Bot info configuration
        self._name = "TestBot"
        self._version = "1.0"
        self._authors = ["Test Author"]
        self._description = "Test bot for unit tests"
        self._platform = "Python 3.10"
        self._programming_lang = "Python"

        # Behavior configuration
        self._behavior = BotBehavior.PASSIVE

        # Event callbacks
        self._on_tick_callback: Optional[Callable[[TickEvent], None]] = None
        self._on_scanned_bot_callback: Optional[Callable[[ScannedBotEvent], None]] = None
        self._on_hit_bot_callback: Optional[Callable[[HitBotEvent], None]] = None
        self._on_hit_wall_callback: Optional[Callable[[HitWallEvent], None]] = None
        self._on_bullet_fired_callback: Optional[Callable[[BulletFiredEvent], None]] = None
        self._on_hit_by_bullet_callback: Optional[Callable[[HitByBulletEvent], None]] = None
        self._on_bullet_hit_bot_callback: Optional[Callable[[BulletHitBotEvent], None]] = None
        self._on_death_callback: Optional[Callable[[DeathEvent], None]] = None
        self._on_round_started_callback: Optional[Callable[[RoundStartedEvent], None]] = None
        self._on_round_ended_callback: Optional[Callable[[RoundEndedEvent], None]] = None
        self._on_game_started_callback: Optional[Callable[[GameStartedEvent], None]] = None
        self._on_game_ended_callback: Optional[Callable[[GameEndedEvent], None]] = None
        self._on_run_callback: Optional[Callable[[], None]] = None

    def with_name(self, name: str) -> "TestBotBuilder":
        """
        Set the bot name.

        Args:
            name: The bot name.

        Returns:
            This builder.
        """
        self._name = name
        return self

    def with_version(self, version: str) -> "TestBotBuilder":
        """
        Set the bot version.

        Args:
            version: The bot version.

        Returns:
            This builder.
        """
        self._version = version
        return self

    def with_authors(self, *authors: str) -> "TestBotBuilder":
        """
        Set the bot authors.

        Args:
            authors: The bot authors.

        Returns:
            This builder.
        """
        self._authors = list(authors)
        return self

    def with_description(self, description: str) -> "TestBotBuilder":
        """
        Set the bot description.

        Args:
            description: The bot description.

        Returns:
            This builder.
        """
        self._description = description
        return self

    def with_behavior(self, behavior: BotBehavior) -> "TestBotBuilder":
        """
        Set the bot behavior profile.

        Args:
            behavior: The behavior profile.

        Returns:
            This builder.
        """
        self._behavior = behavior
        return self

    def on_tick(self, callback: Callable[[TickEvent], None]) -> "TestBotBuilder":
        """
        Set the on_tick callback.

        Args:
            callback: The callback to invoke on tick events.

        Returns:
            This builder.
        """
        self._on_tick_callback = callback
        return self

    def on_scanned_bot(self, callback: Callable[[ScannedBotEvent], None]) -> "TestBotBuilder":
        """
        Set the on_scanned_bot callback.

        Args:
            callback: The callback to invoke when a bot is scanned.

        Returns:
            This builder.
        """
        self._on_scanned_bot_callback = callback
        return self

    def on_hit_bot(self, callback: Callable[[HitBotEvent], None]) -> "TestBotBuilder":
        """
        Set the on_hit_bot callback.

        Args:
            callback: The callback to invoke when hitting another bot.

        Returns:
            This builder.
        """
        self._on_hit_bot_callback = callback
        return self

    def on_hit_wall(self, callback: Callable[[HitWallEvent], None]) -> "TestBotBuilder":
        """
        Set the on_hit_wall callback.

        Args:
            callback: The callback to invoke when hitting a wall.

        Returns:
            This builder.
        """
        self._on_hit_wall_callback = callback
        return self

    def on_bullet_fired(self, callback: Callable[[BulletFiredEvent], None]) -> "TestBotBuilder":
        """
        Set the on_bullet_fired callback.

        Args:
            callback: The callback to invoke when a bullet is fired.

        Returns:
            This builder.
        """
        self._on_bullet_fired_callback = callback
        return self

    def on_hit_by_bullet(self, callback: Callable[[HitByBulletEvent], None]) -> "TestBotBuilder":
        """
        Set the on_hit_by_bullet callback.

        Args:
            callback: The callback to invoke when hit by a bullet.

        Returns:
            This builder.
        """
        self._on_hit_by_bullet_callback = callback
        return self

    def on_bullet_hit(self, callback: Callable[[BulletHitBotEvent], None]) -> "TestBotBuilder":
        """
        Set the on_bullet_hit callback.

        Args:
            callback: The callback to invoke when the bot's bullet hits another bot.

        Returns:
            This builder.
        """
        self._on_bullet_hit_bot_callback = callback
        return self

    def on_death(self, callback: Callable[[DeathEvent], None]) -> "TestBotBuilder":
        """
        Set the on_death callback.

        Args:
            callback: The callback to invoke when the bot dies.

        Returns:
            This builder.
        """
        self._on_death_callback = callback
        return self

    def on_round_started(self, callback: Callable[[RoundStartedEvent], None]) -> "TestBotBuilder":
        """
        Set the on_round_started callback.

        Args:
            callback: The callback to invoke when a round starts.

        Returns:
            This builder.
        """
        self._on_round_started_callback = callback
        return self

    def on_round_ended(self, callback: Callable[[RoundEndedEvent], None]) -> "TestBotBuilder":
        """
        Set the on_round_ended callback.

        Args:
            callback: The callback to invoke when a round ends.

        Returns:
            This builder.
        """
        self._on_round_ended_callback = callback
        return self

    def on_game_started(self, callback: Callable[[GameStartedEvent], None]) -> "TestBotBuilder":
        """
        Set the on_game_started callback.

        Args:
            callback: The callback to invoke when the game starts.

        Returns:
            This builder.
        """
        self._on_game_started_callback = callback
        return self

    def on_game_ended(self, callback: Callable[[GameEndedEvent], None]) -> "TestBotBuilder":
        """
        Set the on_game_ended callback.

        Args:
            callback: The callback to invoke when the game ends.

        Returns:
            This builder.
        """
        self._on_game_ended_callback = callback
        return self

    def on_run(self, callback: Callable[[], None]) -> "TestBotBuilder":
        """
        Set the on_run callback.

        Args:
            callback: The callback to invoke in the run() method.

        Returns:
            This builder.
        """
        self._on_run_callback = callback
        return self

    def build(self, server_url: Optional[str] = None) -> Bot:
        """
        Build a bot using the specified server URL.

        Args:
            server_url: The server URL to connect to. If None, uses MockedServer.SERVER_URL.

        Returns:
            A configured bot instance.
        """
        if server_url is None:
            server_url = MockedServer.SERVER_URL

        bot_info = BotInfo(
            name=self._name,
            version=self._version,
            authors=self._authors,
            description=self._description,
            platform=self._platform,
            programming_lang=self._programming_lang,
        )

        # Create the configurable test bot
        return _ConfigurableTestBot(
            bot_info=bot_info,
            server_url=server_url,
            behavior=self._behavior,
            on_tick_callback=self._on_tick_callback,
            on_scanned_bot_callback=self._on_scanned_bot_callback,
            on_hit_bot_callback=self._on_hit_bot_callback,
            on_hit_wall_callback=self._on_hit_wall_callback,
            on_bullet_fired_callback=self._on_bullet_fired_callback,
            on_hit_by_bullet_callback=self._on_hit_by_bullet_callback,
            on_bullet_hit_bot_callback=self._on_bullet_hit_bot_callback,
            on_death_callback=self._on_death_callback,
            on_round_started_callback=self._on_round_started_callback,
            on_round_ended_callback=self._on_round_ended_callback,
            on_game_started_callback=self._on_game_started_callback,
            on_game_ended_callback=self._on_game_ended_callback,
            on_run_callback=self._on_run_callback,
        )


class _ConfigurableTestBot(Bot):
    """Internal bot implementation that delegates to callbacks."""

    def __init__(
        self,
        bot_info: BotInfo,
        server_url: str,
        behavior: BotBehavior,
        on_tick_callback: Optional[Callable[[TickEvent], None]] = None,
        on_scanned_bot_callback: Optional[Callable[[ScannedBotEvent], None]] = None,
        on_hit_bot_callback: Optional[Callable[[HitBotEvent], None]] = None,
        on_hit_wall_callback: Optional[Callable[[HitWallEvent], None]] = None,
        on_bullet_fired_callback: Optional[Callable[[BulletFiredEvent], None]] = None,
        on_hit_by_bullet_callback: Optional[Callable[[HitByBulletEvent], None]] = None,
        on_bullet_hit_bot_callback: Optional[Callable[[BulletHitBotEvent], None]] = None,
        on_death_callback: Optional[Callable[[DeathEvent], None]] = None,
        on_round_started_callback: Optional[Callable[[RoundStartedEvent], None]] = None,
        on_round_ended_callback: Optional[Callable[[RoundEndedEvent], None]] = None,
        on_game_started_callback: Optional[Callable[[GameStartedEvent], None]] = None,
        on_game_ended_callback: Optional[Callable[[GameEndedEvent], None]] = None,
        on_run_callback: Optional[Callable[[], None]] = None,
    ):
        super().__init__(bot_info, server_url)
        self._behavior = behavior
        self._on_tick_callback = on_tick_callback
        self._on_scanned_bot_callback = on_scanned_bot_callback
        self._on_hit_bot_callback = on_hit_bot_callback
        self._on_hit_wall_callback = on_hit_wall_callback
        self._on_bullet_fired_callback = on_bullet_fired_callback
        self._on_hit_by_bullet_callback = on_hit_by_bullet_callback
        self._on_bullet_hit_bot_callback = on_bullet_hit_bot_callback
        self._on_death_callback = on_death_callback
        self._on_round_started_callback = on_round_started_callback
        self._on_round_ended_callback = on_round_ended_callback
        self._on_game_started_callback = on_game_started_callback
        self._on_game_ended_callback = on_game_ended_callback
        self._on_run_callback = on_run_callback

    def run(self) -> None:
        """Main bot loop."""
        # Call custom run callback if provided
        if self._on_run_callback:
            self._on_run_callback()

        # Behavior-specific run logic
        while self.is_running:
            if self._behavior == BotBehavior.PASSIVE:
                # Do nothing, just wait for next turn
                pass
            elif self._behavior == BotBehavior.AGGRESSIVE:
                # Move forward and fire
                self.set_forward(100)
                self.set_fire(1)
            elif self._behavior == BotBehavior.SCANNING:
                # Spin radar continuously
                self.set_radar_turn_rate(MAX_RADAR_TURN_RATE)
            elif self._behavior == BotBehavior.CUSTOM:
                # No default behavior, rely on callbacks
                pass

            self.go()

    def on_tick(self, tick_event: TickEvent) -> None:
        """Handle tick event."""
        if self._on_tick_callback:
            self._on_tick_callback(tick_event)

    def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        """Handle scanned bot event."""
        # Behavior-specific actions
        if self._behavior == BotBehavior.AGGRESSIVE:
            # Turn gun toward scanned bot and fire
            bearing_from_gun = (
                self.bearing_from(scanned_bot_event.x, scanned_bot_event.y)
                - self.gun_direction
                + self.direction
            )
            self.set_turn_gun_left(bearing_from_gun)
            self.set_fire(2)

        if self._on_scanned_bot_callback:
            self._on_scanned_bot_callback(scanned_bot_event)

    def on_hit_bot(self, hit_bot_event: HitBotEvent) -> None:
        """Handle hit bot event."""
        # Behavior-specific actions
        if self._behavior == BotBehavior.AGGRESSIVE:
            # Fire at point-blank range
            self.set_fire(3)

        if self._on_hit_bot_callback:
            self._on_hit_bot_callback(hit_bot_event)

    def on_hit_wall(self, hit_wall_event: HitWallEvent) -> None:
        """Handle hit wall event."""
        # Behavior-specific actions
        if self._behavior == BotBehavior.AGGRESSIVE:
            # Reverse direction when hitting wall
            self.set_forward(-100)

        if self._on_hit_wall_callback:
            self._on_hit_wall_callback(hit_wall_event)

    def on_bullet_fired(self, bullet_fired_event: BulletFiredEvent) -> None:
        """Handle bullet fired event."""
        if self._on_bullet_fired_callback:
            self._on_bullet_fired_callback(bullet_fired_event)

    def on_hit_by_bullet(self, hit_by_bullet_event: HitByBulletEvent) -> None:
        """Handle hit by bullet event."""
        if self._on_hit_by_bullet_callback:
            self._on_hit_by_bullet_callback(hit_by_bullet_event)

    def on_bullet_hit(self, bullet_hit_bot_event: BulletHitBotEvent) -> None:
        """Handle bullet hit bot event."""
        if self._on_bullet_hit_bot_callback:
            self._on_bullet_hit_bot_callback(bullet_hit_bot_event)

    def on_death(self, death_event: DeathEvent) -> None:
        """Handle death event."""
        if self._on_death_callback:
            self._on_death_callback(death_event)

    def on_round_started(self, round_started_event: RoundStartedEvent) -> None:
        """Handle round started event."""
        if self._on_round_started_callback:
            self._on_round_started_callback(round_started_event)

    def on_round_ended(self, round_ended_event: RoundEndedEvent) -> None:
        """Handle round ended event."""
        if self._on_round_ended_callback:
            self._on_round_ended_callback(round_ended_event)

    def on_game_started(self, game_started_event: GameStartedEvent) -> None:
        """Handle game started event."""
        if self._on_game_started_callback:
            self._on_game_started_callback(game_started_event)

    def on_game_ended(self, game_ended_event: GameEndedEvent) -> None:
        """Handle game ended event."""
        if self._on_game_ended_callback:
            self._on_game_ended_callback(game_ended_event)

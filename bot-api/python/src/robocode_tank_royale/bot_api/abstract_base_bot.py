"""Abstract interface for the tank-royale base bot."""

import abc
import math
import traceback
from typing import Any

from PIL import Image

from robocode_tank_royale.bot_api.color import Color
from robocode_tank_royale.bot_api.bullet_state import BulletState
from robocode_tank_royale.bot_api.events.condition import Condition

from robocode_tank_royale.bot_api.events.bot_event import BotEvent
from robocode_tank_royale.bot_api.events.bot_death_event import BotDeathEvent
from robocode_tank_royale.bot_api.events.bullet_fired_event import BulletFiredEvent
from robocode_tank_royale.bot_api.events.bullet_hit_bot_event import BulletHitBotEvent
from robocode_tank_royale.bot_api.events.bullet_hit_bullet_event import BulletHitBulletEvent
from robocode_tank_royale.bot_api.events.bullet_hit_wall_event import BulletHitWallEvent
from robocode_tank_royale.bot_api.events.connection_error_event import ConnectionErrorEvent
from robocode_tank_royale.bot_api.events.connected_event import ConnectedEvent
from robocode_tank_royale.bot_api.events.custom_event import CustomEvent
from robocode_tank_royale.bot_api.events.disconnected_event import DisconnectedEvent
from robocode_tank_royale.bot_api.events.death_event import DeathEvent
from robocode_tank_royale.bot_api.events.game_started_event import GameStartedEvent
from robocode_tank_royale.bot_api.events.game_ended_event import GameEndedEvent
from robocode_tank_royale.bot_api.events.hit_bot_event import HitBotEvent
from robocode_tank_royale.bot_api.events.hit_by_bullet_event import HitByBulletEvent
from robocode_tank_royale.bot_api.events.hit_wall_event import HitWallEvent
from robocode_tank_royale.bot_api.events.round_started_event import RoundStartedEvent
from robocode_tank_royale.bot_api.events.round_ended_event import RoundEndedEvent
from robocode_tank_royale.bot_api.events.scanned_bot_event import ScannedBotEvent
from robocode_tank_royale.bot_api.events.skipped_turn_event import SkippedTurnEvent
from robocode_tank_royale.bot_api.events.team_message_event import TeamMessageEvent
from robocode_tank_royale.bot_api.events.tick_event import TickEvent
from robocode_tank_royale.bot_api.events.won_round_event import WonRoundEvent


class IBaseBot(abc.ABC):
    """Interface containing the core API for a bot."""
    # Maximum size of a team message, which is 32 KB.
    TEAM_MESSAGE_MAX_SIZE: int = 32768  # in bytes
    # The maximum number of team messages that can be sent per turn, which is 10 messages.
    MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN: int = 10
    
    @abc.abstractmethod
    def start(self) -> None:
        """
        The method used to start running the bot. You should call this method from the main method or similar.
        Example:
        def main(argv):
            # Create my_bot
            ...
            my_bot.start()
        """
        pass
    
    @abc.abstractmethod
    def go(self) -> None:
        """
        Commits the current commands (actions), which finalizes the current turn for the bot.
        
        This method must be called once per turn to send the bot actions to the server and must be
        called before the turn timeout occurs. A turn timer is started when the GameStartedEvent
        and TickEvent occurs. If the go() method is called too late, a turn timeout will
        occur and the SkippedTurnEvent will occur, which means that the bot has skipped all
        actions for the last turn. In this case, the server will continue executing the last actions
        received. This could be fatal for the bot due to loss of control over the bot. So make sure that
        go() is called before the turn ends.
        The commands executed when go() is called are set by calling the various setter
        methods prior to calling the go() method: setTurnRate, setGunTurnRate, setTargetSpeed, and setFire.
        
        see getTurnTimeout.
        """
        pass
    
    @abc.abstractmethod
    def get_my_id(self) -> int:
        """Unique id of this bot, which is available when the game has started.
        
        Returns:
            The unique id of this bot.
        """
        pass
    
    @abc.abstractmethod
    def get_variant(self) -> str:
        """The game variant, which is "Tank Royale".
        
        Returns:
            The game variant of Robocode.
        """
        pass

    @abc.abstractmethod
    def get_version(self) -> str:
        """Game version, e.g. "1.0.0".
        
        Returns:
            The game version.
        """
        pass
    
    @abc.abstractmethod
    def get_game_type(self) -> str:
        """Game type, e.g. "melee" or "1v1".
        
        First available when the game has started.

        Returns:
            The game type.
        """
        pass

    @abc.abstractmethod
    def get_arena_width(self) -> int:
        """Width of the arena measured in units.
        
        First available when the game has started.

        Returns:
            The arena width measured in units
        """
        pass

    @abc.abstractmethod
    def get_arena_height(self) -> int:
        """Height of the arena measured in units.
        
        First available when the game has started.

        Returns:
            The arena height measured in units
        """
        pass

    @abc.abstractmethod
    def get_number_of_rounds(self) -> int:
        """The number of rounds in a battle.
        
        First available when the game has started.

        Returns:
            The number of rounds in a battle.
        """
        pass

    @abc.abstractmethod
    def get_gun_cooling_rate(self) -> float:
        """Gun cooling rate.
        
        The gun needs to cool down to a gun heat of zero before the gun can fire.
        The gun cooling rate determines how fast the gun cools down. That is, the gun cooling rate is
        subtracted from the gun heat each turn until the gun heat reaches zero.
        First available when the game has started.

        Returns:
            The gun cooling rate.
        """
        pass

    @abc.abstractmethod
    def get_max_inactivity_turns(self) -> int:
        """The maximum number of inactive turns allowed.
        
        The bot will become zapped by the game for being inactive. Inactive means
        that the bot has taken no action in several turns in a row.
        
        First available when the game has started.

        Returns:
            The maximum number of allowed inactive turns.
        """
        pass

    @abc.abstractmethod
    def get_turn_timeout(self) -> int:
        """The turn timeout in microseconds.
        
        The turn timeout is important as the bot needs to take action by calling go() before
        the turn timeout occurs. As soon as the TickEvent() is triggered, i.e. when onTick() is called,
        you need to call go() to take action before the turn timeout occurs.
        Otherwise, your bot will skip a turn and receive a onSkippedTurn for each turn where
        go() is called too late.
        
        First available when the game has started.

        Returns:
            The turn timeout in microseconds.
        """
        pass

    @abc.abstractmethod
    def get_time_left(self) -> int:
        """The number of microseconds left of this turn before the bot will skip the turn.
        
        Make sure to call go() before the time runs out.

        Returns:
            The amount of time left in microseconds.
        """
        pass

    @abc.abstractmethod
    def get_round_number(self) -> int:
        """Current round number.

        Returns:
            The current round number.
        """
        pass

    @abc.abstractmethod
    def get_turn_number(self) -> int:
        """Current turn number.

        Returns:
            The current turn number.
        """
        pass

    @abc.abstractmethod
    def get_enemy_count(self) -> int:
        """Number of enemies left in the round.

        Returns:
            The number of enemies left in the round.
        """
        pass

    @abc.abstractmethod
    def get_energy(self) -> float:
        """Current energy level.
        
        When the energy level is positive, the bot is alive and active. When the
        energy level is 0, the bot is still alive but disabled. If the bot becomes disabled it will not
        be able to move or take any action. If negative, the bot has been defeated.

        Returns:
            The current energy level.
        """
        pass

    @abc.abstractmethod
    def is_disabled(self) -> bool:
        """Specifies if the bot is disabled, i.e., when the energy is zero.
        
        When the bot is disabled, it is not able to take any action like movement, turning, and firing.

        Returns:
            True if the bot is disabled; False otherwise.
        """
        pass

    @abc.abstractmethod
    def get_x(self) -> float:
        """Current X coordinate of the center of the bot.

        Returns:
            The current X coordinate of the bot.
        """
        pass

    @abc.abstractmethod
    def get_y(self) -> float:
        """Current Y coordinate of the center of the bot.

        Returns:
            The current Y coordinate of the bot.
        """
        pass

    @abc.abstractmethod
    def get_direction(self) -> float:
        """Current driving direction of the bot in degrees.

        Returns:
            The current driving direction of the bot.
        """
        pass

    @abc.abstractmethod
    def get_gun_direction(self) -> float:
        """Current direction of the gun in degrees.

        Returns:
            The current gun direction of the bot.
        """
        pass

    @abc.abstractmethod
    def get_radar_direction(self) -> float:
        """Current direction of the radar in degrees.

        Returns:
            The current radar direction of the bot.
        """
        pass

    @abc.abstractmethod
    def get_speed(self) -> float:
        """The current speed measured in units per turn.
        
        If the speed is positive, the bot moves forward. If negative, the bot moves backward.
        Zero speed means that the bot is not moving from its current position.

        Returns:
            The current speed.
        """
        pass

    @abc.abstractmethod
    def get_gun_heat(self) -> float:
        """Current gun heat.
        
        When the gun is fired it gets heated and will not be able to fire before it has
        been cooled down. The gun is cooled down when the gun heat is zero.
        When the gun has fired the gun heat is set to 1 + (firepower / 5) and will be cooled down by
        the gun cooling rate.

        Returns:
            The current gun heat.
        """
        pass

    @abc.abstractmethod
    def get_bullet_states(self) -> list[BulletState]:
        """Current bullet states.
        
        Keeps track of all the bullets fired by the bot, which are still active on the arena.

        Returns:
            The current bullet states.
        """
        pass

    @abc.abstractmethod
    def get_events(self) -> list[BotEvent]:
        """Returns an ordered list containing all events currently in the bot's event queue.
        
        You might, for example, call this while processing another event.

        Returns:
            an ordered list containing all events currently in the bot's event queue.
        """
        pass

    @abc.abstractmethod
    def clear_events(self) -> None:
        """Clears out any pending events in the bot's event queue immediately."""
        pass

    @abc.abstractmethod
    def get_turn_rate(self) -> float:
        """Returns the turn rate of the bot in degrees per turn.

        Returns:
            The turn rate of the bot.
        """
        pass

    @abc.abstractmethod
    def set_turn_rate(self, turn_rate: float) -> None:
        """Sets the turn rate of the bot, which can be positive and negative.
        
        The turn rate is measured in degrees per turn. The turn rate is added to the current direction of the bot. But it is also
        added to the current direction of the gun and radar. This is because the gun is mounted on the
        body, and hence turns with the body. The radar is mounted on the gun and hence moves with the
        gun. You can compensate for the turn rate of the bot by subtracting the turn rate of the bot
        from the turn rate of the gun and radar. But be aware that the turn limits defined for the gun
        and radar cannot be exceeded.
        The turn rate is truncated to MAX_TURN_RATE if the turn rate exceeds this value.
        If this property is set multiple times, the last value set before go() counts.

        Args:
            turnRate: The new turn rate of the bot in degrees per turn.
        """
        pass

    @abc.abstractmethod
    def get_max_turn_rate(self) -> float:
        """Returns the maximum turn rate of the bot in degrees per turn.

        Returns:
            The maximum turn rate of the bot.
        """
        pass

    @abc.abstractmethod
    def set_max_turn_rate(self, max_turn_rate: float) -> None:
        """Sets the maximum turn rate which applies to turn the bot to the left or right.
        
        The maximum turn rate must be an absolute value from 0 to MAX_TURN_RATE, both values are included.
        If the input turn rate is negative, the max turn rate will be cut to zero. If the input turn rate
        is above MAX_TURN_RATE, the max turn rate will be set to MAX_TURN_RATE.
        
        If for example the max turn rate is set to 5, then the bot will be able to turn left or right with
        a turn rate down to -5 degrees per turn when turning right, and up to 5 degrees per turn when turning left.
        
        This method will first be executed when go() is called making it possible to call
        other set methods after execution. This makes it possible to set the bot to move, turn the
        body, radar, gun, and also fire the gun in parallel in a single turn when calling go().
        But notice that this is only possible to execute multiple methods in parallel by using
        *setter* methods only prior to calling go().
        
        If this method is called multiple times, the last call before go() is executed counts.

        Args:
            max_turn_rate: The new maximum turn rate.
        """
        pass
    
    @abc.abstractmethod
    def get_gun_turn_rate(self) -> float:
        """Returns the gun turn rate in degrees per turn.

        Returns:
            The turn rate of the gun.
        """
        pass

    @abc.abstractmethod
    def set_gun_turn_rate(self, gun_turn_rate: float) -> None:
        """Sets the turn rate of the gun, which can be positive and negative.
        
        The gun turn rate is measured in degrees per turn. The turn rate is added to the current turn direction of the gun.
        But it is also added to the current direction of the radar. This is because the radar is
        mounted on the gun, and hence moves with the gun. You can compensate for the turn rate of the
        gun by subtracting the turn rate of the gun from the turn rate of the radar. But be aware that
        the turn limits defined for the radar cannot be exceeded.
        The gun turn rate is truncated to MAX_GUN_TURN_RATE if the gun turn rate exceeds this value.
        
        If this property is set multiple times, the last value set before go() counts.

        Args:
            gun_turn_rate: The new turn rate of the gun in degrees per turn.
        """
        pass

    @abc.abstractmethod
    def get_max_gun_turn_rate(self) -> float:
        """Returns the maximum gun turn rate in degrees per turn.

        Returns:
            The maximum turn rate of the gun.
        """
        pass

    @abc.abstractmethod
    def set_max_gun_turn_rate(self, max_gun_turn_rate: float) -> None:
        """Sets the maximum turn rate which applies to turn the gun to the left or right.
        
        The maximum turn rate must be an absolute value from 0 to MAX_GUN_TURN_RATE, both values are included.
        If the input turn rate is negative, the max turn rate will be cut to zero. If the input turn
        rate is above MAX_GUN_TURN_RATE, the max turn rate will be set to MAX_GUN_TURN_RATE.
        
        If for example the max gun turn rate is set to 5, then the gun will be able to turn left or
        right with a turn rate down to -5 degrees per turn when turning right and up to 5 degrees per
        turn when turning left.
        
        This method will first be executed when go() is called making it possible to call
        other set methods after execution. This makes it possible to set the bot to move, turn the
        body, radar, gun, and also fire the gun in parallel in a single turn when calling go(}.
        But notice that this is only possible to execute multiple methods in parallel by using
        *setter* methods only prior to calling go().

        If this method is called multiple times, the last call before go() is executed, counts. 

        Args:
            max_gun_turn_rate: The new maximum gun turn rate.
        """
        pass

    @abc.abstractmethod
    def get_radar_turn_rate(self) -> float:
        """Returns the radar turn rate in degrees per turn.

        Returns:
            The turn rate of the radar.
        """
        pass

    @abc.abstractmethod
    def set_radar_turn_rate(self, gun_radar_turn_rate: float) -> None:
        """Sets the turn rate of the radar, which can be positive and negative.
        
        The radar turn rate is measured in degrees per turn. The turn rate is added to the current direction of the radar.
        Note that besides the turn rate of the radar, the turn rates of the bot and gun are also added
        to the radar direction, as the radar moves with the gun, which is mounted on the gun that moves
        with the body. You can compensate for the turn rate of the gun by subtracting the turn rate of
        the bot and gun from the turn rate of the radar. But be aware that the turn limits defined for
        the radar cannot be exceeded.
        
        The radar turn rate is truncated to MAX_RADAR_TURN_RATE if the radar turn rate
        exceeds this value.
        
        If this property is set multiple times, the last value set before go() counts.

        Args:
            gun_radar_turn_rate: The new turn rate of the radar in degrees per turn.
        """
        pass

    @abc.abstractmethod
    def get_max_radar_turn_rate(self) -> float:
        """Returns the maximum radar turn rate in degrees per turn.

        Returns:
            The maximum turn rate of the radar.
        """
        pass

    @abc.abstractmethod
    def set_max_radar_turn_rate(self, max_radar_turn_rate: float) -> None:
        """Sets the maximum turn rate which applies to turn the radar to the left or right.
        
        The maximum turn rate must be an absolute value from 0 to MAX_RADAR_TURN_RATE, both values are
        included. If the input turn rate is negative, the max turn rate will be cut to zero. If the
        input turn rate is above MAX_RADAR_TURN_RATE, the max turn rate will be set to MAX_RADAR_TURN_RATE.
        
        If for example the max radar turn rate is set to 5, then the radar will be able to turn left
        or right with a turn rate down to -5 degrees per turn when turning right and up to 5 degrees
        per turn when turning left.
        
        This method will first be executed when go() is called making it possible to call
        other set methods after execution. This makes it possible to set the bot to move, turn the
        body, radar, gun, and also fire the gun in parallel in a single turn when calling go().
        But notice that this is only possible to execute multiple methods in parallel by using
        *setter* methods only prior to calling go().
        
        If this method is called multiple times, the last call before go() is executed counts.

        Args:
            max_radar_turn_rate: The new maximum radar turn rate.
        """
        pass

    @abc.abstractmethod
    def get_target_speed(self) -> float:
        """Returns the target speed in units per turn.

        Returns:
            The target speed.
        """
        pass

    @abc.abstractmethod
    def set_target_speed(self, target_speed: float) -> None:
        """Sets the new target speed for the bot in units per turn. 

        The target speed is the speed you want to achieve eventually, which could take one to several turns depending on the current speed.
        For example, if the bot is moving forward with max speed, and then must change to move backward
        at full speed, the bot will have to first decelerate/brake its positive speed (moving forward).
        When passing speed of zero, it will then have to accelerate back to achieve max negative speed.
        
        Note that acceleration is 1 unit per turn and deceleration/braking is faster than
        acceleration as it is -2 unit per turn. Deceleration is negative as it is added to the speed
        and hence needs to be negative when slowing down.
        
        The target speed is truncated to MAX_SPEED if the target speed exceeds this value.
        If this property is set multiple times, the last value set before go() counts.
                                                                                
        Args:
            target_speed: The new target speed in units per turn.
        """
        pass

    @abc.abstractmethod
    def get_max_speed(self) -> float:
        """Returns the maximum speed in units per turn.

        Returns:
            The maximum speed.
        """
        pass

    @abc.abstractmethod
    def set_max_speed(self, max_speed: float) -> None:
        """Sets the maximum speed which applies when moving forward and backward.
        
        The maximum speed must be an absolute value from 0 to MAX_SPEED, both values are included. If the input speed
        is negative, the max speed will be cut to zero. If the input speed is above MAX_SPEED,
        the max speed will be set to MAX_SPEED.
        
        If for example the maximum speed is set to 5, then the bot will be able to move backwards
        with a speed down to -5 units per turn and up to 5 units per turn when moving forward.
        
        This method will first be executed when go() is called making it possible to call
        other set methods after execution. This makes it possible to set the bot to move, turn the
        body, radar, gun, and also fire the gun in parallel in a single turn when calling go(). But notice that
        this is only possible to execute multiple methods in parallel by using
        *setter* methods only prior to calling go().
        
        If this method is called multiple times, the last call before go() is executed counts.

        Args:
            max_speed: The new maximum speed.
        """
        pass

    @abc.abstractmethod
    def set_fire(self, firepower: float) -> bool:
        """Sets the gun to fire in the direction that the gun is pointing with the specified firepower.
        
        Firepower is the amount of energy your bot will spend on firing the gun. This means that the
        bot will lose power on firing the gun where the energy loss is equal to the firepower. You
        cannot spend more energy than available from your bot.
        
        The bullet power must be greater than MIN_FIREPOWER and the gun heat zero before the gun can fire.
        
        If the bullet hits an opponent bot, you will gain energy from the bullet hit. When hitting
        another bot, your bot will be rewarded and retrieve an energy boost of 3x firepower.
        
        The gun will only fire when the firepower is at MIN_FIREPOWER or higher. If the
        firepower is more than MAX_FIREPOWER the power will be truncated to the max firepower.
        
        Whenever the gun is fired, the gun is heated and needs to cool down before it can fire
        again. The gun heat must be zero before the gun is able to fire (see getGunHeat()).
        The gun heat generated by firing the gun is 1 + (firepower / 5). Hence, the more firepower used
        the longer it takes to cool down the gun. The gun cooling rate can be read by calling
        getGunCoolingRate().

        The amount of energy used for firing the gun is subtracted from the bots' total energy. The
        amount of damage dealt by a bullet hitting another bot is 4x firepower, and if the firepower is
        greater than 1 it will do an additional 2 x (firepower - 1) damage.
        
        Note that the gun will automatically keep firing at any turn as soon as the gun heat reaches
        zero. It is possible to disable the gun firing by setting the firepower to zero.

        The firepower is truncated to 0 and MAX_FIREPOWER if the firepower exceeds this value.

        If this property is set multiple times, the last value set before go() counts.

        Args:
            firepower: The new firepower.

        Returns:
            True if the cannon can fire, i.e. if there is no gun heat; False otherwise.
        """
        pass

    @abc.abstractmethod
    def get_firepower(self) -> float:
        """Returns the firepower.

        Returns:
            The firepower.
        """
        pass

    @abc.abstractmethod
    def set_rescan(self) -> None:
        """Sets the bot to rescan with the radar.
        
        This method is useful if the radar has not turned, and hence will not automatically scan bots.
        The last radar direction and sweep angle will be used for scanning for bots.
        """
        pass

    @abc.abstractmethod
    def set_fire_assist(self, enable: bool) -> None:
        """Enables or disables fire assistance explicitly.

        Fire assistance is useful for bots with limited aiming capabilities as it will
        help the bot by firing directly at a scanned bot when the gun is fired, which is a
        very simple aiming strategy.

        When fire assistance is enabled the gun will fire towards the center of the
        scanned bot when all these conditions are met:

        * The gun is fired (`set_fire` and `fire`)
        * The radar is scanning a bot *when* firing the gun (`on_scanned_bot`, `set_rescan`, `rescan`)
        * The gun and radar are pointing in the exact the same direction. You can call
          `set_adjust_radar_for_gun_turn(False)` to align the gun and radar and make
          sure not to turn the radar beside the gun.

        The fire assistance feature is provided for backwards compatibility with the
        original Robocode, where robots that are not an `AdvancedRobot` got fire
        assistance per default as the gun and radar cannot be moved independently of
        each other. (The `AdvancedRobot` allows the body, gun, and radar to move
        independent of each other).

        Args:
            enable: Enables fire assistance when set to True, and disable fire
                assistance otherwise.
        """
        pass

    @abc.abstractmethod
    def set_interruptible(self, interruptible: bool) -> None:
        """Controls continuing or restarting the event handler.

        Call this method during an event handler to control continuing or restarting
        the event handler when a new event occurs again for the same event handler
        while processing an earlier event.

        Example:
        ```python
        def on_scanned_bot(e):
            fire(1)
            set_interruptible(True)  # When a new bot is scanned while moving forward
                                    # this handler will restart from the top as this
                                    # event handler has been set to be interruptible
                                    # right after firing. Without set_interruptible(True),
                                    # new scan events would not be triggered while moving
                                    # forward.
            forward(100)           # We'll only get here if we do not see a robot
                                    # during the move.
            print("No bots were scanned")
        ```

        Args:
            interruptible: True if the event handler should be interrupted and hence
                restart when a new event of the same event type occurs again; False
                otherwise where the event handler will continue processing.
        """
        pass

    @abc.abstractmethod
    def set_adjust_gun_for_body_turn(self, adjust: bool) -> None:
        """Sets the gun to adjust for the bot´s turn.

        This makes the gun behave like it is turning independent of the bot´s turn.

        The gun is mounted on the bot´s body. So, normally, if the bot turns, the
        gun will turn with it. To compensate for this, you can adjust the gun for
        the bot´s turn. When this is set, the gun will turn independent of the
        bot´s turn.

        Note: This property is additive until you reach the maximum the gun can
        turn. The "adjust" is added to the amount you set for turning the bot by
        the turn rate, then capped by the physics of the game.

        Note: The gun compensating this way does count as "turning the gun".

        Args:
            adjust: True if the gun must adjust/compensate for the body turning;
                False if the gun must turn with the body turning (default).
        """
        pass

    @abc.abstractmethod
    def is_adjust_gun_for_body_turn(self) -> bool:
        """Checks if the radar is set to adjust for the body turning.

        This method returns True if the radar is set to turn independently of the
        body's turn. Otherwise, False is returned, meaning that the radar is set
        to turn with the body turning.

        Returns:
            True if the radar is set to turn independently of the body turning;
            False if the radar is set to turn with the body turning (default).
        """
        pass

    @abc.abstractmethod
    def set_adjust_radar_for_body_turn(self, adjust: bool) -> None:
        """Sets the radar to adjust for the body's turn.

        Makes the radar turn independently of the bot's body turn.  The radar is
        mounted on the gun, and the gun is mounted on the bot's body. So,
        normally, if the bot turns, the gun and radar turn with it. To
        compensate for this, you can adjust the radar for the body turn. When
        this is set, the radar will turn independently of the body's turn.

        Note: This property is additive until you reach the maximum the radar
        can turn. The "adjust" is added to the amount you set for turning the
        body by the body turn rate, then capped by the physics of the game.

        Note: The radar compensating this way does count as "turning the radar".

        Args:
            adjust: True if the radar must adjust/compensate for the body's turn;
                False if the radar must turn with the body turning (default).
        """
        pass

    @abc.abstractmethod
    def is_adjust_radar_for_body_turn(self) -> bool:
        """Checks if the radar is set to adjust for the body turning.

        Returns True if the radar is set to turn independently of the body's
        turn. Otherwise, False is returned, meaning that the radar is set to
        turn with the body turning.
        """
        pass

    @abc.abstractmethod
    def set_adjust_radar_for_gun_turn(self, adjust: bool) -> None:
        """Sets the radar to adjust for the gun's turn.

        Makes the radar turn independently of the gun's turn. The radar is
        mounted on the gun. So, normally, if the gun turns, the radar turns
        with it. To compensate for this, you can adjust the radar for the gun
        turn. When this is set, the radar will turn independently of the
        gun's turn.

        Note: This property is additive until you reach the maximum the radar
        can turn. The "adjust" is added to the amount you set for turning the
        gun by the gun turn rate, then capped by the physics of the game.

        When the radar compensates this way it counts as "turning the radar",
        even when it is not explicitly turned by calling a method for turning
        the radar.

        Note: This method automatically disables fire assistance when set to
        True, and automatically enables fire assistance when set to False. This
        is *not* the case for `set_adjust_gun_for_body_turn` and
        `set_adjust_radar_for_body_turn`. Read more about fire assistance with
        the `set_fire_assist` method.

        Args:
            adjust: True if the radar must adjust/compensate for the gun turning;
                False if the radar must turn with the gun turning (default).
        """
        pass

    @abc.abstractmethod
    def is_adjust_radar_for_gun_turn(self) -> bool:
        """Checks if the radar is set to adjust for the gun turning.

        Returns True if the radar is set to turn independently of the gun's
        turn. Otherwise, False is returned, meaning that the radar is set to
        turn with the gun's turn.
        """
        pass

    @abc.abstractmethod
    def add_custom_event(self, condition: Condition) -> bool:
        """Adds a custom event handler.

        This handler will be automatically triggered when the given condition's
        `test` method returns True.  The `on_custom_event` method will be called.

        Args:
            condition: The condition that must be met to trigger the custom event.

        Returns:
            True if the condition was not added already; False if the condition
            was already added.
        """
        pass

    @abc.abstractmethod
    def remove_custom_event(self, condition: Condition) -> bool:
        """Removes a custom event handler.

        Removes triggering a custom event handler for a specific condition that
        was previously added with `add_custom_event`.

        Args:
            condition: The condition that was previously added.

        Returns:
            True if the condition was found; False if the condition was not found.
        """
        pass

    @abc.abstractmethod
    def set_stop(self, overwrite: bool = False) -> None:
        """Sets the bot to stop all movement.

        This includes turning the gun and radar. The remaining movement is saved
        for a call to `set_resume`. This method has no effect if it has already
        been called.

        This method will first be executed when `go` is called, making it
        possible to call other set methods before execution. This makes it
        possible to set the bot to move, turn the body, radar, gun, and also
        fire the gun in parallel in a single turn when calling `go`. But
        notice that this is only possible to execute multiple methods in
        parallel by using *setter* methods only prior to calling `go`.

        Args:
            overwrite: If True, the movement saved by a previous call to this
                method is overridden with the current movement. When False (the default),
                this method is identical to `set_stop()`.
        """
        pass

    @abc.abstractmethod
    def set_resume(self) -> None:
        """Sets the bot to resume movement.

        This method is useful when the bot movement has stopped, e.g., when `set_stop()` has been called.
        The last radar direction and sweep angle will be used for rescanning for bots.

        This method will first be executed when `go` is called, making it
        possible to call other set methods before execution. This makes it
        possible to set the bot to move, turn the body, radar, gun, and also
        fire the gun in parallel in a single turn when calling `go`. But
        notice that this is only possible to execute multiple methods in
        parallel by using *setter* methods only prior to calling `go`.

        """
        pass

    @abc.abstractmethod
    def get_teammate_ids(self) -> set[int]:
        """Returns the ids of all teammates.

        Returns:
            The ids of all teammates if the bot is participating in a team or
            the empty set if the bot is not in a team.
        """
        pass

    @abc.abstractmethod
    def is_teammate(self, bot_id: int) -> bool:
        """Checks if the provided bot id is a teammate or not.

        Example:
        ```python
        def on_scanned_bot(event):
            if is_teammate(event.get_scanned_bot_id()):
                return  # don't do anything by leaving
            fire(1)
        ```

        Args:
            bot_id: The id of the bot to check for.

        Returns:
            True if the provided id is an id of a teammate; False otherwise.
        """
        pass

    @abc.abstractmethod
    def broadcast_team_message(self, message: Any) -> None:
        """Broadcasts a message to all teammates.

        When the message is sent, it is serialized into a JSON representation,
        meaning that all public fields, and only public fields, are being
        serialized into a JSON representation as a DTO (data transfer object).

        The maximum team message size limit is defined by
        `TEAM_MESSAGE_MAX_SIZE`, which is set to
        `TEAM_MESSAGE_MAX_SIZE` bytes. This size is the size of the message
        when it is serialized into a JSON representation.

        The maximum number of messages that can be sent/broadcast per turn is
        limited to `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN`.

        Args:
            message: The message to broadcast.

        Raises:
            ValueError: If the size of the message exceeds the size limit.
        """
        pass

    @abc.abstractmethod
    def send_team_message(self, teammate_id: int, message: Any) -> None:
        """Sends a message to a specific teammate.

        When the message is sent, it is serialized into a JSON representation,
        meaning that all public fields, and only public fields, are being
        serialized into a JSON representation as a DTO (data transfer object).

        The maximum team message size limit is defined by
        `TEAM_MESSAGE_MAX_SIZE`, which is set to
        `TEAM_MESSAGE_MAX_SIZE` bytes. This size is the size of the message
        when it is serialized into a JSON representation.

        The maximum number of messages that can be sent/broadcast per turn is
        limited to `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN`.

        Args:
            teammate_id: The id of the teammate to send the message to.
            message: The message to send.

        Raises:
            ValueError: If the size of the message exceeds the size limit.
        """
        pass

    @abc.abstractmethod
    def is_stopped(self) -> bool:
        """Checks if the movement has been stopped.

        Returns:
            True if the movement has been stopped by `set_stop()`; False
            otherwise.
        """
        pass

    @abc.abstractmethod
    def get_body_color(self) -> Color:
        """Returns the color of the body.

        Returns:
            The color of the body or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_body_color(self, color: Color) -> None:
        """Sets the color of the body.

        Colors can (only) be changed each turn.

        Example:
        ```python
        set_body_color(Color.RED)       # the red color
        set_body_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the body or None if the bot must use the
                default color instead.
        """
        pass

    @abc.abstractmethod
    def get_turret_color(self) -> Color:
        """Returns the color of the gun turret.

        Returns:
            The color of the turret or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_turret_color(self, color: Color) -> None:
        """Sets the color of the gun turret.

        Colors can (only) be changed each turn.

        Example:
        ```python
        set_turret_color(Color.RED)       # the red color
        set_turret_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the gun turret or None if the bot must use the
                default color instead.
        """
        pass

    @abc.abstractmethod
    def get_radar_color(self) -> Color:
        """Returns the color of the radar.

        Returns:
            The color of the radar or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_radar_color(self, color: Color) -> None:
        """Sets the color of the radar.

        Colors can (only) be changed each turn.

        Example:
        ```python
        set_radar_color(Color.RED)       # the red color
        set_radar_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the radar or None if the bot must use the
                default color instead.
        """
        pass

    @abc.abstractmethod
    def get_bullet_color(self) -> Color:
        """Returns the color of the fired bullets.

        Returns:
            The color of the bullets or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_bullet_color(self, color: Color) -> None:
        """Sets the color of the fired bullets.

        Colors can (only) be changed each turn.

        Note that a fired bullet will not change its color after it has been
        fired. But new bullets fired after setting the bullet color will get
        the new color.

        Example:
        ```python
        set_bullet_color(Color.RED)       # the red color
        set_bullet_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the fired bullets or None if the bot must use
                the default color instead.
        """
        pass

    @abc.abstractmethod
    def get_scan_color(self) -> Color:
        """Returns the color of the scan arc.

        Returns:
            The color of the scan arc or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_scan_color(self, color: Color) -> None:
        """Sets the color of the scan arc.

        Colors can (only) be changed each turn.

        Example:
        ```python
        set_scan_color(Color.RED)       # the red color
        set_scan_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the scan arc or None if the bot must use the
                default color instead.
        """
        pass

    @abc.abstractmethod
    def get_tracks_color(self) -> Color:
        """Returns the color of the tracks.

        Returns:
            The color of the tracks or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_tracks_color(self, color: Color) -> None:
        """Sets the color of the tracks.

        Colors can (only) be changed each turn.

        Example:
        ```python
        set_tracks_color(Color.RED)       # the red color
        set_tracks_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the tracks or None if the bot must use the
                default color instead.
        """
        pass

    @abc.abstractmethod
    def get_gun_color(self) -> Color:
        """Returns the color of the gun.

        Returns:
            The color of the gun or None if no color has been set yet,
            meaning that the default color will be used.
        """
        pass

    @abc.abstractmethod
    def set_gun_color(self, color: Color) -> None:
        """Sets the color of the gun.

        Colors can (only) be changed each turn.

        Example:
        ```python
        set_gun_color(Color.RED)       # the red color
        set_gun_color(Color(255, 0, 0))  # also the red color
        ```

        Args:
            color: The color of the gun or None if the bot must use the
                default color instead.
        """
        pass

    @abc.abstractmethod
    def is_debugging_enabled(self) -> bool:
        """Checks if graphical debugging is enabled.

        Returns:
            True if graphics debugging is enabled; False otherwise.
        """
        pass

    @abc.abstractmethod
    def get_graphics(self) -> Image.Image:
        """Gets a graphics object for debug painting.

        Example:
        ```python
        g = get_graphics()
        draw = PIL.ImageDraw(g)
        g.rectangle(50, 50, 100, 100, fill=(0, 0, 255))  # A blue filled rect
        ```

        Returns:
            A graphics canvas to use for painting graphical objects, making
            debugging easier.
        """
        pass

    def on_connected(self, connected_event: ConnectedEvent) -> None:
        """Handles the connected event.

        This event handler is triggered when connected to the server.

        Args:
            connected_event: The event details from the game.
        """
        print(f"Connected to: {connected_event.server_uri}")  # Use f-string

    def on_disconnected(self, disconnected_event: DisconnectedEvent) -> None:
        """Handles the disconnected event.

        This event handler is triggered when disconnected from the server.

        Args:
            disconnected_event: The event details from the game.
        """
        msg = f"Disconnected from: {disconnected_event.server_uri}" # Use f-string
        if disconnected_event.status_code is not None:
            msg += f", status code: {disconnected_event.status_code}"
        if disconnected_event.reason is not None:
            msg += f", reason: {disconnected_event.reason}"

        print(msg)

    def on_connection_error(self, connection_error_event: ConnectionErrorEvent) -> None:
        """Handles the connection error event.

        This event handler is triggered when a connection error occurs.

        Args:
            connection_error_event: The event details from the game.
        """
        print(f"Connection error with {connection_error_event.server_uri}")

        if connection_error_event.error is not None:
            traceback.print_exc(connection_error_event.error) # Print the exception

    def on_game_started(self, game_started_event: GameStartedEvent) -> None:
        """Handles the game started event.

        This event handler is triggered when a game has started.

        Args:
            game_started_event: The event details from the game.
        """
        pass

    def on_game_ended(self, game_ended_event: GameEndedEvent) -> None:
        """Handles the game ended event.

        This event handler is triggered when a game has ended.

        Args:
            game_ended_event: The event details from the game.
        """
        pass

    def on_round_started(self, round_started_event: RoundStartedEvent) -> None:
        """Handles the round started event.

        This event handler is triggered when a new round has started.

        Args:
            round_started_event: The event details from the game.
        """
        pass

    def on_round_ended(self, round_ended_event: RoundEndedEvent) -> None:
        """Handles the round ended event.

        This event handler is triggered when a round has ended.

        Args:
            round_ended_event: The event details from the game.
        """
        pass

    def on_tick(self, tick_event: TickEvent) -> None:
        """Handles the tick event.

        This event handler is triggered when a game tick event occurs, i.e.,
        when a new turn in a round has started.

        Args:
            tick_event: The event details from the game.
        """
        pass

    def on_bot_death(self, bot_death_event: BotDeathEvent) -> None:
        """Handles the bot death event.

        This event handler is triggered when another bot has died.

        Args:
            bot_death_event: The event details from the game.
        """
        pass

    def on_death(self, death_event: DeathEvent) -> None:
        """Handles the death event.

        This event handler is triggered when this bot has died.

        Args:
            death_event: The event details from the game.
        """
        pass
    
    def on_hit_bot(self, bot_hit_bot_event: HitBotEvent) -> None:
        """Handles the bot hit bot event.

        This event handler is triggered when the bot has collided with another
        bot.

        Args:
            bot_hit_bot_event: The event details from the game.
        """
        pass

    def on_hit_wall(self, bot_hit_wall_event: HitWallEvent) -> None:
        """Handles the bot hit wall event.

        This event handler is triggered when the bot has hit a wall.

        Args:
            bot_hit_wall_event: The event details from the game.
        """
        pass

    def on_bullet_fired(self, bullet_fired_event: BulletFiredEvent) -> None:
        """Handles the bullet fired event.

        This event handler is triggered when the bot has fired a bullet.

        Args:
            bullet_fired_event: The event details from the game.
        """
        pass

    def on_hit_by_bullet(self, hit_by_bullet_event: HitByBulletEvent) -> None:
        """Handles the hit by bullet event.

        This event handler is triggered when the bot has been hit by a bullet.

        Args:
            hit_by_bullet_event: The event details from the game.
        """
        pass

    def on_bullet_hit(self, bullet_hit_bot_event: BulletHitBotEvent) -> None:
        """Handles the bullet hit bot event.

        This event handler is triggered when the bot's bullet has hit another
        bot.

        Args:
            bullet_hit_bot_event: The event details from the game.
        """
        pass

    def on_bullet_hit_bullet(self, bullet_hit_bullet_event: BulletHitBulletEvent) -> None:
        """Handles the bullet hit bullet event.

        This event handler is triggered when a bullet fired from the bot has
        collided with another bullet.

        Args:
            bullet_hit_bullet_event: The event details from the game.
        """
        pass

    def on_bullet_hit_wall(self, bullet_hit_wall_event: BulletHitWallEvent) -> None:
        """Handles the bullet hit wall event.

        Triggered when a bullet has hit a wall.

        Args:
            bullet_hit_wall_event: The event details from the game.
        """
        pass

    def on_scanned_bot(self, scanned_bot_event: ScannedBotEvent) -> None:
        """Handles the scanned bot event.

        Triggered when the bot has scanned another bot.

        Args:
            scanned_bot_event: The event details from the game.
        """
        pass

    def on_skipped_turn(self, skipped_turn_event: SkippedTurnEvent) -> None:
        """Handles the skipped turn event.

        Triggered when the bot has skipped a turn (taken no action).  This
        occurs if `go()` was not called before the turn timeout.  If the bot
        skips multiple turns, it receives a `SkippedTurnEvent` for each turn.
        The server uses the newest received instructions for target speed,
        turn rates, firing, etc.

        Args:
            skipped_turn_event: The event details from the game.
        """
        pass

    def on_won_round(self, won_round_event: WonRoundEvent) -> None:
        """Handles the won round event.

        Triggered when the bot has won a round.

        Args:
            won_round_event: The event details from the game.
        """
        pass

    def on_custom_event(self, custom_event: CustomEvent) -> None:
        """Handles the custom event.

        Triggered when some condition has been met. Use `condition.name` to
        differentiate between different types of conditions.

        Args:
            custom_event: The event details from the game.
        """
        pass

    def on_team_message(self, team_message_event: TeamMessageEvent) -> None:
        """Handles the team message event.

        Triggered when the bot has received a message from a teammate.

        Args:
            team_message_event: The event details from the game.
        """
        pass

    @abc.abstractmethod
    def calc_max_turn_rate(self, speed: float) -> float:
        """Calculates the maximum turn rate for a specific speed.

        Args:
            speed: The speed.

        Returns:
            The maximum turn rate determined by the given speed.
        """
        pass

    @abc.abstractmethod
    def calc_bullet_speed(self, firepower: float) -> float:
        """Calculates the bullet speed given a firepower.

        Args:
            firepower: The firepower.

        Returns:
            The bullet speed determined by the given firepower.
        """
        pass

    @abc.abstractmethod
    def calc_gun_heat(self, firepower: float) -> float:
        """Calculates gun heat after having fired the gun.

        Args:
            firepower: The firepower used when firing the gun.

        Returns:
            The gun heat produced when firing the gun with the given
            firepower.
        """
        pass
    
    @abc.abstractmethod
    def get_event_priority(self, event_class: type) -> int:
        """Returns the event priority for a specific event class.

        Example:
        ```python
        scanned_bot_event_priority = get_event_priority(ScannedBotEvent)
        ```

        Args:
            event_class: The event class to get the event priority for.

        Returns:
            The event priority for a specific event class.
        """
        pass

    @abc.abstractmethod
    def set_event_priority(self, event_class: type, priority: int) -> None:
        """Changes the event priority for an event class.

        The event priority determines which event types (classes) are handled
        before others. Events with higher priorities are handled first.

        Note: You should normally not need to change the event priority.

        Args:
            event_class: The event class to change the event priority for.
            priority: The new priority (typically 1-150; higher value = higher
                priority).
        """
        pass

    def calc_bearing(self, direction: float) -> float:
        """Calculates the bearing (delta angle) relative to the bot's direction.

        `bearing = calc_bearing(direction) = normalize_relative_angle(direction - get_direction())`

        Args:
            direction: The input direction.

        Returns:
            A bearing (delta angle) in the range [-180, 180).
        """
        return self.normalize_relative_angle(direction - self.get_direction())

    def calc_gun_bearing(self, direction: float) -> float:
        """Calculates the bearing (delta angle) relative to the gun's direction.

        `bearing = calc_gun_bearing(direction) = normalize_relative_angle(direction - get_gun_direction())`

        Args:
            direction: The input direction.

        Returns:
            A bearing (delta angle) in the range [-180, 180).
        """
        return self.normalize_relative_angle(direction - self.get_gun_direction())
    
    def calc_radar_bearing(self, direction: float) -> float:
        """Calculates the bearing (delta angle) relative to the radar's direction.

        `bearing = calc_radar_bearing(direction) = normalize_relative_angle(direction - get_radar_direction())`

        Args:
            direction: The input direction.

        Returns:
            A bearing (delta angle) in the range [-180, 180).
        """
        return self.normalize_relative_angle(direction - self.get_radar_direction())

    def direction_to(self, x: float, y: float) -> float:
        """Calculates the absolute direction (angle) to a point (x, y).

        Args:
            x: The x coordinate of the point.
            y: The y coordinate of the point.

        Returns:
            The direction to the point (x, y) in the range [0, 360).
        """
        return self.normalize_absolute_angle(math.degrees(math.atan2(y - self.get_y(), x - self.get_x())))

    def bearing_to(self, x: float, y: float) -> float:
        """Calculates the bearing (delta angle) to a point (x, y).

        Calculates the bearing relative to the bot's current direction.

        Args:
            x: The x coordinate of the point.
            y: The y coordinate of the point.

        Returns:
            The bearing to the point (x, y) in the range [-180, 180).
        """
        return self.normalize_relative_angle(self.direction_to(x, y) - self.get_direction())

    def gun_bearing_to(self, x: float, y: float) -> float:
        """Calculates the bearing (delta angle) to a point (x, y) from the gun.

        Args:
            x: The x coordinate of the point.
            y: The y coordinate of the point.

        Returns:
            The bearing to the point (x, y) in the range [-180, 180).
        """
        return self.normalize_relative_angle(self.direction_to(x, y) - self.get_gun_direction())

    def radar_bearing_to(self, x: float, y: float) -> float:
        """Calculates the bearing (delta angle) to a point (x, y) from the radar.

        Args:
            x: The x coordinate of the point.
            y: The y coordinate of the point.

        Returns:
            The bearing to the point (x, y) in the range [-180, 180).
        """
        return self.normalize_relative_angle(self.direction_to(x, y) - self.get_radar_direction())

    def distance_to(self, x: float, y: float) -> float:
        """Calculates the distance to a point (x, y).

        Args:
            x: The x coordinate of the point.
            y: The y coordinate of the point.

        Returns:
            The distance to the point (x, y).
        """
        return math.hypot(x - self.get_x(), y - self.get_y())

    def normalize_absolute_angle(self, angle: float) -> float:
        """Normalizes an angle to the range [0, 360).

        Args:
            angle: The angle to normalize.

        Returns:
            The normalized absolute angle.
        """
        angle %= 360
        return angle if angle >= 0 else angle + 360

    def normalize_relative_angle(self, angle: float) -> float:
        """Normalizes an angle to the range [-180, 180).

        Args:
            angle: The angle to normalize.

        Returns:
            The normalized relative angle.
        """
        angle %= 360
        if angle >= 0:
            return angle if angle < 180 else angle - 360
        else:
            return angle if angle >= -180 else angle + 360

    def calc_delta_angle(self, target_angle: float, source_angle: float) -> float:
        """Calculates the difference between two angles.

        The delta angle will be in the range [-180, 180].

        Args:
            target_angle: The target angle.
            source_angle: The source angle.

        Returns:
            The delta angle.
        """
        angle = target_angle - source_angle
        if angle > 180:
            angle -= 360
        elif angle < -180:
            angle += 360
        return angle

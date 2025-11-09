from collections import deque
from threading import Lock


from ..events import CustomEvent, BotEvent, TickEvent
from .base_bot_internal_data import BaseBotInternalData
from .bot_event_handlers import BotEventHandlers
from .event_interruption import EventInterruption
from .event_priorities import EventPriorities
from .thread_interrupted_exception import ThreadInterruptedException


class EventQueue:
    """Queue containing bot events that are being prioritized and dispatched to event handlers.
    
    The event queue makes sure that the events are being processed in the right order based on event
    priority and age of the events. Old events that are no longer relevant will be removed from the
    queue.
    """
    MAX_QUEUE_SIZE = 256
    MAX_EVENT_AGE = 2

    def __init__(self, base_bot_internal_data: BaseBotInternalData, bot_event_handlers: BotEventHandlers):
        self.base_bot_internal_data = base_bot_internal_data
        self.bot_event_handlers = bot_event_handlers
        self.events: deque[BotEvent] = deque()
        self.events_lock = Lock()
        self.current_top_event: None | BotEvent= None
        self.current_top_event_priority = float('-inf')

    def clear(self) -> None:
        """Clears all events in the queue and custom event conditions."""
        self.clear_events()
        self.base_bot_internal_data.conditions.clear()  # conditions might be added in the bots run() method each round
        self.current_top_event_priority = float('-inf')

    def get_events(self, turn_number: int) -> list[BotEvent]:
        """Returns a list containing all events in the queue.
        
        Args:
            turn_number: Current turn number used for removing old events.
        Returns:
            List of all events in the queue.
        """
        self.remove_old_events(turn_number)
        with self.events_lock:
            return list(self.events)

    def clear_events(self):
        """Removes all events in the queue."""
        with self.events_lock:
            self.events.clear()

    def set_current_event_interruptible(self, interruptible: bool) -> None:
        """Sets if the current event can be interrupted by new events with higher priority.
        
        Args:
            interruptible: True if the current event can be interrupted; false otherwise.
        """
        assert self.current_top_event is not None, "No current event to set interruptible state for."
        EventInterruption.set_interruptible(type(self.current_top_event), interruptible)

    def is_current_event_interruptible(self) -> bool:
        """Checks if the current event can be interrupted by new events with higher priority.
        
        Returns:
            True if the current event can be interrupted; false otherwise.
        """
        assert self.current_top_event is not None, "No current event to check interruptibility for."
        return EventInterruption.is_interruptible(type(self.current_top_event))

    def add_events_from_tick(self, event: TickEvent) -> None:
        """Adds standard events from a tick event, and custom events from conditions.
        
        Args:
            event: The tick event containing the standard events to add.
        """
        self.add_event(event)
        for e in event.events:
            self.add_event(e)

        self.add_custom_events()

    async def dispatch_events(self, turn_number: int) -> None:
        """Dispatches events in prioritized order to event handlers.
        
        Args:
            turn_number: Current turn number used for removing old events.
        """
        #        dumpEvents(turn_number); // for debugging purposes

        self.remove_old_events(turn_number)
        self.sort_events()

        while self.is_bot_running():
            current_event = self.get_next_event()
            if current_event is None:
                break
            if self.is_same_event(current_event):
                if self.is_current_event_interruptible():
                    EventInterruption.set_interruptible(type(current_event), False)  # clear interruptible flag

                    # Mark that the current handler was interrupted so API methods can react accordingly
                    self.base_bot_internal_data.was_current_event_interrupted = True

                    # We are already in an event handler, took action, and a new event was generated.
                    # So we want to break out of the old handler to process the new event here.
                    raise ThreadInterruptedException()
                # Same event but not interruptible: we've finished handling the previous event, clear its flag
                if self.current_top_event is not None:
                    EventInterruption.set_interruptible(type(self.current_top_event), False)
                break

            # Different event than the one we just handled: clear any pending interruptible flag
            if self.current_top_event is not None:
                EventInterruption.set_interruptible(type(self.current_top_event), False)

            old_top_event_priority = self.current_top_event_priority

            self.current_top_event_priority = self.get_priority(current_event)
            self.current_top_event = current_event

            try:
                await self.dispatch(current_event, turn_number)
            except ThreadInterruptedException:
                # Expected when event handler is interrupted on purpose
                pass
            finally:
                self.current_top_event_priority = old_top_event_priority

    def remove_old_events(self, turn_number:int) -> None:
        with self.events_lock:
            self.events = deque(
                filter(lambda event: not self.is_old_and_non_critical_event(event, turn_number), self.events))

    def sort_events(self) -> None:
        with self.events_lock:
            self.events = deque(sorted(self.events, key=lambda bot_event: (
                -1 if bot_event.is_critical() else 0,
                bot_event.turn_number,
                -self.get_priority(bot_event)
            )))

    def is_bot_running(self):
        return self.base_bot_internal_data.is_running

    def get_next_event(self):
        with self.events_lock:
            return self.events.popleft() if self.events else None

    def is_same_event(self, bot_event: BotEvent) -> bool:
        return self.get_priority(bot_event) == self.current_top_event_priority

    @staticmethod
    def get_priority(bot_event: BotEvent) -> int:
        return EventPriorities.get_priority(type(bot_event))

    async def dispatch(self, bot_event: BotEvent, turn_number: int):
        try:
            if self.is_not_old_or_is_critical_event(bot_event, turn_number):
                await self.bot_event_handlers.fire_event(bot_event)
        finally:
            # Align with Java semantics: always clear interruptible flag for the event type after dispatch
            EventInterruption.set_interruptible(type(bot_event), False)

    @staticmethod
    def is_not_old_or_is_critical_event(bot_event: BotEvent, turn_number: int) -> bool:
        is_not_old = bot_event.turn_number >= turn_number - EventQueue.MAX_EVENT_AGE
        return is_not_old or bot_event.is_critical()

    @staticmethod
    def is_old_and_non_critical_event(bot_event: BotEvent, turn_number: int) -> bool:
        is_old = bot_event.turn_number < turn_number - EventQueue.MAX_EVENT_AGE
        return is_old and not bot_event.is_critical()

    def add_event(self, bot_event: BotEvent):
        with self.events_lock:
            if len(self.events) <= EventQueue.MAX_QUEUE_SIZE:
                self.events.append(bot_event)
            else:
                print(f"Maximum event queue size has been reached: {EventQueue.MAX_QUEUE_SIZE}")

    def add_custom_events(self):
        for condition in self.base_bot_internal_data.conditions:
            if condition.test():
                self.add_event(
                    CustomEvent(self.base_bot_internal_data.current_tick_or_throw.turn_number, condition))

    def dump_events(self, turn_number: int):
        string_joiner = ", ".join(
            f"{type(event).__name__}({event.turn_number})" for event in self.events
        )
        print(f"{turn_number} events: {string_joiner}")

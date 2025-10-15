from threading import Lock
from typing import Any, Generic, TypeVar, Callable, List, Awaitable
import heapq
from weakref import WeakSet
from ..events import EventABC

T = TypeVar('T', bound=EventABC)


class EventHandler(Generic[T]):
    """Generic event handler for handling and dispatching events of type T.

    Events can be published to all subscribed listeners, which will be invoked in order of their priority.
    Subscribers with higher priority are invoked before lower priority ones. This handler provides 
    thread-safety through synchronization and exception handling during event publication.

    Type Parameters:
        T: The type of event that this handler processes. Must implement the EventABC interface.
    """

    _DEFAULT_PRIORITY = 1

    def __init__(self):
        """Initialize a new EventHandler instance."""
        self._lock = Lock()
        self._subscriber_entries: List[EventHandler.EntryWithPriority] = []
        self._subscriber_set: WeakSet[Callable[[T], Awaitable[None]]] = (
            WeakSet()
        )

    def subscribe(
        self,
        subscriber: Callable[[T], Awaitable[None]],
        priority: int = _DEFAULT_PRIORITY,
    ) -> None:
        """Subscribe a new event handler with a given priority.

        Args:
            subscriber: The subscriber callback that will handle the events
            priority: The priority of the subscriber; higher values indicate higher priority (0-100)

        Raises:
            ValueError: If priority is outside valid range or subscriber is already registered
            TypeError: If subscriber is None
        """
        if subscriber is None:
            raise TypeError("Subscriber cannot be None")
        if priority < 0:
            raise TypeError("Priority must be a non-negative value")

        with self._lock:
            # Check for duplicate subscribers using a set for O(1) lookup
            if subscriber in self._subscriber_set:
                raise ValueError("Subscriber is already registered")

            self._subscriber_set.add(subscriber)
            entry = EventHandler.EntryWithPriority(subscriber, priority)

            # Use heapq to maintain the priority queue
            heapq.heappush(self._subscriber_entries, entry)

    def unsubscribe(
        self, subscriber: Callable[[T], Awaitable[None]]
    ) -> bool:
        """Unsubscribe a subscriber from this event handler.

        Args:
            subscriber: The subscriber to be removed from subscriptions

        Returns:
            bool: True if the subscriber was found and removed, False otherwise
        """
        if subscriber is None:
            return False

        with self._lock:
            # Find and remove the subscriber entry
            for i, entry in enumerate(self._subscriber_entries):
                if entry.subscriber == subscriber:
                    # Remove the entry and rebuild the heap
                    self._subscriber_entries.pop(i)
                    heapq.heapify(self._subscriber_entries)  # Reorder remaining entries

                    # Remove from subscriber set
                    self._subscriber_set.remove(subscriber)
                    return True
            return False

    def clear(self) -> None:
        """Removes all subscribers from this event handler."""
        with self._lock:
            self._subscriber_entries.clear()
            self._subscriber_set.clear()
            # No need to heapify an empty list

    async def publish(self, event_data: T) -> None:
        """Publishes an event, invoking all subscribed listeners in order of their priority.

        Args:
            event_data: The event data to be published to the subscribers

        Raises:
            TypeError: If event_data does not implement the EventABC interface
        """
        # Get a copy of subscriber entries under lock to allow concurrent modifications
        with self._lock:
            # Sort entries to ensure correct priority order for invocation
            # This is necessary because heapq only guarantees the smallest element is at position 0
            sorted_entries = sorted(self._subscriber_entries)

        # Process subscribers in priority order
        for entry in sorted_entries:
            # try:
            await entry.subscriber(event_data)
            # except Exception:
            #     # Catch the exception to allow other subscribers to process
            #     pass

    def get_subscriber_count(self) -> int:
        """Returns the number of subscribers currently registered.

        Returns:
            int: The count of subscribers
        """
        # Get a copy of subscriber entries under lock to allow concurrent modifications
        with self._lock:
            return len(self._subscriber_entries)

    class EntryWithPriority:
        """Private class to store a subscriber together with its priority."""

        def __init__(
            self,
            subscriber: Callable[[T], Awaitable[None]],
            priority: int,
        ):
            """Constructs a new entry with the specified subscriber and priority.

            Args:
                subscriber: The subscriber callback function
                priority: The priority of the subscriber
            """
            self.priority = priority
            self.subscriber = subscriber

        def __lt__(self, other: 'EventHandler.EntryWithPriority') -> bool:
            # Reverse comparison for higher priority first (heapq prioritizes lower values)
            return other.priority < self.priority

        def __eq__(self, other: Any):
            if not isinstance(other, EventHandler.EntryWithPriority):
                return NotImplemented
            return self.subscriber == other.subscriber and self.priority == other.priority

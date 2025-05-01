package dev.robocode.tankroyale.botapi.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Generic event handler for handling and dispatching events.
 * Events can be published to all subscribed listeners, which will be invoked in order of their priority.
 * Subscribers can be added with priorities, where higher priority subscribers are invoked
 * before lower priority ones. This handler provides thread-safety through synchronization
 * and exception handling during event publication.
 *
 * @param <T> The type of event that this handler processes.
 */
final class EventHandler<T> {
    /**
     * Default priority value for subscribers.
     */
    private static final int DEFAULT_PRIORITY = 1;

    /**
     * The lock object for thread synchronization.
     */
    private final Object lock = new Object();

    /**
     * A thread-safe list that holds subscriber entries with their respective priorities.
     * This list is used to manage subscribers for an event handler, allowing subscribers
     * to be added, sorted by priority, and invoked in the correct order based on priority.
     */
    private final List<EntryWithPriority<T>> subscriberEntries = new ArrayList<>();

    /**
     * A set to quickly check for duplicate subscribers with O(1) lookup.
     */
    private final HashSet<Consumer<T>> subscriberSet = new HashSet<>();

    /**
     * Subscribes a consumer to this handler with a given priority.
     * The subscribers are executed when an event is published.
     * Higher priority values are executed before lower priority values.
     *
     * @param subscriber the consumer to be notified of published events, must not be null.
     * @param priority the priority for the subscriber, where a higher value indicates a higher priority.
     *                 Must be a non-negative value.
     * @throws NullPointerException if the subscriber is null.
     * @throws IllegalArgumentException if the priority is negative or the subscriber is already registered.
     */
    void subscribe(Consumer<T> subscriber, int priority) {
        Objects.requireNonNull(subscriber, "Subscriber cannot be null");
        if (priority < 0) {
            throw new IllegalArgumentException("Priority must be a non-negative value");
        }

        synchronized (lock) {
            // Check for duplicate subscribers using a set for O(1) lookup
            if (!subscriberSet.add(subscriber)) {
                throw new IllegalArgumentException("Subscriber is already registered");
            }

            subscriberEntries.add(new EntryWithPriority<>(subscriber, priority));
            // Use Comparator.comparingInt for more efficient sorting
            subscriberEntries.sort(Comparator.comparingInt((EntryWithPriority<T> e) -> e.priority).reversed());
        }
    }

    /**
     * Subscribes a consumer to the event handler with the default priority.
     *
     * @param subscriber The consumer function to be invoked when an event is published must not be null.
     * @throws NullPointerException if the subscriber is null.
     * @throws IllegalArgumentException if the subscriber is already registered.
     */
    void subscribe(Consumer<T> subscriber) {
        subscribe(subscriber, DEFAULT_PRIORITY);
    }

    /**
     * Unsubscribes a consumer from this event handler.
     * If the consumer is not subscribed, this method has no effect.
     *
     * @param subscriber The consumer to be removed from subscriptions.
     * @return true if the subscriber was found and removed, false otherwise.
     */
    @SuppressWarnings("unused") // keep for future use
    boolean unsubscribe(Consumer<T> subscriber) {
        if (subscriber == null) {
            return false;
        }

        synchronized (lock) {
            boolean removed = subscriberEntries.removeIf(entry -> entry.subscriber.equals(subscriber));
            if (removed) {
                subscriberSet.remove(subscriber);
            }
            return removed;
        }
    }

    /**
     * Removes all subscribers from this event handler.
     */
    @SuppressWarnings("unused") // keep for future use
    void clear() {
        synchronized (lock) {
            subscriberEntries.clear();
            subscriberSet.clear();
        }
    }

    /**
     * Publishes an event, invoking all subscribed listeners in order of their priority.
     * Subscribers with higher priority are invoked before those with lower priority.
     * If a subscriber throws an exception, it is caught, allowing other
     * subscribers to continue processing the event.
     * <p>
     * Note: Changes to the subscriber list during event publication will not affect
     * the current event being processed, but only future events.
     *
     * @param event The event object to be passed to all subscribers. Can be null, in which case subscribers must handle null values.
     */
    void publish(T event) {
        List<EntryWithPriority<T>> entriesCopy;
        synchronized (lock) {
            entriesCopy = new ArrayList<>(subscriberEntries);
        }

        for (EntryWithPriority<T> entry : entriesCopy) {
            try {
                entry.subscriber.accept(event);
            } catch (Exception e) {
                // Catch the exception to allow other subscribers to process
            }
        }
    }

    /**
     * Returns the number of subscribers currently registered with this event handler.
     *
     * @return The count of subscribers.
     */
    @SuppressWarnings("unused") // keep for future use
    int getSubscriberCount() {
        synchronized (lock) {
            return subscriberEntries.size();
        }
    }

    /**
     * Private class to store a subscriber together with its priority.
     */
    private static class EntryWithPriority<T> {
        /**
         * Gets the priority of the subscriber. Higher values indicate higher priority.
         */
        private final int priority;

        /**
         * Gets the subscriber delegate.
         */
        private final Consumer<T> subscriber;

        /**
         * Constructs a new entry with the specified subscriber and priority.
         *
         * @param subscriber The subscriber consumer.
         * @param priority The priority of the subscriber.
         */
        EntryWithPriority(Consumer<T> subscriber, int priority) {
            this.subscriber = subscriber;
            this.priority = priority;
        }

        /**
         * Determines whether the specified object is equal to the current object.
         *
         * @param o The object to compare with the current object.
         * @return true if the specified object is equal to the current object; otherwise, false.
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryWithPriority<?> that = (EntryWithPriority<?>) o;
            return priority == that.priority && Objects.equals(subscriber, that.subscriber);
        }

        /**
         * Returns the hash code for this instance.
         *
         * @return A hash code for the current object.
         */
        @Override
        public int hashCode() {
            return Objects.hash(priority, subscriber);
        }
    }
}

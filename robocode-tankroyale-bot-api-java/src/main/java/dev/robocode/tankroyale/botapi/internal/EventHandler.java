package dev.robocode.tankroyale.botapi.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/** Event handler which events in the order they have been added to the handler. */
final class EventHandler<T> {
    private final List<EntryWithPriority> subscriberEntries = Collections.synchronizedList(new ArrayList<>());

    final void subscribe(Consumer<T> subscriber, int priority) {
        subscriberEntries.add(new EntryWithPriority(subscriber, priority));
    }

    final void subscribe(Consumer<T> subscriber) {
        subscribe(subscriber, 1);
    }

    final void publish(T event) {
        subscriberEntries.sort(new EntryWithPriorityComparator());
        for (EntryWithPriority entry : new ArrayList<>(subscriberEntries)) {
            entry.subscriber.accept(event);
        }
    }

    class EntryWithPriority {
        private final int priority; // Lower values means lower priority
        private final Consumer<T> subscriber;

        EntryWithPriority(Consumer<T> subscriber, int priority) {
            this.subscriber = subscriber;
            this.priority = priority;
        }
    }

    class EntryWithPriorityComparator implements Comparator<EntryWithPriority> {
        @Override
        public int compare(EntryWithPriority e1, EntryWithPriority e2) {
            return e2.priority - e1.priority;
        }
    }
}

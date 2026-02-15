package dev.robocode.tankroyale.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicInteger

class EventTest : FunSpec({

    context("Event class") {

        test("subscribe and fire") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event.subscribe(this) { result.add(it) }
            event.fire("test")

            result shouldBe listOf("test")
        }

        test("invoke operator fires event") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event.subscribe(this) { result.add(it) }
            event("test")

            result shouldBe listOf("test")
        }

        test("unsubscribe") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event.subscribe(this) { result.add(it) }
            event.fire("test1")
            event.unsubscribe(this)
            event.fire("test2")

            result shouldBe listOf("test1")
        }

        test("operator subscribe and unsubscribe") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event += this to { result.add(it) }
            event.fire("test1")
            event -= this
            event.fire("test2")

            result shouldBe listOf("test1")
        }

        test("operator subscribe once") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event += Once(this) { result.add(it) }
            event.fire("test1")
            event.fire("test2")

            result shouldBe listOf("test1")
        }

        test("property delegation") {
            val events = object {
                val onMessage by event<String>()
            }

            val result = mutableListOf<String>()

            events.onMessage.subscribe(this) { result.add(it) }
            events.onMessage.fire("test")

            (events.onMessage === events.onMessage) shouldBe true
            result shouldBe listOf("test")
        }

        test("subscribe once") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event.subscribe(this, once = true) { result.add(it) }
            event.fire("test1")
            event.fire("test2")

            result shouldBe listOf("test1")
        }

        test("multiple subscribers") {
            val event = Event<String>()
            val result1 = mutableListOf<String>()
            val result2 = mutableListOf<String>()

            event.subscribe("subscriber1") { result1.add(it) }
            event.subscribe("subscriber2") { result2.add(it) }
            event.fire("test")

            result1 shouldBe listOf("test")
            result2 shouldBe listOf("test")
        }

        test("Handler class") {
            val handler = Event.Handler<String>({ }, true)

            handler.shouldBeInstanceOf<Event.Handler<String>>()
            handler.once shouldBe true
        }

        test("subscriber is removed when owner is garbage collected") {
            val event = Event<String>()
            val result = AtomicInteger(0)

            class Subscriber {
                fun handleEvent(msg: String) {
                    result.incrementAndGet()
                }
            }

            var subscriber: Subscriber? = Subscriber()
            val weakRef = WeakReference(subscriber)

            event.subscribe(subscriber!!) { msg ->
                weakRef.get()?.handleEvent(msg)
            }

            // Verify that the subscriber is initially present
            event.fire("test")
            result.get() shouldBe 1

            // Remove strong reference to subscriber and force garbage collection
            subscriber = null
            System.gc()
            Thread.sleep(100)

            // Fire the event again
            event.fire("test")

            // The result should still be 1 because the subscriber should have been removed
            result.get() shouldBe 1

            // Verify that the eventHandlers map is empty
            val eventHandlersField = Event::class.java.getDeclaredField("eventHandlers")
            eventHandlersField.isAccessible = true
            val eventHandlersRef = eventHandlersField.get(event) as AtomicReference<*>
            val eventHandlers = eventHandlersRef.get() as Map<*, *>
            eventHandlers.shouldBeEmpty()
        }

        test("subscriber reference type") {
            val event = Event<String>()
            val subscriber = object : Any() {}

            event.subscribe(subscriber) { }

            // Use reflection to access the private eventHandlers field
            val eventHandlersField = Event::class.java.getDeclaredField("eventHandlers")
            eventHandlersField.isAccessible = true
            val eventHandlersRef = eventHandlersField.get(event) as AtomicReference<*>
            val eventHandlers = eventHandlersRef.get() as Map<*, *>

            // Check that the value in the eventHandlers map is our Handler class
            eventHandlers.values.first().shouldBeInstanceOf<Event.Handler<String>>()

            // Check that the key in the eventHandlers map is our subscriber object
            eventHandlers.keys.first() shouldBe subscriber
        }

        test("re-subscription during fire") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            val subscriber = object {
                fun handle(msg: String) {
                    result.add(msg)
                    if (msg == "first") {
                        // Re-subscribe or subscribe another handler during fire
                        event.subscribe(this) { result.add("second-$it") }
                    }
                }
            }

            event.subscribe(subscriber) { subscriber.handle(it) }
            event.fire("first")

            // Should have "first" from original handler.
            // The new handler should NOT be called during the same fire.
            result shouldBe listOf("first")

            event.fire("third")
            // Now both handlers should be called (or rather, the last one subscribed for this owner)
            // Wait, Event.subscribe with same owner REPLACES the handler.
            result shouldBe listOf("first", "second-third")
        }

        test("un-subscription during fire") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            val subscriber = object {
                fun handle(msg: String) {
                    result.add(msg)
                    event.unsubscribe(this)
                }
            }

            event.subscribe(subscriber) { subscriber.handle(it) }
            event.fire("first")
            event.fire("second")

            result shouldBe listOf("first")
        }
    }
})

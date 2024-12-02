package dev.robocode.tankroyale.gui.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
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

        test("unsubscribe") {
            val event = Event<String>()
            val result = mutableListOf<String>()

            event.subscribe(this) { result.add(it) }
            event.fire("test1")
            event.unsubscribe(this)
            event.fire("test2")

            result shouldBe listOf("test1")
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

        test("enqueue") {
            val event = Event<Unit>()
            val counter = AtomicInteger(0)
            val latch = CountDownLatch(1)

            event.enqueue(this) {
                counter.incrementAndGet()
                latch.countDown()
            }
            event.fire(Unit)

            // Wait for the EDT to process the event
            withContext(Dispatchers.Default) {
                latch.await(5, TimeUnit.SECONDS) shouldBe true
            }

            counter.get() shouldBe 1
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
            val eventHandlers = eventHandlersField.get(event) as Map<*, *>
            eventHandlers.shouldBeEmpty()
        }

        test("subscriber reference type") {
            val event = Event<String>()
            val subscriber = object : Any() {}

            event.subscribe(subscriber) { }

            // Use reflection to access the private eventHandlers field
            val eventHandlersField = Event::class.java.getDeclaredField("eventHandlers")
            eventHandlersField.isAccessible = true
            val eventHandlers = eventHandlersField.get(event) as Map<*, *>

            // Check that the value in the eventHandlers map is our Handler class
            eventHandlers.values.first().shouldBeInstanceOf<Event.Handler<String>>()

            // Check that the key in the eventHandlers map is our subscriber object
            eventHandlers.keys.first() shouldBe subscriber
        }
    }
})
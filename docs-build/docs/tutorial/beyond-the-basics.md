# Beyond the Basics

At this point, you should already have read the [Introduction], [Getting Started], and the [My First Bot] tutorial, and
now you want to progress and learn more about Robocode beyond the basics.

In the following, it is assumed that you are already familiar with and use the official [Bot APIs]. This tutorial is
taking offset in the official Bot APIs. Also notice, that the class and method names take offset in the Java API, but
should be similar in APIs for other platforms.

## The RoboWiki

The [RoboWiki] is the best place to gain the most knowledge about Robocode. This site has been collecting lots of great
expertise from Robocoders since 2003. On the RoboWiki you can benefit from learning a lot of different strategies for
movement, targeting, efficient radar sweeps, etc. You can also get some pretty good hints with for example using various
kinds of AIs.

Note that the [RoboWiki] is primarily targeted at the [original version] of Robocode, so you should expect differences
between the original version and the new version (Robocode Tank Royale). For example, the API is a bit different, and
the original version of Robocode did not use a booter or a server and was mostly based on the Java language only.
You can read about some important differences [here](../articles/tank-royale). Nevertheless, the various strategies you
would apply for Robocoding with the original version of Robocode should be easy to apply for the new version as well.
And even when most code snippets are written for Java, it should be straightforward to port to a similar programming
language e.g. C# or Typescript.

Also note that the RoboWiki is driven by Robocoders, not by the author(s) of Robocode. So if the site is down, which
happens sometimes, you can (and should) report this on e.g. the [Google Group] for Robocode. Then action will be taken
to get it up and running as soon as possible.

If the [RoboWiki] is down, you can use the [Web Archive] temporarily until the RoboWiki site is up and running again.
But note that the link points to a snapshot of the site, and might be a bit outdated.

## Event Queue

An essential part of Robocoding is event handling. Hence, this is explained in more detail in the following along with some advice.

All events received from the game server are immediately stored in an event queue. The events in the event queue will
first and foremost be ordered by the _turn number_, and secondly events that belong to the same turn are ordered by
their _event priority_. Events with higher priorities are handled before events with lower priorities.

> Note that all events have a predefined event priority, but you can change this with the API if you want to change the
> ordering of the events.

Note that you should expect multiple events to occur within the same turn and more events of the same type,
e.g. `ScannedRobotEvent` occurring in the same turn when more bots have been scanned.

Events are dispatched from the event queue one event at a time. Each calls a synchronous blocking method call via a
single dedicated event thread. When an event is dispatched its corresponding event handler method is being called.
When e.g. a `ScannedBotEvent` is triggered, the corresponding `onScannedBotEvent(ScannedBotEvent)` method will be
called, blocking the event thread, until it has finished executing.

Notice that the event queue will stack up events until they have been handled by calling event handlers or till the
events are getting "too old". When an event has been queued up for more than 2 turns, it is considered outdated and will
be removed from the event queue automatically. The only exception is events that have been defined as _critical_. Those
events will only be removed when these have been handled regardless of how old they are.

Examples of critical events are the `DeathEvent`, `SkippedTurnEvent`, and `WonRoundEvent`.

## Event handlers

Event handlers are predefined methods prefixed with `on`, e.g. the `onScannedBot`. To take action when an event happens,
you need to implement/override the predefined method for the event handler, i.e. override the `onScannedBot` method, so
you can take action when an enemy bot has been scanned to target the bot.

When handling an event, e.g. when a bot has been scanned with the `ScannedRobotEvent` in the `onScannedRobot()` event
handler, make sure to:

1) Keep the code at a minimum
2) Avoid calling API methods!
3) Avoid calling I/O methods!
4) Gather intelligence information only

### Keep the code at a minimum

Avoid CPU-consuming logic in your event handlers. The reason is that the event handler is a `blocking call`. Hence, no
other event handlers can execute before the current event handler has been executed.

And if a method takes too long to process, the current turn, and perhaps several turns will have passed since it was
invoked, meaning it will not be possible to react fast enough on an event for the current turn.

### Avoid calling API methods!

It is considered bad practice to call Bot API methods from the event handlers. If you call e.g. the `fire()` method, the
event handler will be blocked until the gun has fired, meaning that other event handlers will be triggered at a later
turn, and might get too old on the event queue, and hence removed. This way the bot will not be able to cope with other
events and might lack crucial information for the logic to work properly and efficiently.

### Avoid calling I/O methods!

For the same reasons as with calling Bot API methods, it is considered bad practice to call I/O intensive methods as the
event handler might block for one or several turns.

A better alternative may be to use a thread that runs beside the event handlers or use the logic in the `run()` method
to make the I/O calls if possible.

If for example, you need to log data, you might want to use a synchronous logger or put each log entry into some
logging-specific data structure, and save all log entries at once when the round or battle has ended, which could be
achieved by using the `onRoundEnded()` or `onGameEnded()` methods respectively.

### Gather intelligence information only

The event handlers are only required to gain up-to-date information about your bot's information. Consider your event handlers as "sensors" of your bot. And you should do your best to make the most out of the information you gain from the event handlers.

There is no need to keep track of event data if your bot does not need to take action for a specific type of event (obviously). However, with some events, it is crucial to keep track of the event data.

With a minimal event handler, you only need to store the event data in some data structure, and nothing more. That is, to gain information about the bot's surroundings. The gathered data will then be used later when the bot must decide on proper actions.

> As mentioned earlier, it is a bad idea to take immediate action within an event handler. More on that later!

One example is when an enemy bot has been spotted triggering the `onScannedBot()` method. Here it makes sense to store
e.g. the position and orientation of enemy bots scanned on specific turns. For example, you could create a history of
the recent positions for each robot.
This way, you might be able to predict the position of an enemy but some turns ahead.

The ability to predict the future position of an enemy bot is crucial for aiming the turret in the direction of this
target position to hit the bot.

## Overall Bot Behavior

A lot has been said about event handling in this tutorial. And you have been advised to avoid taking immediate actions with the event handlers. Why so, you might rightfully ask?

With primitive bots, in particular small bots used for demoing e.g. the sample bots, a simple targeting strategy would be to just shoot directly at the point where an enemy is currently located, and also move around more or less randomly. This might seem good for a start, but in the long run, more advanced bots will outperform simple bots due to better intelligence.

Here are a few examples of what advanced robots can do:

- Make efficient use of the scanner.
- Pick out the best target enemy among the enemy bots left on the battlefield to fire at.
- Predict the future position of a target bot taking the bullet's travel time into account to hit the target.
- Balance how much energy to use when firing a bullet depending on various situations.
- Decide on the best movement for moving the robot around depending on the situation.

All of the above requires a structured approach and "centralizing" the code into one place that is guaranteed to always run in each turn, and not rely on bits and pieces of code being run one or multiple times in a turn within event handlers that might or might not be triggered in a turn.

So you should set up a central place in your bot that takes action based on the (new) data it gets from its "sensors", i.e. the events. The `run()` method of the bot is an excellent place to put this logic. Alternatively, you could start one to multiple threads for handling e.g. movement, scanning, targeting, etc.

## Strategies

With Robocode, successful bots use multiple strategies. Some bots are specialized for 1-versus-1 battle, where only 2 bots participate, and hence only have a single enemy to worry about. This makes things less complicated compared to e.g. a bot participating in a melee battle against 10 different enemy bots. And if the bot is a bot in a team, a team strategy should be applied as well. So you need to consider, which types of battles your bot is developed for.

> Note that you can specify the game types your bot can handle, e.g. classic, 1v1, and melee.

### Strategy types

A very efficient mindset to have when Robocoding is to divide the strategies into categories fitting the various parts of the tank:

1) **Movement strategy**: The _body_ is responsible for movement and getting the bot to a good position on the battlefield
   and avoid being hit by bullets.
2) **Gun strategy**:
   1) The _turret_ is responsible for turning the cannon in the right direction of the target.
   2) The _cannon_ is responsible for firing the bullets with the right amount of energy and speed to hit the target,
      and also times when to fire, and when the cannon is not heated.
3) **Scanning strategy**: The _radar_ is responsible for scanning all enemies as efficiently as possible, and getting as up-to-date information about the enemies as possible.

So you should have at least 3 strategies to cover these categories. And notice, that you may have multiple strategies
to use for each category.

You might also have strategies, that are independent of the 3 strategies above, e.g. a strategy for picking out the next enemy target. For example, it is far easier to hit bots that move very slowly, and perhaps are not moving at all, if they have been disabled, making them an easy target. Such a strategy could work independently of the **Gun strategy**.

### Divide and Conquer

One important note is that executing code for different strategies should not depend on the code execution used in other ones. For example, a movement strategy should be able to run independently of a scanning, gun strategy.
Hence, using strategies is a good way to _divide and conquer_ different aspects of the bot's behavior instead of mixing it all, making it easier to concentrate on one aspect at a time.

### Multiple strategies

#### Scanning strategies

If there is only one enemy left on the battlefield, a simple radar/scanning strategy would be to point the radar in the direct direction of the enemy bot. But if there are a few enemies, you might choose to sweep the radar continuously left and right in a scanning arc that is as small as possible to receive as accurate information about the enemy positions as possible. If there are 10 or more bots on the battlefield, turning the radar left infinitely to scan all enemies in 360 degrees at all times might turn out to be an efficient strategy in that situation.

### Gun strategies

It might also make sense to have different Gun strategies that depend on how an enemy moves. This could for example be a gun that can do simple linear targeting, circle targeting, but also more targeting for more advanced movement. So the important part is to figure out which movement pattern an enemy is using to figure out how to hit the target.

Note that you need to target "ahead" of its current position to a future position where you think the bot will be when the bullet should hit the target. This is because it takes time for the bullet to travel towards the target. And the heavier the bullet, the slower the bullet will move.

#### Virtual bullets

A common strategy used for picking the best gun strategy for a given bot is to make use of _virtual bullets_. The idea is to simulate if a bullet would have hit its target if you used one or the other gun strategy. That is without firing the gun, but just simulate that the bullet was actually fired from the gun, and then keep track of the bullet and check if it would have hit its target. The more hits you have with the "virtual gun", the better that strategy is for the particular target. You might also benefit from using different strategies between various bots, e.g. to hit bots that each move differently.

### Movement strategies

Movement can be difficult to master in Robocode. But first and foremost you should make sure your bot is not standing still as it will be an easy target. The same is the case if it moves slowly or in a very predictable pattern. If there are many bots on the battlefield, it might be wise to move into an area on the battlefield that or not too crowded.

If there are a few enemies on the battlefield, a movement strategy could be to keep a certain distance to the target enemy, and e.g. move perpendicular to the target enemy by continuously moving forward and back some distance and due small turn adjustments to keep the pointing bot in a direction 90 degrees towards the target.

Another topic is the distance to the current target. If the distance is too big, it will be more difficult to hit the target due to the bullet travel time, where the target has more time to move away. And if your bot is too close to the target, it might be better at hitting your bot taking more damage. So one challenge is to find the sweet spot.

#### Bullet dodging

In Robocode, your bot receives no event about enemies firing bullets. It sure would be nice to receive information about which bullets enemies have fired, e.g. the position, direction, and speed. This information would
be useful to avoid getting hit by enemy bullets, i.e. _bullet dodging_.

However, you receive some event data from your enemies that can give you an idea if an enemy bot might be firing a bullet. Whenever an enemy bot is scanned, you receive information about how much energy it has. If you keep the radar laser-focused on the enemy, you should be able to detect it as soon as there is an energy drop. This could mean that the
enemy took damage from a wall, a bot collision, or a bullet hit. But it could also be due to spending energy on firing a bullet.

So the first challenge is to figure out if the enemy seems to fire a bullet or not. And if so, if the bullet is fired in your direction. If there is only one bot left on the battlefield, the enemy will most likely be firing in your bot's direction. When multiple enemies are left, it is more difficult to predict, but it would be safe to assume that it fired against your bot, i.e. take no chances.

One tactic would be to keep track of potential bullets hitting you similar to [virtual bullets](#virtual-bullets) fired from your bot. For example, you could figure out the potential target directions of the bullet, and calculate the speed of the bullet based on the energy spent on firing it. Then calculate the new positions of the virtual bullet for each new turn until the bullet is leaving the battlefield. This way, you should of course move your bots away from where the tracked virtual bullets might be to avoid getting hit.

## Centralized logic

### The run method

As mentioned earlier, it is wise to centralize the logic that performs various calculations and makes decisions but also takes action at the end of the current turn. An excellent place to do this is with the `run()` method, which is invoked for each new round.

### Main Loop

As the `run()` method is executed once, you need to keep the method running, i.e. _blocking_. Otherwise, the method will exit as any other method, and you will not be able to use the `run()` method for any good afterward.

The best way to keep the `run()` method _running_ is to make sure of this loop:

```java
public void run() {
    // Initial code can be run here running each round

    while (isRunning()) {
        // Code running throughout the entire round
    }

    // Final code can be run here when the bot is not running anymore
    // when the round has ended.
}
```

> ⚠ Do _not_ use an infinite loop like it was the case with many bots with the original Robocode. You risk that your `run()` method will keep running in the background, while a new round is started, and the `run()` method is called yet another time. This way, you risk that multiple `run()` methods run in parallel causing a [race condition] and will ruin your control over what happens in the bot.

The while-loop is called the _main loop_ of your bot, and here you should put the logic that needs to be run each turn.

Here is some pseudo code of how the main loop could look like:

```java

public void run() {
    initializeRound(); // Initial method for initializing data structures etc.

    // Loop for each round
    while (isRunning()) {
        pickTargetEnemy(); // Picks a (new) target enemy

        handleMovement(); // Moves the tank to a new position
        handleGun();      // Aims and fires against the current target
        handleRadar();    // Turns the radar to scan enemies

        go(); // Send actions to the server and hereby end the current turn
    }
    finalizeRound(); // Method called when the round is over, e.g. saving data
}
```

Here it will be up to you to implement the various methods. Each method should call commands in the API for moving the bot by calling e.g. `setForward()`, `setTurnLeft()`, etc.

### Use setter methods!

Note that you should use _setter_ methods like `setForward()` and `setFire()`, and not the corresponding `forward()` and `fire()` methods. The reason is that the `forward()` and `fire()` methods are blocking methods and automatically invoke the `go()` method, which will send the actions to the server and wait for the next turn. And you need to be in
control and only call the `go()` as the last thing in your main loop to conclude your actions.

Another benefit of using the setter methods is that you can call the same setter method multiple times during the current turn to override the recent setter. For example, you might have called `setForward(50)` in one place, but choose to override this by calling `setBack(20)` or `setForward(-20)` (these calls are equal) with some code having higher precedence (e.g. for bullet dodging).

## Final words

This concludes this tutorial. There is much more to learn about Robocoding, and we have just been scratching the surface.

### Study the RoboWiki!

Your first challenge should be to beat the sample bots that come with Robocode. And I highly recommend you to continue
to study the [RoboWiki] to get tips and tricks for building a good bot.

### Use version control

I highly recommend that you make use of version control like e.g. GitHub, BitBucket, GitLab, or similar so you don't lose your code, but also can do experiments with your bot(s) using various code branches. If you make your bot "open source", other people will benefit from learning techniques from your bot, and you might also get the credit if other people decide to use parts of your code. Moreover, it will be easier to help you out, if you have an issue with your bot and need help with solving this.

### Challenge your bot

I also recommend that you find some challenging bots to battle against, which are more advanced than the sample bots
coming with Robocode, so you can adjust and improve your bot(s) even more.

Happy Robocoding! ❤️


[RoboWiki]: https://robowiki.net/wiki/Main_Page "RoboWiki. Collecting Robocode knowledge since 2003"

[original version]: https://robocode.sourceforge.io/ "Home of the original version of Robocode"

[Introduction]: ../articles/intro "Introduction"

[Getting Started]: ../tutorial/getting-started "Getting Started tutorial"

[My First Bot]: ../tutorial/my-first-bot "My First Bot tutorial"

[Bot APIs]: ../api/apis "Bot APIs"

[Google Group]: https://groups.google.com/g/robocode "Google Group for Robocode"

[Web Archive]: https://web.archive.org/web/20240514014223/https://robowiki.net/wiki/Main_Page "Web Archive for the RoboWiki"

[blocking]: https://en.wikipedia.org/wiki/Blocking_(computing) "Wikipedia on Blocking (computing)"

[race condition]: https://en.wikipedia.org/wiki/Race_condition#In_software "Race condition in software"
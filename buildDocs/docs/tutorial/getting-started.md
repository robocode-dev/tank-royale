# Getting Started

## The Basics

This tutorial introduces you to the basics of Robocoding. That is, getting a basic understanding of what Robocode is all
about before getting started coding your first bot.

## Run some battles first

The best introduction to Robocode is to see some battles between existing bots to get an idea of what Robocode is about.

This GIF animation can give you a glimpse, but not the full picture:

![Robocode battle](../images/robocode-battle-anim.gif)

So if you have not seen the battles already, you should:

1) [install and run](../articles/installation) the GUI application,
2) [unzip the sample bots](../articles/installation#sample-bots) to some directory on your system,
3) [set up bot directories](../articles/installation#set-up-bot-directories) for the sample bots, and
4) [start a battle](../articles/installation#sample-bots) with the sample bots.

You can read more about how to use the GUI [here](../articles/gui).

## What is Robocode about?

As written in the [Introduction](../articles/intro), Robocode is about creating a program for a tank. This program is
run when selected and booted from the [GUI](../articles/gui). The goal is to keep the bot (tank) alive as long as
possible and defeat all other enemy bots in a battle. The more damage you deal to enemy bots, and the better the bot is
to survive enemy tanks, the better [score](../articles/scoring) your bot will receive. The higher the score, the better.

## Rounds and Turns

A _battle_ between bots in Robocode can contain multiple _rounds_. A battle could for example contain 10 individual
rounds, where each round will have its winners and losers.

Each round is divided into _turns_ which are the smallest time units. A _turn_ is a clock tick and game loop in
Robocode. How many turns a round takes depends on how long time it takes for the last survivor(s) to last the round.

With each turn, a bot should:

1) Move the bot, scan for enemies, and potentially fire the gun
2) React on events like e.g. when the bot is hit by bullets or collides with a bot or the wall.

Commands for moving, turning, scanning, firing, etc. are sent to the server as an _intent_ for each turn.

> Note that the official bot API sends the _bot intent_ to the server behind the scenes, so you do not need to worry
> about this yourself, unless you are creating your own Bot API.

With each turn, the bot automatically receives updated information about its new and current state, e.g. new position
and orientation on the battlefield. The bot will also get information about enemy bots when they are detected by the
scanner. More information follows about this later.

### Turn timeout

It is important to note there is a time restriction on how much time each bot can spend each turn called _turn timeout_,
which is typically 30-50 ms (can be set up as a battle rule). This means that bots cannot use as long time as it suits
them to make a move and finish the current turn.

### Skipped turn

Whenever a new turn starts, a timeout timer is reset and starts ticking. If the timeout is reached, the bot does not
send its _intent_ for the turn, and hence no commands are provided for the server. Hence, the bot will be _skipping_ the
turn. With a _skipped turn_, a bot will not be able to make adjustments to its movement, and will not be able to fire
the gun, as the server did not receive the command in time before the next turn started.

## Bots and Teams

Robocode supports team battles, where a team consists of multiple bots (teammates) teaming up against enemy teams or
single bots. Creating a team takes a bit more effort than creating a bot, and is something you can try out later. Just
keep in mind, that you can develop both individual bots, but also teams consisting of multiple different bots.

Also note that it is possible to let some of the teammates be "Droids", which have _no scanner_, but more initial
energy. Droids only make sense to use in teams, when they have no scanner.

## Energy

All bots start with an initial amount of energy of typically 100 energy points. (Note that Droids start with 120 energy
points).

1. A bot _looses_ energy when being _hit_ by enemy bullets or _rammed_ by an enemy bot. <br>(Note that no energy is lots
   if the bot is hit by a teammate, as friendly fire is not supported.)
2. A bot _spends_ energy when firing its cannon. But it can only spend energy if it has enough energy to fire the
   bullet.
3. A bot _gains_ energy when one of its bullets hits an enemy bot.
4. A bot with zero energy left, will be _disabled_ and cannot move this would require energy. This makes the bot an easy
   target and hence should be avoided.

Hence, it is crucial to avoid getting hit by enemies to keep energy and have the energy to fire the cannon against
enemies.
But firing the cannon must be done wisely, as energy is spent when firing the cannot, and energy is only earned when
the bullet hits an enemy. Also notice that a bot gains 3x the firepower energy spent on the bullet, which hits an enemy.

Read more about how much energy is required to fire bullets and how much energy is gained by hitting enemy bots etc. in
the [physics](../articles/physics) for Robocode.

## Bullets

### Firing bullets

The bullets fired in Robocode are virtual energy bullets (not projectiles). The more energy (firepower) spent on
firing the bullet from the cannon, the heavier the bullet makes it slower. But the heavier bullets also make more
[damage](../articles/physics#bullet-damage), and hence let your bot [gain energy](../articles/physics#energy-gain) when
hitting an enemy bot.

As mentioned, the heavier the bullet, the slower it moves. Hence, it will take a longer time to reach its target, and
the risk that it will not hit its target is higher. Lighter bullets move much faster making it easier to hit targets.
But notice that they do not earn the bots as many energy points when they hit enemy bots. Read more about bullet speed
[here](../articles/physics#bullet-speed)

### Gun Heat

When bullets are fired, the cannon gets heated. Heavier bullets generated more heat than lighter bullets. When the gun
is heated, it cannot fire until it has cooled down to a gun heat equal to zero. Also notice, that the gun is hot within
the first rounds and needs to cool down before it can fire the first time. Read more about the gun heat
[here](../articles/physics#gun-heat).

## Collisions

Note that your bot will take damage when it hits the wall (borders), i.e.
taking [wall damage](../articles/physics#wall-damage). The same is the case, if the bot hits another bot. If the bot
hits an enemy bot by moving forward, it will be [ramming](../articles/physics#ramming) the enemy bot, which generates a
small amount of score.

## Tank parts

A bot in Robocode is a tank that consists of 3 parts:

1) Body
2) Turret and gun
3) Radar

![Tank anatomy](../images/anatomy.svg)

The _body_ is the main and the bottom part of the tank is used for moving the tank around on the battlefield.

The _turret_ is mounted on the main body and can turn either _with_ or _independently of_ the body.
The cannon is mounted on the turret and is used for firing bullets (obviously).

The _radar_ is mounted on the top of the turret and can turn either with or independently of both the body and the
turret too.

## Movement

The bot can move forward and backward up to a [maximum speed](../articles/physics#maximum-speed). It takes several
turns to get to reach the maximum speed. The bot can achieve a maximum acceleration of 1 unit per turn, and brake with a
maximum deceleration of 2 units per turn. The maximum acceleration and deceleration do not depend on the current speed
of the bot.

### Turning

As mentioned earlier, it is possible to turn the body, turret, and radar independently of each other. If you don't turn
turret or radar, these will of course point in the same direction as the body.

Each part of the body has different turning speeds. The radar is the fastest part and can turn up to 45 degrees per
turn, meaning it can turn 360 degrees in 8 turns. The turret and gun can turn up to 20 degrees per turn.

The slowest part is the tank body, which in the best case can turn up to 10 degrees per turn. But notice that this
depends on the current speed of the bot. The faster the bot moves, the slower the bot will be able to turn. Read more
about it [here](../articles/physics#bot-base-rotation).

Note that no energy is spent on moving or turning the bot.

### Scanning

A crucial aspect of Robocoding is to scan enemy bots with the radar of the bot. The radar can scan bots within a range
of 1200 pixels. So enemies more than 1200 pixels away from the bot cannot be detected/scanned by the radar.

#### Radar sweep/scan arc

It is important to notice a bot will only be able to scan bots that are within its scan arc. The scan arc is the "radar
sweep" from its previous radar direction to its current direction in the turn.

![Radar sweep](../images/radar.svg)

If the radar is not being turned in a round, meaning it is pointing in the same direction as the previous turn, then the
scan arc will be zero degrees, and the bot will not be able to scan enemies.

![Scan arc](../images/radar-no-sweep.svg)

Hence, it is highly recommended to ensure the radar is always shifting its direction to keep scanning enemies.

#### Scanning event

Whenever and _only_ when a bot scans an enemy, the bot receives a `ScannedBotEvent` with information about the enemy,
e.g. its coordinates and current energy. This means that a bot never receives updated information about enemies'
whereabouts, when they are not being scanned by the bot.

Hence, it is important to keep the direction of radar in the direction of the enemies. This is easy, if there is only one bot on the battlefield, but much harder when there are multiple enemies to keep track of, as the radar is only able to cover a scan arc up to 45 degrees.

It is highly recommended to keep track of the latest scanned data for each enemy to get an idea of where they might be
positioned and heading when the radar is not pointing towards those enemies.

### End of tutorial

This is the end of this tutorial.

You should now have gained knowledge about the basic concepts of Robocode. Now it is time for some Robocoding. So head
over to the [My First Bot] tutorial to get started creating your first bot.


[My First Bot]: ../tutorial/my-first-bot "My First Bot tutorial"
# Getting Started

## Introduction

This tutorial introduces you to Robocoding. That is, how to get started coding bots for Robocode, and get some basic
understanding of the game.

## Run some battles first

The best introduction to robocode is to see some battles between existing bots to get an idea of what Robocode is about.

This GIF animation can give you a glimpse, but not the full picture:

![img.png](img.png)

So if you have not seen the battles already, you should:

1) [install and run](../articles/installation) the GUI application,
2) [unzip the sample bots](../articles/installation#sample-bots) to some directory on your system,
3) [set up bot directories](../articles/installation#set-up-bot-directories) for the sample bots, and
4) [start a battle](../articles/installation#sample-bots) with the sample bots.

You can read more about how to use the GUI [here](../articles/gui).

## What is Robocode about?

As written in the [Introduction](../articles/intro), Robocode is about creating a program for a tank. This program is
run when it is selected and booted from the [GUI](../articles/gui). The goal is to keep the bot (tank) alive as long as
possible, and defeat all other enemy bots. The more damage you deal to enemy bots, and the better the bot is to
survive enemy tanks, the better [score](../articles/scoring) your bot will receive. The higher score, the better.

### Teams

Robocode supports team battles, where a team consists of multiple bots (teammates) teaming up against enemy teams or
single bots.

It is possible to let some of the teammates be "Droids", which have _no scanner_, but more initial energy.

### Energy

All bots start with an initial amount of energy of typical 100 energy points. (Note that Droids start with 120 energy
points).

1. A bot _looses_ energy when being _hit_ by enemy bullets or _rammed_ by an enemy bot. <br>(Note that no energy is lots
   if the bot is hit my a teammate, as friendly fire is not supported.)
2. A bot _spends_ energy when firing its cannon. But it can only spend energy, if it has enough energy left required to
   fire the bullet.
3. A bot _gains_ energy when one of its bullets hits an enemy bot.
4. A bot that have exactly 0 energy points left, will be _disabled_, meaning that it cannot move as it requires energy.
   This makes it an easy target, and hence should be avoided.

Hence, it is crucial to avoid getting hit by enemies to keep energy, and have energy to fire the cannon against enemies.
But firing the cannon must be done wisely, as energy is spent when firing the cannot, and energy is only earned when
the bullet hits an enemy. Also notice that a bot gains 3x the firepower energy spend on the bullet, which hits an enemy.

Read more about how much energy is required to fire bullets and how much energy is gained by hitting enemy bots etc. in
the [physics](../articles/physics) for Robocode.

### Lighter bullets vs heavier bullets

The bullets fired in Robocode is pure energy bullets (not projectiles). The more energy (firepower) that is spent on
firing the bullet from the cannon, the heavier the bullet is making it slower. But the heavier bullets also make more
damage, and hence let your bot gain more energy when hitting an enemy bot.

As mentioned: The heavier a bullet is, the slower it moves. Hence, it will take longer time to reach its target, and the
risk that it will not hit its target is higher.

Lighter bullets moves much faster making it easier to hit targets. But they to not earn the bots as many energy points
when hitting enemy bots.

### Gun Heat
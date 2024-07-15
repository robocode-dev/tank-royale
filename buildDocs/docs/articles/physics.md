# Physics

## Measurements

### Time measurement

Robocode is turn-based, and hence time is measured in turns. With each turn, each bot receives new information and
events about what is going on in the arena. And with each turn, the bot needs to send new commands to the server.

A battle has one or more rounds, for example, 10 rounds. Turns and rounds are measured are provided as a number. Rounds
start at round number 1, and each round starts with turn number 1.

### Distance measurement

Distance in Robocode is measured as _units_ which are floating-point numbers using double precision.

## Movement

### Acceleration (a)

Bots accelerate at the rate of 1 unit per turn but decelerate at the rate of 2 units per turn. Hence, the bot is twice
as fast at braking than gaining speed. Robocode determines acceleration for your bot, based on the speed or distance
that is set as target for the bot.

### Speed / velocity (v)

The speed (velocity) equation is:

$v = a × t$

Hence speed = acceleration × time, or deceleration × time.

#### Maximum speed

The speed can never exceed 8 units per turn. Note that technically, velocity is a vector, but in Robocode we simply
assume the direction of the vector to be the bot´s heading.

### Distance (d)

The distance formula is:

$d = vt$

Hence, distance = speed × time.

## Rotation

Rotation is measured in degrees in Robocode.

### Bot base rotation

If standing still (0 units/turn), the maximum rate is 10° per turn. But the turn rate of a bot is limited by its speed.

The maximum rate of rotation is:

$10 - \frac{3}{4}|v|$

This means that the faster you're moving, the slower you turn.

If moving with a max. speed of 8 units/turn, the maximum rate of rotation is only 4° per turn.

### Gun rotation

The maximum rate of rotation is 20° per turn. This is added to the current rate of rotation of the bot.

### Radar rotation

The maximum rate of rotation is 45° per turn. This is added to the current rate of rotation of the gun.

## Bullets

### Firepower

The maximum firepower is 3 and the minimum firepower is 0.1. The amount of energy used on the firepower is subtracted
from the bot´s energy.

### Bullet damage

Bullet damage depends on firepower. When a bullet hits a bot the damage is:

$4 × firepower$

If firepower > 1, it does additional damage:

$2 × (firepower - 1)$

### Bullet speed

The bullet speed (v) is constant and depends on the firepower used for firing the gun:

$20 - 3 × firepower$

This means that the maximum bullet speed is 19.7 units/turn with the minimum bullet power of 0.1, and the minimum bullet
speed is 11 units/turn with the maximum bullet power of 3.

### Gun heat

The gun gets heated when fired. The amount of gun heat produced is:

$\frac{1 + firepower}{5}$

Bots cannot fire if gun heat > 0. All guns start hot at the start of each round and start at 3.

### Energy gain

Bots get awarded by receiving energy when one of their bullets hits another bot. The amount of energy received is:

$3 × firepower$

## Collisions

When a bot collides with another bot or a wall, it is stopped. The exception is a bot being hit by another bot, which it
is moving away from. In this case, the bot is not stopped.

### Bot collisions

Each bot takes 0.6 damage when colliding with each other.

### Ramming

If a bot is hitting another bot by moving forward, this counts as _ramming_, meaning that the bot is deliberately trying
to hit the other bot. Both bots take damage, but a ramming bot will get a ramming kill bonus. (see more under
[Scoring](scoring.md)).

### Wall damage

When a bot hits a wall it will take damage depending on its speed (v):

$\frac{|v|}{2} - 1$

Hence, the higher speed the more damage.

Note that if the damage is negative, it is reduced to zero.

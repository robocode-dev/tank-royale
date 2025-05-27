"""
Constants for the Tank Royale Bot API.
"""

BOUNDING_CIRCLE_RADIUS = 18
"""
Radius of the bounding circle of the bot.
A bot gets hit by a bullet when the bullet gets inside this bounding circle.
"""

SCAN_RADIUS = 1200
"""
Radius of the radar's scan beam.
Bots within this radius are detectable by the radar.
"""

MAX_TURN_RATE = 10
"""
Maximum possible driving turn rate in degrees per turn.
The speed of the bot affects the turn rate.
Formula: MaxTurnRate - 0.75 x abs(speed)
"""

MAX_GUN_TURN_RATE = 20
"""
Maximum gun turn rate in degrees per turn.
"""

MAX_RADAR_TURN_RATE = 45
"""
Maximum radar turn rate in degrees per turn.
"""

MAX_SPEED = 8
"""
Maximum absolute speed in units per turn.
"""

MIN_FIREPOWER = 0.1
"""
Minimum firepower for the bot's gun.
The gun cannot fire with power less than this.
"""

MAX_FIREPOWER = 3
"""
Maximum firepower for the bot's gun.
The gun cannot fire with power greater than this.
"""

MIN_BULLET_SPEED = 20 - 3 * MAX_FIREPOWER
"""
Minimum bullet speed in units per turn.
Derived using the formula: 20 - 3 x MAX_FIREPOWER.
"""

MAX_BULLET_SPEED = 20 - 3 * MIN_FIREPOWER
"""
Maximum bullet speed in units per turn.
Derived using the formula: 20 - 3 x MIN_FIREPOWER.
"""

ACCELERATION = 1
"""
Acceleration in units per turn.
Incremental increase in speed when moving forward.
"""

DECELERATION = -2
"""
Deceleration in units per turn.
Incremental decrease in speed when moving backward.
The bot brakes faster than it accelerates forward.
"""

MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN = 8
"""
Maximum number of team messages that can be sent per turn.
Note: This is an assumed value.
"""

TEAM_MESSAGE_MAX_SIZE = 128
"""
Maximum size of a team message in bytes.
Note: This is an assumed value.
"""

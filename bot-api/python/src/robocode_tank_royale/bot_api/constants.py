"""
Constants for the Tank Royale Bot API.
"""

# Radius of the bounding circle of the bot
# A bot gets hit by a bullet when the bullet gets inside this bounding circle
BOUNDING_CIRCLE_RADIUS = 18

# Radius of the radar's scan beam
# Bots within this radius are detectable by the radar
SCAN_RADIUS = 1200

# Maximum possible driving turn rate (degrees per turn)
# The speed of the bot affects the turn rate
# Formula: MaxTurnRate - 0.75 x abs(speed)
MAX_TURN_RATE = 10

# Maximum gun turn rate (degrees per turn)
MAX_GUN_TURN_RATE = 20

# Maximum radar turn rate (degrees per turn)
MAX_RADAR_TURN_RATE = 45

# Maximum absolute speed (units per turn)
MAX_SPEED = 8

# Minimum firepower for the bot's gun
# The gun cannot fire with power less than this
MIN_FIREPOWER = 0.1

# Maximum firepower for the bot's gun
# The gun cannot fire with power greater than this
MAX_FIREPOWER = 3

# Minimum bullet speed (units per turn)
# Minimum bullet speed = 20 - 3 * MAX_FIREPOWER
MIN_BULLET_SPEED = 20 - 3 * MAX_FIREPOWER

# Maximum bullet speed (units per turn)
# Maximum bullet speed = 20 - 3 * MIN_FIREPOWER
MAX_BULLET_SPEED = 20 - 3 * MIN_FIREPOWER

# Acceleration (units per turn)
# Incremental increase in speed when moving forward
ACCELERATION = 1

# Deceleration (units per turn)
# Incremental decrease in speed when moving backward
# The bot brakes faster than it accelerates forward
DECELERATION = -2

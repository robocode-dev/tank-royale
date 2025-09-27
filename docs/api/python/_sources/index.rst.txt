Overview
========

This API is used when creating bots for the Robocode Tank Royale programming game. The API handles communication with a
game server behind the scene, so you can focus on the fun part of controlling the bot.

The Bot API is available here:

- `Bot API for Python <https://robocode-dev.github.io/tank-royale/api/python/index.html>`_

A good way to get started with Robocode Tank Royale is to head over to the general documentation for Tank Royale to
learn about the basics first:

- `Robocode Tank Royale Docs <https://robocode-dev.github.io/tank-royale/>`_

Another good way to get started is to look at the source files for the sample bots.

The bot classes
---------------

The first primary class that you should know about is the ``Bot`` class and perhaps the ``BaseBot``. The ``BaseBot`` class
provides all the base and minimum functionality of a bot and deals with the communication with the server. The ``Bot``
class is based on ``BaseBot``, but provides more convenient methods like e.g. blocking methods for moving and turning the
bot, and firing the gun.

Code example
------------

Here is an example of a simple bot using the Bot API written in Python and should run as a regular application.

MyFirstBot.py:

.. code-block:: python

   from robocode_tankroyale_botapi import Bot, BaseBot, BotInfo
   from robocode_tankroyale_botapi.events import ScannedBotEvent, HitByBulletEvent

   # ------------------------------------------------------------------
   # MyFirstBot
   # ------------------------------------------------------------------
   # A sample bot originally made for Robocode by Mathew Nelson.
   # Ported to Robocode Tank Royale by Flemming N. Larsen.
   #
   # Probably the first bot you will learn about.
   # Moves in a seesaw motion, and spins the gun around at each end.
   # ------------------------------------------------------------------
   class MyFirstBot(Bot):
       # Constructor, which loads the bot config file
       def __init__(self) -> None:
           super().__init__(BotInfo.from_file("MyFirstBot.json"))

       # Called when a new round is started -> initialize and do some movement
       def run(self) -> None:
           # Repeat while the bot is running
           while self.is_running:
               self.forward(100)
               self.turn_gun_left(360)
               self.back(100)
               self.turn_gun_left(360)

       # We saw another bot -> fire!
       def on_scanned_bot(self, evt: ScannedBotEvent) -> None:
           self.fire(1)

       # We were hit by a bullet -> turn perpendicular to the bullet
       def on_hit_by_bullet(self, evt: HitByBulletEvent) -> None:
           # Calculate the bearing to the direction of the bullet
           bearing = self.calc_bearing(evt.bullet.direction)

           # Turn 90 degrees to the bullet direction based on the bearing
           self.turn_right(90 - bearing)

   if __name__ == "__main__":
       # Start the bot and join a battle
       MyFirstBot().start()

The above code describes the behavior of the bot. In Python, the entry point is guarded by ``if __name__ == "__main__":``.
Using the bot API, we need to start the bot by calling the ``start()`` method, which will tell the server that this bot wants
to join the battle and also provide the server with the required bot info.

With the bot's constructor (``__init__``) we call the ``BotInfo.from_file(str)`` method that provides the bot info for the
server, like e.g. the name of the bot, and its author, etc.

The ``run()`` method is called when the bot needs to start its real execution to send instructions to the server.

The on-methods (for example, ``on_scanned_bot`` and ``on_hit_by_bullet``) are event handlers with code that triggers when a
specific type of event occurs. For example, the event handler ``on_scanned_bot`` triggers whenever an opponent bot is
scanned by the radar. The event instance (e.g. ``ScannedBotEvent``) contains the event data for the scanned bot.

JSON config file
----------------

The code in this example is accompanied by a ``MyFirstBot.json``, which is a JSON file containing the config file for
the bot, and is used by the **booter** to start up the bot on a local machine.

MyFirstBot.json:

.. code-block:: json

   {
     "name": "My First Bot",
     "version": "1.0",
     "authors": [
       "Mathew Nelson",
       "Flemming N. Larsen"
     ],
     "description": "A sample bot that is probably the first bot you will learn about.",
     "homepage": "",
     "countryCodes": [
       "us",
       "dk"
     ],
     "platform": "Python",
     "programmingLang": "Python 3.x"
   }

You can read more details about the format of this JSON file here:
https://robocode-dev.github.io/tank-royale/articles/booter.html#json-config-file.html


.. toctree::
   :maxdepth: 2
   :caption: Contents:

   api/modules

Indices and tables
------------------

* :ref:`genindex`
* :ref:`modindex`
* :ref:`search`

from abc import ABC


class DroidABC(ABC):
    """
    An abstract base class representing a specialized droid bot for team-based operations.

    A droid bot starts with 120 energy points, 20 more than a standard robot, but it lacks
    a scanner. Due to this limitation, droids rely entirely on their teammates to perform
    scanning tasks and share critical information, such as the coordinates of target enemies.

    Teams composed of droids and at least one non-droid team member gain a strategic
    advantage over similarly sized teams without droids, thanks to the increased energy pool.
    """
    pass
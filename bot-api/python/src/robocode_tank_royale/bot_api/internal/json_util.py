import json
import inspect
from typing import Type, Any

from robocode_tank_royale.schema import (
    BotDeathEvent,
    BotHitBotEvent,
    BotHitWallEvent,
    BulletFiredEvent,
    BulletHitBotEvent,
    BulletHitBulletEvent,
    BulletHitWallEvent,
    ScannedBotEvent,
    WonRoundEvent,
    TeamMessageEvent,
    Message,
    Color,
    BulletState,
)

_EVENT_CLASS_MAP: dict[str, Any] = {
    "BotDeathEvent": BotDeathEvent,
    "BotHitBotEvent": BotHitBotEvent,
    "BotHitWallEvent": BotHitWallEvent,
    "BulletFiredEvent": BulletFiredEvent,
    "BulletHitBotEvent": BulletHitBotEvent,
    "BulletHitBulletEvent": BulletHitBulletEvent,
    "BulletHitWallEvent": BulletHitWallEvent,
    "ScannedBotEvent": ScannedBotEvent,
    "WonRoundEvent": WonRoundEvent,
    "TeamMessageEvent": TeamMessageEvent,
    "BulletState": BulletState,
    "Color": Color,
}


def _sanitize_type_str(type_str: str) -> str:
    """
    Sanitizes a type string to get a clean class name.
    e.g. "robocode_tank_royale.schema.bullet_state.BulletState | None" -> "BulletState"
    """
    return type_str.split(".")[-1].split(" ")[0]


class MessageEncoder(json.JSONEncoder):
    def default(self, o: Any) -> Any:
        if isinstance(o, Color):
            return o.value
        if hasattr(o, "__dict__"):
            return o.__dict__
        return super().default(o)


def from_json(json_str: str) -> Message:
    """
    Deserializes a JSON string into a Message object.
    """
    obj = json.loads(json_str)
    msg_type = obj.get("type")
    if not msg_type:
        raise ValueError(
            "JSON object does not have a 'type' field for message deserialization"
        )

    if msg_type in _EVENT_CLASS_MAP:
        event_class = _EVENT_CLASS_MAP[msg_type]
        return _from_json_object(obj, event_class)

    # Fallback for other message types if needed
    # For now, we only handle events
    raise ValueError(f"Unknown message type: {msg_type}")


def _from_json_object(obj: dict[str, Any], klass: Type[Any]) -> Any:
    """
    Recursively deserializes a dictionary into an object of the specified class.
    """
    if not inspect.isclass(klass):
        return obj

    kwargs = {}
    sig = inspect.signature(klass.__init__)
    for key, value in obj.items():
        if key not in sig.parameters:
            # `key` not needed to construct the class.
            continue
        if isinstance(value, dict):
            param = sig.parameters[key]
            param_type = _EVENT_CLASS_MAP[_sanitize_type_str(str(param.annotation))]
            kwargs[key] = _from_json_object(value, param_type)  # type: ignore
        elif "color" == key.lower():
            assert _sanitize_type_str(str(sig.parameters[key].annotation)) == "Color"
            assert isinstance(value, str)
            kwargs[key] = Color(value=value)
        else:
            kwargs[key] = value
    return klass(**kwargs)


def to_json(obj: Message) -> str:
    """
    Serializes a Message object into a JSON string.
    """
    return json.dumps(obj, cls=MessageEncoder, sort_keys=True, indent=4)

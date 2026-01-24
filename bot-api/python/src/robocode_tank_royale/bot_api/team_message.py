"""Team message type registry for typed team message serialization/deserialization.

This module provides a registry mechanism for team message types, allowing bots to
send and receive typed objects (like RobotColors, Point) instead of raw dictionaries.
This matches the behavior of Java and C# Bot APIs.

Usage:
    # Using decorator
    @team_message_type
    @dataclass
    class RobotColors:
        body_color: Color
        turret_color: Color

    # Using function
    register_team_message_type(Point)

    # The type is automatically serialized/deserialized when using
    # broadcast_team_message() and on_team_message()
"""

from __future__ import annotations

import inspect
import json
from dataclasses import fields, is_dataclass
from typing import Any, Optional, Type, get_type_hints

# Registry mapping class names to class types
_registry: dict[str, Type[Any]] = {}


def team_message_type(cls: Type[Any]) -> Type[Any]:
    """Decorator to register a class as a team message type.

    The class name is used as the message type identifier for serialization.

    Args:
        cls: The class to register.

    Returns:
        The same class, unmodified.

    Example:
        @team_message_type
        @dataclass
        class RobotColors:
            body_color: Color
            turret_color: Color
    """
    register_team_message_type(cls)
    return cls


def register_team_message_type(cls: Type[Any]) -> None:
    """Register a class as a team message type.

    The class name is used as the message type identifier for serialization.

    Args:
        cls: The class to register.

    Example:
        register_team_message_type(Point)
    """
    _registry[cls.__name__] = cls


def get_team_message_type(name: str) -> Optional[Type[Any]]:
    """Look up a registered team message type by name.

    Args:
        name: The class name to look up.

    Returns:
        The registered class, or None if not found.
    """
    return _registry.get(name)


def _to_camel_case(snake_str: str) -> str:
    """Converts a snake_case string to camelCase."""
    components = snake_str.split("_")
    return components[0] + "".join(x.title() for x in components[1:])


def _to_snake_case(camel_str: str) -> str:
    """Converts a camelCase string to snake_case."""
    s = "".join(["_" + c.lower() if c.isupper() else c for c in camel_str])
    return s.lstrip("_")


def serialize_team_message(obj: Any) -> str:
    """Serialize a team message object to JSON string.

    Handles Color objects by converting them to hex strings.
    Converts snake_case field names to camelCase for JSON.

    Args:
        obj: The object to serialize.

    Returns:
        JSON string representation.
    """
    from robocode_tank_royale.bot_api.graphics.color import Color

    def convert(o: Any) -> Any:
        if isinstance(o, Color):
            return o.to_hex_color()
        if is_dataclass(o) and not isinstance(o, type):
            result = {}
            for field in fields(o):
                value = getattr(o, field.name)
                result[_to_camel_case(field.name)] = convert(value)
            return result
        if isinstance(o, dict):
            return {_to_camel_case(k) if isinstance(k, str) else k: convert(v) for k, v in o.items()}
        if isinstance(o, (list, tuple)):
            return [convert(item) for item in o]
        return o

    return json.dumps(convert(obj), separators=(",", ":"))


def deserialize_team_message(json_str: str, message_type: str) -> Any:
    """Deserialize a JSON string to a team message object.

    Uses the message_type to look up the registered class and instantiate it.
    Handles Color fields by converting hex strings to Color objects.

    Args:
        json_str: The JSON string to deserialize.
        message_type: The class name to deserialize into.

    Returns:
        The deserialized object, or a dictionary if the type is not registered.
    """
    from robocode_tank_royale.bot_api.graphics.color import Color

    data = json.loads(json_str)

    cls = get_team_message_type(message_type)
    if cls is None:
        # Type not registered, return raw dictionary
        return data

    return _deserialize_object(data, cls)


def _deserialize_object(data: dict[str, Any], cls: Type[Any]) -> Any:
    """Deserialize a dictionary into an instance of the given class.

    Uses type hints to identify Color fields and convert hex strings.

    Args:
        data: The dictionary to deserialize.
        cls: The class to instantiate.

    Returns:
        An instance of the class.
    """
    from robocode_tank_royale.bot_api.graphics.color import Color

    # Get type hints for the class
    try:
        hints = get_type_hints(cls)
    except Exception:
        hints = {}

    # Convert camelCase keys to snake_case
    snake_data = {_to_snake_case(k): v for k, v in data.items()}

    # Build kwargs for constructor
    kwargs: dict[str, Any] = {}

    if is_dataclass(cls):
        for field in fields(cls):
            field_name = field.name
            if field_name not in snake_data:
                continue

            value = snake_data[field_name]
            field_type = hints.get(field_name, field.type)

            # Handle Color type
            if _is_color_type(field_type):
                if value is not None and isinstance(value, str):
                    kwargs[field_name] = Color.from_hex_color(value)
                else:
                    kwargs[field_name] = None
            # Handle Optional[Color]
            elif _is_optional_color_type(field_type):
                if value is not None and isinstance(value, str):
                    kwargs[field_name] = Color.from_hex_color(value)
                else:
                    kwargs[field_name] = None
            else:
                kwargs[field_name] = value
    else:
        # For non-dataclass, try to use __init__ signature
        sig = inspect.signature(cls.__init__)
        for param_name, param in sig.parameters.items():
            if param_name == "self":
                continue
            if param_name not in snake_data:
                continue

            value = snake_data[param_name]
            field_type = hints.get(param_name, param.annotation)

            if _is_color_type(field_type) or _is_optional_color_type(field_type):
                if value is not None and isinstance(value, str):
                    kwargs[param_name] = Color.from_hex_color(value)
                else:
                    kwargs[param_name] = None
            else:
                kwargs[param_name] = value

    return cls(**kwargs)


def _is_color_type(type_hint: Any) -> bool:
    """Check if a type hint is the Color type."""
    from robocode_tank_royale.bot_api.graphics.color import Color

    if type_hint is Color:
        return True
    if isinstance(type_hint, str) and type_hint == "Color":
        return True
    return False


def _is_optional_color_type(type_hint: Any) -> bool:
    """Check if a type hint is Optional[Color]."""
    from robocode_tank_royale.bot_api.graphics.color import Color

    # Handle typing.Optional[Color] which is Union[Color, None]
    origin = getattr(type_hint, "__origin__", None)
    if origin is not None:
        # Python 3.10+ uses types.UnionType for X | Y
        import sys
        if sys.version_info >= (3, 10):
            import types
            if origin is types.UnionType:
                args = getattr(type_hint, "__args__", ())
                return Color in args or type(None) in args

        # typing.Union or typing.Optional
        from typing import Union
        if origin is Union:
            args = getattr(type_hint, "__args__", ())
            return Color in args

    return False


__all__ = [
    "team_message_type",
    "register_team_message_type",
    "get_team_message_type",
    "serialize_team_message",
    "deserialize_team_message",
]

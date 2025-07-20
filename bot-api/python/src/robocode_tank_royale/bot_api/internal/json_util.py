import json
import inspect
from typing import Type, Any

from robocode_tank_royale import schema


def to_camel_case(snake_str: str) -> str:
    """Converts a snake_case string to camelCase."""
    components = snake_str.split("_")
    return components[0] + "".join(x.title() for x in components[1:])


def to_snake_case(camel_str: str) -> str:
    """Converts a camelCase string to snake_case."""
    s = "".join(["_" + c.lower() if c.isupper() else c for c in camel_str])
    return s.lstrip("_")


def _sanitize_type_str(type_str: str) -> str:
    """
    Sanitizes a type string to get a clean class name.
    e.g. "robocode_tank_royale.schema.bullet_state.BulletState | None" -> "BulletState"
    """
    if type_str.startswith("list["):
        # Handle list types like "list[BulletState]"
        type_str = type_str[5:-1]  # Remove "list[" and "]"
    # Remove the module path and any type annotations like | None
    return type_str.split(".")[-1].split(" ")[0]


class MessageEncoder(json.JSONEncoder):
    def default(self, o: Any) -> Any:
        if isinstance(o, schema.Color):
            return o.value
        if hasattr(o, "__dict__"):
            return {to_camel_case(k): v for k, v in o.__dict__.items() if not k.startswith("_")}
        return super().default(o)


def from_json(json_str_or_dict: str | dict[str, Any]) -> schema.Message:
    """
    Deserializes a JSON string into a Message object.
    """
    if isinstance(json_str_or_dict, str):
        json_str = json_str_or_dict
        obj = json.loads(json_str)
    else:
        obj = json_str_or_dict
    msg_type = obj.get("type")
    if not msg_type:
        raise ValueError(
            "JSON object does not have a 'type' field for message deserialization"
        )

    if msg_type in schema.CLASS_MAP:
        event_class = schema.CLASS_MAP[msg_type]
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
        key = to_snake_case(key)
        if key not in sig.parameters:
            # `key` not needed to construct the class.
            continue

        if isinstance(value, dict):
            param = sig.parameters[key]
            param_type = schema.CLASS_MAP[_sanitize_type_str(str(param.annotation))]
            kwargs[key] = _from_json_object(value, param_type)  # type: ignore
        elif isinstance(value, list):
            param = sig.parameters[key]
            param_type = schema.CLASS_MAP.get(_sanitize_type_str(str(param.annotation)), None)
            if param_type is not None:
                deserialized_items = []
                for item in value:  # type: ignore
                    assert isinstance(item, dict)
                    item_type = schema.CLASS_MAP[item["type"]] if 'type' in item else param_type
                    deserialized_items.append(_from_json_object(item, item_type))  # type: ignore
                kwargs[key] = deserialized_items
            else:
                kwargs[key] = value
        elif "color" == key.lower():
            assert _sanitize_type_str(str(sig.parameters[key].annotation)) == "Color"
            assert isinstance(value, str)
            kwargs[key] = schema.Color(value=value)
        else:
            kwargs[key] = value
    result = klass(**kwargs)
    return result


def to_json(obj: schema.Message) -> str:
    """
    Serializes a Message object into a JSON string.
    """
    return json.dumps(obj, cls=MessageEncoder, sort_keys=True, indent=4)

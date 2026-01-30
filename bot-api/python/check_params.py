import inspect
from robocode_tank_royale.schema import BotIntent

sig = inspect.signature(BotIntent.__init__)
print("BotIntent __init__ parameters:")
for name in sig.parameters:
    print(f"  - {name}")

pip install pyyaml mypy
python3 scripts/update_version.py
python3 scripts/schema_to_python.py -d ../../schema/schemas -o generated/robocode_tank_royale/tank_royale/schema
stubgen src/robocode_tank_royale -o src/robocode_tank_royale-stubs
stubgen generated/robocode_tank_royale -o generated/robocode_tank_royale-stubs
touch generated/robocode_tank_royale/tank_royale/schema/py.typed

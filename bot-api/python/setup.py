from setuptools import setup, find_packages  # type: ignore - setuptools.setup is missing type.

with open('VERSION', 'r', encoding='utf-8') as f:
    version = f.read()

setup(
    name="robocode-tank-royale",
    version=version,
    description="The Python Bot API for Robocode Tank Royale",
    long_description=open("README-PyPI.md", encoding='utf-8').read(),
    long_description_content_type="text/markdown",
    url="https://robocode-dev.github.io/tank-royale",
    package_dir={
        # Core bot API lives under src
        "robocode_tank_royale.bot_api": "src/robocode_tank_royale/bot_api",
        # Install the lightweight shim as robocode_tank_royale.schema
        # This shim forwards imports to the generated schema package at runtime
        "robocode_tank_royale.schema": "src/robocode_tank_royale/schema",
        # Ship the generated schema as an actual package under the 'generated' namespace
        # so the shim can import it in installed environments
        "generated.robocode_tank_royale.tank_royale.schema": "generated/robocode_tank_royale/tank_royale/schema",
    },
    packages=[
        # Core API packages
        "robocode_tank_royale.bot_api",
        # Shim package (re-exports generated types)
        "robocode_tank_royale.schema",
        # Generated schema package (installed under the 'generated' namespace)
        "generated.robocode_tank_royale.tank_royale.schema",
    ]
    + [
        f"robocode_tank_royale.bot_api.{subpackage}"
        for subpackage in find_packages("src/robocode_tank_royale/bot_api")
    ],
    python_requires=">=3.10",
    install_requires=open("requirements.txt", encoding='utf-8').read().splitlines(),
)

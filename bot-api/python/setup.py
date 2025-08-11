from setuptools import setup, find_packages  # type: ignore - setuptools.setup is missing type.

with open('VERSION', 'r') as f:
    version = f.read()

setup(
    name="robocode-tank-royale",
    version=version,
    description="The Python Bot API for Robocode Tank Royale",
    long_description=open("README.md").read(),
    long_description_content_type="text/markdown",
    url="https://robocode-dev.github.io/tank-royale",
    package_dir={
        "robocode_tank_royale.bot_api": "src/robocode_tank_royale/bot_api",
        "robocode_tank_royale.schema": "generated/robocode_tank_royale/tank_royale/schema",  # Specific override for schema
    },
    packages=[
        "robocode_tank_royale.bot_api",
        "robocode_tank_royale.schema",
    ]
    + [
        f"robocode_tank_royale.bot_api.{subpackage}"
        for subpackage in find_packages("src/robocode_tank_royale/bot_api")
    ],
    python_requires=">=3.10",
    install_requires=open("requirements.txt").read().splitlines(),
)

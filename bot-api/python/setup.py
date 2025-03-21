from setuptools import setup, find_packages
import configparser

# Read version from gradle.properties file
config = configparser.ConfigParser()
with open('../../gradle.properties') as f:
    config.read_string(f"[default]\n{f.read()}")
version = config.get('default', 'version')

setup(
    name='robocode-tank-royale',
    version=version,
    description='The Python Bot API for Robocode Tank Royale',
    long_description=open('README.md').read(),
    long_description_content_type='text/markdown',
    url='https://robocode-dev.github.io/tank-royale',
    package_dir={
        "robocode.tank_royale.schema": "generated/robocode/tank_royale/schema",
        "robocode.tank_royale.bot_api": "generated/robocode/tank_royale/bot_api",
    },
    packages=find_packages(where='src') + find_packages(where='generated'),
    python_requires='>=3.10',
    install_requires=open('requirements.txt').read().splitlines(),
)

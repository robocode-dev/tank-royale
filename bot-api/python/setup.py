from setuptools import setup, find_packages
from setuptools.errors import InternalError
import subprocess
import os
import configparser

# Run the schema generation script
result = subprocess.run(['python', 'schema_to_python.py', '-d', '../../schema/schemas/', '-o', 'generated/tank_royale/schema'])
if result.returncode != 0:
    raise InternalError(f'Schema generation return code {result.returncode}: {result.stderr}')

# Create an empty __init__.py file in the generated/tank_royale directory
init_file_path = 'generated/tank_royale/__init__.py'
open(init_file_path, 'a').close()
if not os.path.exists(init_file_path):
    raise InternalError(f'Failed to create file: {init_file_path}')

# Read version from gradle.properties file
config = configparser.ConfigParser()
with open('../../gradle.properties') as f:
    config.read_string(f"[default]\n{f.read()}")
version = config.get('default', 'version')

setup(
    name='robocode-tank-royale',
    version=version,
    package_dir={"src": "src", "generated": "generated"},
    packages=find_packages(where='src') + find_packages(where='generated'),
    python_requires='>=3.10',
    install_requires=[
        'pillow>11.0',
        'PyYAML>6.0'
    ],
    description='The Python bot API for Robocode Tank Royale',
    long_description=open('README.md').read(),
    long_description_content_type='text/markdown',
    url='https://robocode-dev.github.io/tank-royale',
)
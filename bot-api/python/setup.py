from setuptools import setup, find_packages
from setuptools.errors import InternalError
import subprocess
import os

# Run the schema generation script
result = subprocess.run(['python', '../../schema/python/schema_to_python.py', '-d', '../../schema/schemas/', '-o', 'generated/tank_royale/schema'])
if result.returncode != 0:
    raise InternalError(f'Schema generation return code {result.returncode}: {result.stderr}')

# Create an empty __init__.py file in the generated/tank_royale directory
init_file_path = 'generated/tank_royale/__init__.py'
open(init_file_path, 'a').close()
if not os.path.exists(init_file_path):
    raise InternalError(f'Failed to create file: {init_file_path}')

setup(
    name='tank_royale',
    version='0.1.0',
    package_dir={"": "src", "": "generated"},
    packages=find_packages(where='src') + find_packages(where='generated'),
    python_requires='>=3.10',
    install_requires=[
        'pillow',
    ],
    description='A python bot API for Robocode Tank Royale.',
    long_description=open('README.md').read(),
    long_description_content_type='text/markdown',
    url='https://robocode-dev.github.io/tank-royale',
)

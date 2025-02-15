from setuptools import setup, find_packages

setup(
    name='tank_royale',
    version='0.1.0',
    package_dir={'': 'src'},
    packages=find_packages(where='src'),
    python_requires='>=3.10',
    install_requires=[
        'pillow',
    ],
    description='A python bot API for Robocode Tank Royale.',
    long_description=open('README.md').read(),
    long_description_content_type='text/markdown',
    url='https://robocode-dev.github.io/tank-royale',
)

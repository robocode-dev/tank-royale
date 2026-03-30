# Copy version from root VERSION file
with open('../../VERSION', 'r') as f:
    version = f.read().strip()
with open('VERSION', 'w') as f:
    f.write(version)


# Use Debian slim as the base for minimal size
FROM debian:12-slim

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

# Install essential dependencies
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    ca-certificates \
    git \
    unzip \
    libicu72 \
    && rm -rf /var/lib/apt/lists/*

# Install .NET SDK 8.0
RUN wget https://dot.net/v1/dotnet-install.sh -O dotnet-install.sh \
    && chmod +x dotnet-install.sh \
    && ./dotnet-install.sh --channel 8.0 --install-dir /usr/share/dotnet \
    && rm dotnet-install.sh \
    && ln -s /usr/share/dotnet/dotnet /usr/bin/dotnet

# Install Java 11 and 17 from Adoptium (Eclipse Temurin)
# Java 17 for running Gradle 9.x, Java 11 as toolchain for building
RUN apt-get update && apt-get install -y \
    gnupg \
    && wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor > /usr/share/keyrings/adoptium.gpg \
    && echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb bookworm main" > /etc/apt/sources.list.d/adoptium.list \
    && apt-get update \
    && apt-get install -y temurin-17-jdk temurin-11-jdk \
    && rm -rf /var/lib/apt/lists/*

# Install Python 3.11 (Debian 12 comes with 3.11)
RUN apt-get update && apt-get install -y \
    python3 \
    python3-pip \
    python3-venv \
    && rm -rf /var/lib/apt/lists/*

# Set Python 3 as default
RUN ln -sf /usr/bin/python3 /usr/bin/python

# Set environment variables
# Use Java 17 as default (for Gradle 9.x), but Java 11 is available as toolchain
ENV JAVA_HOME=/usr/lib/jvm/temurin-17-jdk-amd64
ENV DOTNET_ROOT=/usr/share/dotnet

# Install DocFX 2.78.4 as a .NET tool
RUN dotnet tool install -g docfx --version 2.78.4

# Create a non-root user
ARG USERNAME=devuser
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && apt-get update \
    && apt-get install -y sudo \
    && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME \
    && rm -rf /var/lib/apt/lists/*

# Switch to non-root user
USER $USERNAME

# Install DocFX 2.78.4 as a .NET tool for the user
RUN dotnet tool install -g docfx --version 2.78.4

# Update PATH for the user
ENV PATH="${PATH}:/home/${USERNAME}/.dotnet/tools"

# Verify installations
RUN dotnet --version && \
    java -version && \
    python --version && \
    docfx --version

WORKDIR /workspace
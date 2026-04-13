# Minimal base image — tool installation is handled by scripts/setup.sh
FROM debian:12-slim

ENV DEBIAN_FRONTEND=noninteractive

# Install only the prerequisites needed before setup.sh can run
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    ca-certificates \
    git \
    unzip \
    sudo \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user
ARG USERNAME=devuser
ARG USER_UID=1000
ARG USER_GID=$USER_UID

RUN groupadd --gid $USER_GID $USERNAME \
    && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
    && echo "$USERNAME ALL=(root) NOPASSWD:ALL" > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

USER $USERNAME
WORKDIR /workspace

# Docs

This module contains the user-facing documentation sources for Robocode Tank Royale under `web/docs`.

[VitePress] generates the static site. The complete publishable Pages artifact, including generated API references, is staged under the repository-root `build/pages` directory; repository-root `/docs` is reserved for the Cliewen corpus and must never receive generated output.

## Prerequisites

- **Node.js 22 (LTS)** — required by [VitePress] to build the documentation site.
  Install via [NodeSource](https://github.com/nodesource/distributions):
  ```bash
  curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
  sudo apt-get install -y nodejs
  ```
- **JDK 17+** and the rest of the build tools — see [DEVELOPMENT.md](../DEVELOPMENT.md).

## Building the docs

The docs are build by this command:

```shell
../gradlew :web:clean :web:build
```

In order to run and view the generated documentation, you write:

```shell
../gradlew :web:run
```

Now you can view the documentation with a browser from the link provided by VitePress as a result of generating the
documentation.

## Staging the published site

Generate the VitePress site and every API reference in the same layout deployed by GitHub Pages:

```shell
../gradlew upload-docs
```

The disposable output is written to `../build/pages`. Do not commit it; the Pages workflow regenerates the same artifact from accepted `main`.

[VitePress]: https://vitepress.dev/ "VitePress home page"

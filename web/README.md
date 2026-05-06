# Docs

This module contains the documentation for Robocode Tank Royale.

[VitePress] is being used for generating static web content for the Robocode documentation. You will find all
documentation under the `/docs` directory in the root directory.

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

## Uploading the docs

After having built the docs you can upload the documentation to GitHub docs using this command:

```shell
../gradlew :web:clean
```

This will create new files, update existing ones, and remove others.

The files need to be committed to git:

```shell
cd ../docs
git add .
git commit -m "Updated docs"
```

[VitePress]: https://vitepress.dev/ "VitePress home page"

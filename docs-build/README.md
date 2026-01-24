# Docs

This module contains the documentation for Robocode Tank Royale.

[VitePress] is being used for generating static web content for the Robocode documentation. You will find all
documentation under the `/docs` directory in the root directory.

## Prerequisites

* node.js 16+
* Java JDK 11+

Check out the official prerequisite for
VitePress [here](https://vitepress.dev/guide/getting-started#prerequisites)
and for Gradle [here](https://gradle.org/install/#prerequisites).

## Building the docs

The docs are build by this command:

```shell
../gradlew :docs-build:clean :docs-build:build
```

In order to run and view the generated documentation, you write:

```shell
../gradlew :docs-build:run
```

Now you can view the documentation with a browser from the link provided by VitePress as a result of generating the
documentation.

## Uploading the docs

After having built the docs you can upload the documentation to GitHub docs using this command:

```shell
../gradlew :docs-build:clean
```

This will create new files, update existing ones, and remove others.

The files need to be committed to git:

```shell
cd ../docs
git add .
git commit -m "Updated docs"
```

[VitePress]: https://vitepress.dev/ "VitePress home page"

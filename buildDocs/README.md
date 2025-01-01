# Docs

This module contains the documentation for Robocode Tank Royale.

[VuePress] is being used for generating static web content for the Robocode documentation. You will find all
documentation under the `/docs` directory in the root directory.

## Prerequisites

* node.js 10+
* Java JDK 11+

Check out the official prerequisite for
VuePress [here](https://vuepress.vuejs.org/guide/getting-started.html#prerequisites)
and for Gradle [here](https://gradle.org/install/#prerequisites).

## Building the docs

The docs are build by this command:

```shell
./gradlew :buildDocs:clean :buildDocs:build
```

In order to run and view the generated documentation, you write:

```shell
./gradlew :buildDocs:run
```

Now you can view the documentation with a browser from the link provided by VuePress as a result of generating the
documentation.

## Uploading the docs

After having built the docs you can upload the documentation to GitHub docs using this command:

```shell
./gradlew :buildDocs:clean
```

This will create new files, update existing ones, and remove others.

The files need to be committed to git:

```shell
cd ../docs
git add .
git commit -m "Updated docs"
```

[VuePress]: https://vuepress.vuejs.org/ "VuePress home page"

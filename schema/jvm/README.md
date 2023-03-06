# Schema code generator for Java/JVM

This module contains a code generator that takes the schemas in YAML format as input and produces Java classes as
output.

## Build commands

Note the sources of this project is being included in other projects ([bot-api/java](../../bot-api/java)
and [server](../../server)). Hence, the generated artifact is currently not being used, but the build commands are still
available, e.g. for checking that schemas can be generated to Java files without errors.

#### Clean build directory:

```shell
./gradlew :schema:jvm:clean
```

#### Create archive

```shell
./gradlew :schema:jvm:build
```

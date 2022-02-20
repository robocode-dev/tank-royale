# Schema code generator for Java

This directory contains a code generator that takes the schemas in YAML format as input and produces Java classes as
output.

The generator is outputs a Maven artifact, `robocode-tankroyale-schema-0.x.y.z`, that can be pushed to the local or
remote Maven repository. The [server]
and [Bot API for Java] are both using the generated artifact from this generator.

## Build Commands

#### Publish to the local Maven repository

    gradle publishToMavenLocal


[server]: ../../server/README.md

[Bot API for Java]: ../../bot-api/java/README.md
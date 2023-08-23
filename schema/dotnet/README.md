# Schema code generator for .Net

This module contains a code generator that takes the schemas in YAML format as input and produces C# classes for .Net as
output.

The code generator is "homemade" in lack of a code generator that can produce C# classes based on YAML schemas. If such
generator exists or arrives someday, this generator should be replaced.

## Setting the target output directory

Currently, the generator outputs the generated classes to the Bot API for C# under `/bot-api/dotnet/src/generated`. This
is controlled bt the `Properties/launchSettings.json` file with the `commandLineArgs`, which takes a string as input
containing: `"«filepath of schemas directory» «filepath to output directory»"`

#### Clean build directory:

```shell
./gradlew :schema:dotnet:clean
```

#### Create archive

```shell
./gradlew :schema:dotnet:build
```

## Generating the files with dotnet

You can also generate the C# classes by using the `dotnet` command:

```
dotnet run «filepath of schemas directory» «filepath to output directory»
```

For example:

```
dotnet run C:/Code/tank-royale/schema/schemas C:/Code/tank-royale/bot-api/dotnet/src/generated
```
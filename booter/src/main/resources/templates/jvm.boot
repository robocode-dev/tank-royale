# JVM boot template
# ${base} - base class (from .json or directory name)
# ${classPath} - classpath (typically ../lib/*)

# Standard Java execution
[sh]
java -cp "${classPath}" "${base}.java"

[cmd]
java -cp "${classPath}" "${base}.java"

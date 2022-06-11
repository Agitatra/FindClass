

# FindClass

The application looks for one or more classes in folder trees and Java-Archives contained within these folders.

## Command Line Parameters

The application accepts the following parameters
### Commands
All commands start with one hyphen.
- -jarfilter regular-expression [regular-expression...}

    All subsequent positional parameters (every term not starting with a hyphen) are regarded as a regular expression.  These regular expressions are used to limit the Java-archives that are being searched.  Any number of positional parameters are allowed.
    **N.B.**: The regular expression are not filesystem wildcards (i.e. "*" or "?") but regular expressions in the Java dialect (see https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).  So instead of: "*" ".*" ist to used, "." is used instead of "?".  This means that periods that are to be used literally have to be quoted with a backslash.  On *ix-systems the backslash has to be quoted by a second one.  So on non *ix-systems the Java archive "pille.jar" has to be specified as: "pille\\.jar" on *ix-systems as: "pille\\\\.jar".

- -packagefilter regular-expression [regular-expression...}

    All subsequent positional parameters (every term not starting with a hyphen) are regarded as a regular expression.  These regular expressions are used to limit the Java-packages for which a search is performed.  Any number of positional parameters are allowed.
    **N.B.**: The regular expression are not filesystem wildcards (i.e. "*" or "?") but regular expressions in the Java dialect (see https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).  So instead of: "*" ".*" ist to used, "." is used instead of "?".  This means that periods that are to be used literally have to be quoted with a backslash.  On *ix-systems the backslash has to be quoted by a second one.  So on non *ix-systems the Java package "java.io" has to be specified as: "java\\.io" on *ix-systems as: "java\\\\.io".

- -classfilter regular-expression [regular-expression...}

    All subsequent positional parameters (every term not starting with a hyphen) are regarded as a regular expression.  These regular expressions are used to limit the Java class for which a search is performed.  Any number of positional parameters are allowed.
    **N.B.**: The regular expression are not filesystem wildcards (i.e. "*" or "?") but regular expressions in the Java dialect (see https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html).  So instead of: "*" ".*" ist to used, "." is used instead of "?".

- -verbose

    Additional information messages are logged to the standard output.

### Positional parameters

The meaning of positional parameters change with the Commands provided.
- As long as neither "-jarfilter", "-packagefilter" nor "-classfilter" commands are specified and only one positional parameter is given, the positional parameter is regarded as a class specification, identical to being preceded by a "-classfilter" command.
- If no command but two positional parameters are provided, the first specifes a starting point within the file system, and the second the class filter.
- All positional parameters behind a command ("-jarfilter", "-packagefilter" or "-classfilter") are filter specifications for their preceding commands, either until another command or all parameters are exhausted.

### Examples

The following parameter searches for the the standard Java string class in all Java archives below the current folder
- $ java -jar FindClass.jar java.lang.String
The following parameter searches for a class whose name ends with "String" in all Java archives below the current folder
- $ java -jar FindClass.jar String

The following example search for all classes from the package: "java.io" in all Java archives called "palle.jar", "pille.jar" and "pulle.jar" in the current folder and below.  The example assumes that "FindClass" is called from a *ix like shell, therefore backslashes have to be quoted.
- j$ ava FindClass.jar -jarfilter "p[aiu]lle\\\\.jar" -packagefilter "java\\\\.io"

The following example searches in a *ix-shell for all classes in the package "java.io" and the class: "java.lang.String" in all Java archives called: "pulle.jar", "bla.jar", "blub.jar" und "blubber.jar" in the folder: "/usr/java/lib" and below. During the execution additional log messages are shown.
- $ java FindClass /usr/java/lib -verbose -jarfilter "p[aiu]lle\\\\.jar" "bl(a|ub|ubber)\\\\.jar" -packagefilter "^java\\\\.io" -classfilter "java\\\\.lang\\\\.String"


### Licenses
The project is licensed under both the Apache 2 and the MIT license.
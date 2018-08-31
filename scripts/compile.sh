#!/bin/bash

# check arguments
if [ "$#" -ne 2 ]; then
    echo "Usage: ./compile.sh [MAIN_CLASS_FULL_NAME] [SOURCE_FILES]"
    exit 1
fi

# switch to the current folder
cd "$(dirname $(realpath $0))"

# search the javac executable
if type -p javac; then
    JAVAC=javac
    JAR=jar
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/javac" ]];  then
    JAVAC="$JAVA_HOME/bin/javac"
    JAR="$JAVA_HOME/bin/jar"
else
    echo "No javac found in your system!"
    exit 2
fi

# clean
echo "Cleaning the build folder..."
rm -fr "./build"

# compile
echo "Compiling the source code..."
$JAVAC -cp "./lib/*" -d "./build" "${@:2}"  || exit 3

# assemble the bytecode
echo "Assembling the final jar..."
cd "./build"
$JAR -xf "../lib/org-aion-avm-userlib.jar"
echo "Main-Class: $1" > "./META-INF/MANIFEST.MF"
$JAR -cf "dapp.jar" .
cd ..

# done!
echo "Success!"

echo "The jar has been generated at: $(realpath ./build/dapp.jar)"

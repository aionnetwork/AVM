#!/bin/bash

# TODO: build the org.aion.avm.rt package and release a jar

# create temporary folder
mkdir -p out/org/aion/avm
cd out

# copy rt classes
cp -r ../src/org/aion/avm/rt org/aion/avm

# restore byte[]
sed -i 's/ByteArray/byte[]/g' org/aion/avm/rt/*.java

# return
cd ..
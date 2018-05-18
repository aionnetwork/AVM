#!/bin/bash

# reset the temporary out folder
rm -fr out
mkdir -p out/org/aion/avm
cd out

# copy runtime files
cp -r ../src/org/aion/avm/rt org/aion/avm

# restore byte[] type
sed -i 's/import org.aion.avm.arraywrapper.ByteArray;//g' org/aion/avm/rt/*.java
sed -i 's/ByteArray/byte[]/g' org/aion/avm/rt/*.java

# generate module info
echo "module org.aion.avm.rt {
    exports org.aion.avm.rt;
}" > module-info.java

# compile
javac org/aion/avm/rt/*.java module-info.java

# package
jar --create --file=org-aion-avm-rt.jar --module-version=1.0 .

# return
cd ..
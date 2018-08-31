# User guide

This pages describes how to use the AVM release.

## How to compile

To compile your DAapp source code into a deployable jar file,  run the following command:
```
./compile.sh [MAIN_CLASS_FULL_NAME] [SOURCE_FILES]
```
The generated jar will be located at the `build` folder.


## How to deploy

Once you get code is compiled, your can deploy it via:
```
java -jar avm.jar deploy [PATH_TO_THE_JAR]
```

## How to make a transfer/call

To make a call to a deployed DApp, run:
```
java -jar avm.jar call [THE_DAPP_ADDRESS] -m [THE_METHOD_NAME] -a [THE_ARGUMENT_LIST]
```

The argument list has to be in the following format:
```
-I int
-J long
-S short
-C char
-F float
-D double
-B byte
-Z boolean
-A Address
```

For example, to call the transfer method, run:
```
java -jar avm.jar call 0x1122334455667788112233445566778811223344556677881122334455667788 \
-m transfer \
-a -A 0x1122334455667788112233445566778811223344556677881122334455667788 -J 100
```

## How to explore the storage

The check the storage of a deployed DApp, run:
```
java -jar avm.jar explore [THE_DAPP_ADDRESS]
```
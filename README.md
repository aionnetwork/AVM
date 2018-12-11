# Aion Virtual Machine

# Overview

The Aion Virtual Machine (AVM) is a Turing complete execution engine that is core to the Aion Network. The **AVM runs Java bytecode on the blockchain**, enabling developers to build blockchain-based applications in Java. Its goal is to provide a robust global ecosystem that is familiar to the mainstream developer audience to unlock the potential of blockchain applications without learning new tools, frameworks or languages. The AVM will run alongside the solidity-compatible FastVM.

## AVM & JVM

**AVM is not a modification or rewrite of the underlying JVM**, but a bytecode transformer and runtime library, which provides control over how developers are allowed to interact with the AVM. It isolates DApps from each other while restricting access to class libraries.

## Storage

The storage system is an object graph based on reachability from the user code’s static variables, at the time the contract terminates.  This means that **storage is transparent to the developer** and can be optimized by the AVM.

Additionally, the top-level execution engine speculatively **runs all transactions concurrently**, abandoning partial transactions when data hazards are detected.


# Features

## Performance

The AVM uses the JIT compiler which optimizes the DApp code. It also features multi-threading, allowing increased throughput via concurrent transaction execution as opposed to most blockchain VMs.

## Reliability & Maturity

The core of the JVM is a very mature piece of software, changing little between releases and being used for a myriad of purposes, across countless environments, for over 2 decades. Anything we can do to leverage that history avoids potential pit-falls.

## Developer-friendly

The Java-based AVM leverages the entire Java tooling ecosystem, making developer onboarding and experience extremely streamlined. You get the full developer package including the compiler and IDE straight out of the box! That said, the AVM taps into a well-established community that has claimed the top spot for the #1 language (Java) in the past few years.


# How it Works

![AVM Architecture](https://aion.network/media/AVM-How-it-Works.png)

This diagram shows the high-level connections between these components as they apply to the case of deploying a new DApp. The user’s code is written as standard Java, using the standard class library (JCL) and an API we provide for accessing blockchain-specific functionality.

Upon deployment, this code goes through our transformer (a pipeline of ASM-based bytecode modification classes):

* Instrumentation is added to give us control (hide internal exception use-cases, implement billing, etc).  This results in adding calls to a runtime support library which will be present during execution.
* Reparenting/renaming of the class and method references is done.  This ensures that any JCL references are redirected to our “shadow JCL”, which we restrict/bill/control.  This ensures no access to externals or non-deterministic operations.
* References to our API and to the user’s own code are updated to continue to map to these targets in their new locations/shapes.

This instrumented code is saved and is what is loaded and executed when transactions arrive, in the future.

![Storage System](https://aion.network/media/AVM-Storage-System.png)

While most DApp environments expose storage as a key-value store (often with rigid key and value size requirements), the AVM exposes the data store as a generic object graph, derived directly from the objects alive in the user’s address space, as reachable from their static variables, when a transaction completes.

Unreachable objects are cleaned up, for a refund, by a special GC transaction, which a node is allowed to run on any contract it wishes when it mines a block. This means that the storage is transparent to the user’s code, thus moving all such correctness concerns into the domain of the virtual machine.

Additionally, since the reachable graph could be larger than the heap, objects are only loaded lazily (when a field is first accessed), incurring the billing cost for the IO load at this time.  Correspondingly, the modified or reachable objects are written back to the storage after the transaction completes, incurring billing cost for the IO store at that time.

# Build and Test

## Prerequisites

```
JDK 10 or higher
Apache Ant 1.10 or higher
```


## Build

```
ant
```


## Run the tests
```
ant test
```


## License

This project is licensed under the [MIT License](./LICENSE.txt).

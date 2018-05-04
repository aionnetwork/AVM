# AVM - VM for AION smart contracts

There are two main things that AVM does for a contract : estimates contract's computation cost and guarantees its deterministic execution.

## Computation Cost Estimation (CCE).

* Insert cost computation instructions into bytecode.
* Assign cost budget per bytecode type. E.g., there are expensive bytecodes (method invocation, throwing exception, backward jump).
* Allocated memory estimation

## Non-determinism

Basically, there are three main sources that may influence a contract to be computed in different ways:
1. Operation system
2. Hardware
3. JVM itself

As a solution to that, several techniques are proposed to be implemented as an extensible mechanisms:
1. **Class usage analysis (CUA)**. Analyze which classes being loaded and forbid the ones that are not in white list of non-deterministic. (E.g., socket class wouldn't be whitelisted)
2. **Bytecode transformation (BCT)**. E.g., insert 'strictfp' when need.
3. **Patch JVM (or use agent) (Patch)**. E.g., agent can be used to make Object.hashCode() consistent.

### Operation System Non-determinism:
Problem | Solution
---|---
Access to file handles | CUA 
Access to sockets and other devices | CUA 
Locale | CUA 
Direct RAM access | CUA 
Native calls (ability to call system-specific api) | CUA, SecurityManager ? 
Access to clocks | CUA 
System properties | CUA 

### JVM Non-determinism:
Problem | Solution
---|---
Ability to define ClassLoader and load anything | CUA, SecurityManager?
Invokedynamic | BCT or CUA
Finalyzers (introduce ND during GC) | CUA
Access to GC calls, zero GC? | CUA, PATCH
Ability to play with Threads | CUA
Access to Reflection | SecurityManager?
Ability to run JVM with arbitrary flags | ?
Floating point computation (enforce strictfp) | BCT
Usage of synchronization API  | CUA
Random number generator | CUA
Different decisions when to terminate long runnging programs. | BCT
Object.hashCode() | PATCH
Prevent exception handlers from caching Throwable, Error, ThreadDeath. | BCT or fail class loading?
volatile ? |
Weak, Soft, Phantom references ?|

### Hardware
No idea at the moment


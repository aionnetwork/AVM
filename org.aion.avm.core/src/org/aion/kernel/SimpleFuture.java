package org.aion.kernel;


/**
 * The java.util.concurrent.Future interface defines too many methods and exceptions that we don't use so this is meant to be a simpler
 * interface to accomplish the same goal.
 */
public interface SimpleFuture<R> {
    public R get();
}

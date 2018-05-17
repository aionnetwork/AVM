package org.aion.avm.internal;

/**
 * Error that indicates an internal runtime error, especially for AVM execution rule violation.
 *
 * Note: this class extends {@link Throwable} instead of {@link Error}; we need to add a check at the beginning of
 * any user defined {@link Throwable} catchers so that this error won't be caught.
 */
public class InternalError extends Throwable {
}

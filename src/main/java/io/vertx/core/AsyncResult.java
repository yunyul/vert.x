/*
 * Copyright (c) 2011-2013 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.core;

import java.util.function.Function;

/**
 * Encapsulates the result of an asynchronous operation.
 * <p>
 * Many operations in Vert.x APIs provide results back by passing an instance of this in a {@link io.vertx.core.Handler}.
 * <p>
 * The result can either have failed or succeeded.
 * <p>
 * If it failed then the cause of the failure is available with {@link #cause}.
 * <p>
 * If it succeeded then the actual result is available with {@link #result}
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public interface AsyncResult<T> {

  /**
   * The result of the operation. This will be null if the operation failed.
   *
   * @return the result or null if the operation failed.
   */
  T result();

  /**
   * A Throwable describing failure. This will be null if the operation succeeded.
   *
   * @return the cause or null if the operation succeeded.
   */
  Throwable cause();

  /**
   * Did it succeed?
   *
   * @return true if it succeded or false otherwise
   */
  boolean succeeded();

  /**
   * Did it fail?
   *
   * @return true if it failed or false otherwise
   */
  boolean failed();

  /**
   * Apply a {@code mapper} function on this async result.<p>
   *
   * The {@code mapper} is called with the completed value and this mapper returns a value. This value will complete the result returned by this method call.<p>
   *
   * If the {@code mapper} throws an exception, the returned future will be failed with this exception.<p>
   *
   * When this async result is failed, the failure will be propagated to the returned future and the {@code mapper}
   * will not be called.
   *
   * @param mapper the mapper function
   * @return the mapped async result
   */
  default <U> AsyncResult<U> map(Function<T, U> mapper) {
    return new AsyncResult<U>() {
      @Override
      public U result() {
        if (succeeded()) {
          return mapper.apply(AsyncResult.this.result());
        } else {
          return null;
        }
      }

      @Override
      public Throwable cause() {
        return AsyncResult.this.cause();
      }

      @Override
      public boolean succeeded() {
        return AsyncResult.this.succeeded();
      }

      @Override
      public boolean failed() {
        return AsyncResult.this.failed();
      }
    };
  }

  /**
   * Map the result of this async result to a specific {@code value}.<p>
   *
   * When this async result succeeds, this {@code value} will succeeed the async result returned by this method call.<p>
   *
   * When this async result fails, the failure will be propagated to the returned async result.
   *
   * @param value the value that eventually completes the mapped async result
   * @return the mapped async result
   */
  default <V> AsyncResult<V> map(V value) {
    return map(t -> value);
  }

  /**
   * Apply a {@code mapper} function on this async result.<p>
   *
   * The {@code mapper} is called with the failure and this mapper returns a value. This value will complete the result returned by this method call.<p>
   *
   * If the {@code mapper} throws an exception, the returned future will be failed with this exception.<p>
   *
   * When this async result is succeeded, the value will be propagated to the returned future and the {@code mapper}
   * will not be called.
   *
   * @param mapper the mapper function
   * @return the mapped async result
   */
  default AsyncResult<T> orElse(Function<Throwable, T> mapper) {
    return new AsyncResult<T>() {
      @Override
      public T result() {
        if (AsyncResult.this.succeeded()) {
          return AsyncResult.this.result();
        } else if (AsyncResult.this.failed()) {
          return mapper.apply(AsyncResult.this.cause());
        } else {
          return null;
        }
      }

      @Override
      public Throwable cause() {
        return null;
      }

      @Override
      public boolean succeeded() {
        return AsyncResult.this.succeeded() || AsyncResult.this.failed();
      }

      @Override
      public boolean failed() {
        return false;
      }
    };
  }

  /**
   * Map the failure of this async result to a specific {@code value}.<p>
   *
   * When this async result fails, this {@code value} will succeeed the async result returned by this method call.<p>
   *
   * When this async succeeds, the result will be propagated to the returned async result.
   *
   * @param value the value that eventually completes the mapped async result
   * @return the mapped async result
   */
  default AsyncResult<T> orElse(T value) {
    return orElse(err -> value);
  }
}

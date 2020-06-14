/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.gcolin.cache;

import net.gcolin.common.io.ByteArrayInputStream;
import net.gcolin.common.io.ByteArrayOutputStream;
import net.gcolin.common.io.Io;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.cache.CacheException;

/**
 * Convert by serializing. So if a cached object is modified out of the cache, 
 * it is not modified in the cache.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class SerializingInternalConverter<T> implements InternalConverter<T> {
  /**
   * The {@link ClassLoader} to use for locating classes to serialize/deserialize.
   * <p>
   * This is a WeakReference to prevent ClassLoader memory leaks.
   * </p>
   */
  private WeakReference<ClassLoader> classLoaderReference;

  /**
   * Constructs a {@link SerializingInternalConverter}.
   *
   * @param classLoader the {@link ClassLoader} to use for locating classes when deserializing
   */
  public SerializingInternalConverter(ClassLoader classLoader) {
    this.classLoaderReference = new WeakReference<ClassLoader>(classLoader);
  }

  /**
   * Gets the {@link ClassLoader} that will be used to locate classes during serialization and
   * deserialization.
   *
   * @return the {@link ClassLoader}
   */
  public ClassLoader getClassLoader() {
    return classLoaderReference.get();
  }
  
  @Override
  public Object toInternal(T value) {
    return new Serialized<T>(value);
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public T fromInternal(Object internal) {
    if (internal == null) {
      return null;
    } else if (internal instanceof Serialized) {
      return (T) ((Serialized) internal).deserialize(getClassLoader());
    } else {
      throw new IllegalArgumentException(
          "internal value is not a Serialized instance [" + internal + "]");
    }
  }

  /**
   * A container for a serialized object.
   *
   * @param <V> the type of value that was serialized
   */
  private static class Serialized<V> {

    /**
     * The serialized form of the value.
     */
    private final byte[] bytes;

    /**
     * The hashcode of the value.
     */
    private final int hashCode;

    /**
     * Constructs a {@link Serialized} representation of a value.
     *
     * @param value the value to be serialized (in a serialized form)
     */
    Serialized(V value) {
      if (value == null) {
        this.hashCode = 0;
        this.bytes = null;
      } else {
        this.hashCode = value.hashCode();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {          
          oos = new ObjectOutputStream(bos);
          oos.writeObject(value);
          oos.flush();
          this.bytes = bos.toByteArray();
        } catch (IOException ex) {
          throw new IllegalArgumentException(
              "Failed to serialize: " + value + " due to " + ex.getMessage(), ex);
        } finally {
          bos.release();
          Io.close(oos);
          Io.close(bos);
        }
      }
    }

    /**
     * Deserialize the {@link Serialized} value.
     *
     * @param classLoader the {@link ClassLoader} to use for resolving classes
     */
    @SuppressWarnings({"unchecked"})
    public V deserialize(ClassLoader classLoader) {
      ByteArrayInputStream bos = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = null;
      try {
        ois = new ClassLoaderObjectInputStream(bos, classLoader);

        // this must fail if the types are incompatible
        return (V) ois.readObject();
      } catch (IOException ex) {
        throw new CacheException("Failed to deserialize: " + ex.getMessage(), ex);
      } catch (ClassNotFoundException ex) {
        throw new CacheException("Failed to resolve a deserialized class: " + ex.getMessage(), ex);
      } finally {
        Io.close(ois);
        Io.close(bos);
      }
    }

    @Override
    public boolean equals(Object object) {
      if (this == object) {
        return true;
      }
      if (object == null) {
        return false;
      }
      if (!(object instanceof Serialized)) {
        return false;
      }
      Serialized<?> serialized = (Serialized<?>) object;
      if (!Arrays.equals(bytes, serialized.bytes)) {
        return false;
      }
      if (hashCode != serialized.hashCode) {
        return false;
      }
      return true;
    }
    
    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}

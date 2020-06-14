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

package net.gcolin.cache.ext;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.cache.Cache.Entry;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriter;
import javax.cache.integration.CacheWriterException;

import net.gcolin.cache.CachingProviderImpl;
import net.gcolin.cache.ClassLoaderObjectInputStream;
import net.gcolin.cache.EntryImpl;
import net.gcolin.cache.ExpirableCache;
import net.gcolin.common.collection.Func;
import net.gcolin.common.io.ByteArrayOutputStream;
import net.gcolin.common.io.Io;

/**
 * Store the cache in a file. Entry0, Entry1, Entry2, ...
 * 
 * <p>
 * Entry structure (boolean : valid, integer : entry size, integer : key size, integer : value size,
 * long : expire, data : key, data : value)
 * </p>
 * 
 * @author Gaël COLIN
 * @since 1.0
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CacheFile<K, V> extends ExpirableCache<FItem<K>>
    implements CacheLoader<K, V>, CacheWriter<K, V>, Closeable {

  private static final int HEADER_SIZE = 21;
  private long expiry = -1;
  private RandomAccessFile file;
  private Class<K> keyType;
  private Class<V> valueType;
  private Map<K, FItem<K>> table = new HashMap<>();
  private List<FItem<K>> availables = new LinkedList<>();
  private boolean deleteOnExit;
  private File repo;
  private boolean closed = false;

  /**
   * Create a cache file.
   * 
   * @param name : the name of the file
   * @param dir : the directory of the file
   * @param deleteOnExit : {@code true} if delete the file after stopping the JVM
   * @param keyType : the type of key (must be Serializable)
   * @param valueType : the type of values (must be Serializable)
   * @throws IOException if an I/O error occurs.
   */
  public CacheFile(String name, File dir, boolean deleteOnExit, Class<K> keyType,
      Class<V> valueType) throws IOException {
    this.keyType = keyType;
    this.valueType = valueType;
    this.deleteOnExit = deleteOnExit;
    repo = new File(dir, name);
    init();
    if (file.length() > 0) {
      loadTable();
    }
  }

  /**
   * Modify the configuration for mirroring the live cache data.
   * 
   * @param config Cache configuration
   */
  public void asMirror(MutableConfiguration<K, V> config) {
    config.setCacheLoaderFactory(new FactoryBuilder.SingletonFactory<>(this));
    config.setCacheWriterFactory(new FactoryBuilder.SingletonFactory<>(this));
    config.setWriteThrough(true);
    config.setReadThrough(true);
  }

  /**
   * Modify the configuration for mirroring the idle cache data.
   * 
   * @param config Cache configuration
   */
  public void asIdle(MutableConfiguration<K, V> config) {
    config.setCacheLoaderFactory(new FactoryBuilder.SingletonFactory<>(this));
    config.setReadThrough(true);
    MutableCacheEntryListenerConfiguration<K, V> conf =
        new MutableCacheEntryListenerConfiguration<>(
            new FactoryBuilder.SingletonFactory<>(new IldeListener()), null, false, true);
    config.addCacheEntryListenerConfiguration(conf);
  }

  private class IldeListener
      implements CacheEntryExpiredListener<K, V>, CacheEntryRemovedListener<K, V> {

    @Override
    public void onExpired(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      writeAll(Func.map(events, x -> new EntryImpl<>(x.getKey(), x.getValue())));
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> events) {
      deleteAll(Func.map(events, x -> x.getKey()));
    }

  }

  private void loadTable() throws IOException {
    long now = System.currentTimeMillis();
    while (file.getFilePointer() != file.length()) {
      FItem<K> item = new FItem<>();
      item.start = file.getFilePointer();
      boolean valid = file.readBoolean();
      item.entrySize = file.readInt();
      item.keySize = file.readInt();
      item.valueSize = file.readInt();
      item.expire = file.readLong();
      if (now >= item.expire) {
        valid = false;
        file.seek(file.getFilePointer() - HEADER_SIZE);
        file.writeBoolean(false);
      }
      if (valid) {
        K key = read(keyType, item.keySize);
        item.key = key;
        item.node = evictionList.insert(item);
        table.put(key, item);
      } else {
        availables.add(item);
      }
      file.seek(item.start + item.entrySize + HEADER_SIZE);
    }
  }

  @SuppressWarnings("unchecked")
  private <X> X read(Class<X> type, int size) throws IOException {
    int oldSize = size;

    int bufferSize = oldSize / Io.BUFFER_SIZE;
    if (oldSize % Io.BUFFER_SIZE != 0) {
      bufferSize++;
    }

    byte[][] array = new byte[bufferSize][];
    for (int i = 0; i < bufferSize; i++) {
      byte[] ba = array[i] = Io.takeBytes();
      int toRead = Math.min(oldSize, Io.BUFFER_SIZE);
      file.readFully(ba, 0, toRead);
      oldSize -= toRead;
    }

    ClassLoaderObjectInputStream reader = null;
    try {
      reader =
          new ClassLoaderObjectInputStream(new ByteArrayInputStream2(array), type.getClassLoader());
      return (X) reader.readObject();
    } catch (ClassNotFoundException ex) {
      throw new IOException(ex);
    } finally {
      Io.close(reader);
      for (int i = 0; i < bufferSize; i++) {
        Io.recycleBytes(array[i]);
      }
    }
  }

  public void setExpiry(long expiry) {
    this.expiry = expiry == Long.MAX_VALUE ? -1 : expiry;
  }

  public long getExpiry() {
    return expiry;
  }

  @Override
  public V load(K key) {
    cleanUp();
    lock.lock();
    try {
      return load0(key);
    } catch (IOException ex) {
      throw new CacheLoaderException(ex);
    } finally {
      lock.unlock();
    }
  }

  private V load0(K key) throws IOException {
    FItem<K> item = table.get(key);
    if (item != null) {
      file.seek(item.start + HEADER_SIZE + item.keySize);
      return read(valueType, item.valueSize);
    }
    return null;
  }

  @Override
  public Map<K, V> loadAll(Iterable<? extends K> keys) {
    cleanUp();
    lock.lock();
    try {
      Map<K, V> map = new HashMap<>();
      for (K key : keys) {
        V value = load0(key);
        if (value != null) {
          map.put(key, value);
        }
      }
      return map;
    } catch (IOException ex) {
      throw new CacheLoaderException(ex);
    } finally {
      lock.unlock();
    }
  }

  private ByteArrayOutputStream toBytes(Object obj) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ObjectOutputStream writer = null;
    try {
      writer = new ObjectOutputStream(bout);
      writer.writeObject(obj);
    } finally {
      Io.close(writer);
    }
    return bout;
  }

  @Override
  public void write(Entry<? extends K, ? extends V> entry) {
    cleanUp();
    lock.lock();
    try {
      write0(entry);
    } catch (IOException ex) {
      throw new CacheWriterException(ex);
    } finally {
      lock.unlock();
    }
  }

  private void write0(Entry<? extends K, ? extends V> entry) throws IOException {
    ByteArrayOutputStream keyOut = toBytes(entry.getKey());
    ByteArrayOutputStream valueOut = toBytes(entry.getValue());
    int keySize = keyOut.getSize();
    int valueSize = valueOut.getSize();
    int size = keySize + valueSize;
    FItem<K> select = null;

    FItem<K> existing = table.get(entry.getKey());
    if (existing != null) {
      if (existing.entrySize >= size) {
        select = existing;
      } else {
        delete0(entry.getKey());
      }
    }
    if (select == null) {
      Iterator<FItem<K>> it = availables.iterator();
      while (it.hasNext() && select == null) {
        FItem<K> item = it.next();
        if (item.entrySize >= size) {
          select = item;
          it.remove();
        }
      }
    }
    if (select == null) {
      select = new FItem<>();
      select.entrySize = size;
      select.start = file.length();
    }

    select.keySize = keySize;
    select.valueSize = valueSize;
    select.key = entry.getKey();
    select.expire = expiry == -1 ? Long.MAX_VALUE : expiry + System.currentTimeMillis();
    if (select.node == null) {
      select.node = evictionList.insert(select);
    } else {
      select.node.update();
    }
    file.seek(select.start);
    file.writeBoolean(true);
    file.writeInt(select.entrySize);
    file.writeInt(select.keySize);
    file.writeInt(select.valueSize);
    file.writeLong(select.expire);
    table.put(entry.getKey(), select);
    keyOut.writeTo(file);
    valueOut.writeTo(file);
    keyOut.release();
    valueOut.release();
  }

  @Override
  public void writeAll(Collection<Entry<? extends K, ? extends V>> entries) {
    cleanUp();
    lock.lock();
    try {
      for (Entry<? extends K, ? extends V> entry : entries) {
        write0(entry);
      }
    } catch (IOException ex) {
      throw new CacheWriterException(ex);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(Object key) {
    cleanUp();
    lock.lock();
    try {
      delete0(key);
    } catch (IOException ex) {
      throw new CacheWriterException(ex);
    } finally {
      lock.unlock();
    }
  }

  private void delete0(Object key) throws IOException {
    FItem<K> item = table.remove(key);
    if (item != null) {
      file.seek(item.start);
      file.writeBoolean(false);
      availables.add(item);
    }
  }

  @Override
  public void deleteAll(Collection<?> keys) {
    cleanUp();
    lock.lock();
    try {
      for (Object key : keys) {
        delete0(key);
      }
    } catch (IOException ex) {
      throw new CacheWriterException(ex);
    } finally {
      lock.unlock();
    }
  }

  /**
   * Unused space in the cache data file.
   * 
   * @return bytes number of unused space
   */
  public int lostSpace() {
    lock.lock();
    try {
      int sum = 0;
      for (FItem<K> item : table.values()) {
        sum += (item.entrySize - item.keySize - item.valueSize);
      }
      for (FItem<K> item : availables) {
        sum += item.entrySize + HEADER_SIZE;
      }
      return sum;
    } finally {
      lock.unlock();
    }
  }

  /**
   * optimize the cache file.
   */
  public void defragment() {
    lock.lock();
    try {
      File tmp = File.createTempFile("cache", "defrag");
      RandomAccessFile tmpFile = null;
      byte[] buffer = Io.takeBytes();
      try {
        tmpFile = new RandomAccessFile(tmp, "rw");
        file.seek(0);
        int position = 0;
        while (file.getFilePointer() != file.length()) {
          boolean valid = file.readBoolean();
          int entrySize = file.readInt();
          position += entrySize + HEADER_SIZE;
          if (valid) {
            tmpFile.writeBoolean(true);
            tmpFile.writeInt(entrySize);
            entrySize += HEADER_SIZE - 5;
            while (entrySize > 0) {
              int nb = file.read(buffer, 0, Math.min(buffer.length, entrySize));
              entrySize -= nb;
              tmpFile.write(buffer, 0, nb);
            }
          }
          file.seek(position);
        }
      } finally {
        Io.recycleBytes(buffer);
        Io.close(tmpFile);
      }
      Io.close(file);
      if (!repo.delete()) {
        CachingProviderImpl.LOGGER.warn("cannot delete " + repo);
      }
      if (!tmp.renameTo(repo)) {
        CachingProviderImpl.LOGGER.warn("cannot rename to " + repo);
      }
      init();
    } catch (IOException ex) {
      throw new CacheWriterException(ex);
    } finally {
      lock.unlock();
    }
  }

  private void init() throws FileNotFoundException {
    if (deleteOnExit) {
      repo.deleteOnExit();
    }
    file = new RandomAccessFile(repo, "rw");
  }

  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      if (!deleteOnExit && lostSpace() > Io.BUFFER_SIZE) {
        defragment();
      }
      Io.close(file);
    }
  }

  @Override
  protected void evict(FItem<K> item) {
    try {
      item.node.remove();
      item.node = null;
      delete0(item.key);
    } catch (IOException ex) {
      throw new CacheLoaderException(ex);
    }
  }

}

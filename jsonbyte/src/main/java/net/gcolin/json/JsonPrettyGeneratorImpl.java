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

package net.gcolin.json;

import net.gcolin.common.io.FastOutputStreamWriter;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.json.stream.JsonGenerator;

/**
 * The {@code JsonPrettyGeneratorImpl} class writes a Json in a writer to produce a formatted JSON.
 * The instances of JsonPrettyGeneratorImpl are pooled.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
public class JsonPrettyGeneratorImpl extends JsonGeneratorImpl {
  private int indentLevel;
  private static final String INDENT = "    ";
  private static final int POOL_SIZE = 50;
  private static final Queue<JsonPrettyGeneratorImpl> POOL = new ArrayBlockingQueue<>(POOL_SIZE);

  public JsonPrettyGeneratorImpl(Writer writer) {
    super(writer);
  }

  @Override
  protected void recycle() {
    POOL.offer(this);
  }

  public static JsonPrettyGeneratorImpl take(OutputStream source) {
    return take(new FastOutputStreamWriter(source, StandardCharsets.UTF_8.displayName()));
  }

  /**
   * Get or create a JsonPrettyGenerator.
   * 
   * @param source a writer
   * @return a JSON generator
   */
  public static JsonPrettyGeneratorImpl take(Writer source) {
    JsonPrettyGeneratorImpl obj = POOL.poll();
    if (obj == null) {
      return new JsonPrettyGeneratorImpl(source);
    } else {
      obj.source = source;
      obj.generated = false;
      return obj;
    }
  }

  @Override
  protected void startObject() {
    super.startObject();
    this.indentLevel++;
  }

  @Override
  protected void startArray() {
    super.startArray();
    this.indentLevel++;
  }

  @Override
  public JsonGenerator writeEnd() {
    writeNewLine();
    this.indentLevel--;
    writeIndent();
    super.writeEnd();
    return this;
  }

  private void writeIndent() {
    for (int i = 0; i < this.indentLevel; ++i) {
      buffer.put(INDENT);
    }
  }

  @Override
  protected void writeComma() {
    super.writeComma();
    writeNewLine();
    writeIndent();
  }

  private void writeNewLine() {
    buffer.put('\n');
  }
}

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

package net.gcolin.jsonb.build;

import java.lang.reflect.Type;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;

import net.gcolin.jsonb.JsonbDeserializerExtended;
import net.gcolin.jsonb.JsonbSerializerExtended;
import net.gcolin.jsonb.serializer.TemporalAccessorDeserializer;
import net.gcolin.jsonb.serializer.TemporalAccessorSerializer;

/**
 * A factory for generating a {@code TemporalAccessor} serializer.
 * 
 * @author Gaël COLIN
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class TemporalAccessorSerializerFactory<T> implements SerializerFactory {

  private DateTimeFormatter formatter;
  private TemporalQuery<T> query;

  public TemporalAccessorSerializerFactory(DateTimeFormatter formatter, TemporalQuery<T> query) {
    this.formatter = formatter;
    this.query = query;
  }

  @Override
  public SerializerPair create(Type genericType, JNodeBuilder nodeBuider, JContext date) {

    DateTimeFormatter df = formatter;

    if (date.getLocale() != null) {
      df = df.withLocale(date.getLocale());
    }

    return new SerializerPair((JsonbSerializerExtended) new TemporalAccessorSerializer(df),
        (JsonbDeserializerExtended) new TemporalAccessorDeserializer(df, query));
  }

}
